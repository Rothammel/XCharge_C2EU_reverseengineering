package com.alibaba.sdk.android.oss.internal;

import android.text.TextUtils;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSSharedPreferences;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/* loaded from: classes.dex */
public class ResumableUploadTask extends BaseMultipartUploadTask<ResumableUploadRequest, ResumableUploadResult> implements Callable<ResumableUploadResult> {
    private List<Integer> mAlreadyUploadIndex;
    private File mCRC64RecordFile;
    private long mFirstPartSize;
    private File mRecordFile;
    private OSSSharedPreferences mSp;

    public ResumableUploadTask(ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback, ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
        this.mAlreadyUploadIndex = new ArrayList();
        this.mSp = OSSSharedPreferences.instance(this.mContext.getApplicationContext());
    }

    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    protected void initMultipartUploadId() throws IOException, ClientException, ServiceException {
        String uploadFilePath = ((ResumableUploadRequest) this.mRequest).getUploadFilePath();
        this.mUploadedLength = 0L;
        this.mUploadFile = new File(uploadFilePath);
        this.mFileLength = this.mUploadFile.length();
        if (this.mFileLength == 0) {
            throw new ClientException("file length must not be 0");
        }
        Map<Integer, Long> recordCrc64 = null;
        if (!OSSUtils.isEmptyString(((ResumableUploadRequest) this.mRequest).getRecordDirectory())) {
            String fileMd5 = BinaryUtil.calculateMd5Str(uploadFilePath);
            String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + ((ResumableUploadRequest) this.mRequest).getBucketName() + ((ResumableUploadRequest) this.mRequest).getObjectKey() + String.valueOf(((ResumableUploadRequest) this.mRequest).getPartSize())).getBytes());
            String recordPath = ((ResumableUploadRequest) this.mRequest).getRecordDirectory() + File.separator + recordFileName;
            this.mRecordFile = new File(recordPath);
            if (this.mRecordFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(this.mRecordFile));
                this.mUploadId = br.readLine();
                br.close();
                OSSLog.logDebug("[initUploadId] - Found record file, uploadid: " + this.mUploadId);
            }
            if (!OSSUtils.isEmptyString(this.mUploadId)) {
                if (this.mCheckCRC64) {
                    String filePath = ((ResumableUploadRequest) this.mRequest).getRecordDirectory() + File.separator + this.mUploadId;
                    File crc64Record = new File(filePath);
                    if (crc64Record.exists()) {
                        FileInputStream fs = new FileInputStream(crc64Record);
                        ObjectInputStream ois = new ObjectInputStream(fs);
                        try {
                            try {
                                recordCrc64 = (Map) ois.readObject();
                                crc64Record.delete();
                                if (ois != null) {
                                    ois.close();
                                }
                                crc64Record.delete();
                            } catch (ClassNotFoundException e) {
                                OSSLog.logThrowable2Local(e);
                                if (ois != null) {
                                    ois.close();
                                }
                                crc64Record.delete();
                            }
                        } catch (Throwable th) {
                            if (ois != null) {
                                ois.close();
                            }
                            crc64Record.delete();
                            throw th;
                        }
                    }
                }
                ListPartsRequest listParts = new ListPartsRequest(((ResumableUploadRequest) this.mRequest).getBucketName(), ((ResumableUploadRequest) this.mRequest).getObjectKey(), this.mUploadId);
                OSSAsyncTask<ListPartsResult> task = this.mApiOperation.listParts(listParts, null);
                try {
                    List<PartSummary> parts = task.getResult().getParts();
                    for (int i = 0; i < parts.size(); i++) {
                        PartSummary part = parts.get(i);
                        PartETag partETag = new PartETag(part.getPartNumber(), part.getETag());
                        partETag.setPartSize(part.getSize());
                        if (recordCrc64 != null && recordCrc64.size() > 0) {
                            if (recordCrc64.containsKey(Integer.valueOf(partETag.getPartNumber()))) {
                                partETag.setCRC64(recordCrc64.get(Integer.valueOf(partETag.getPartNumber())).longValue());
                            }
                        }
                        this.mPartETags.add(partETag);
                        this.mUploadedLength += part.getSize();
                        this.mAlreadyUploadIndex.add(Integer.valueOf(part.getPartNumber()));
                        if (i == 0) {
                            this.mFirstPartSize = part.getSize();
                        }
                    }
                } catch (ClientException e2) {
                    throw e2;
                } catch (ServiceException e3) {
                    if (e3.getStatusCode() == 404) {
                        this.mUploadId = null;
                    } else {
                        throw e3;
                    }
                }
                task.waitUntilFinished();
            }
            if (!this.mRecordFile.exists() && !this.mRecordFile.createNewFile()) {
                throw new ClientException("Can't create file at path: " + this.mRecordFile.getAbsolutePath() + "\nPlease make sure the directory exist!");
            }
        }
        if (OSSUtils.isEmptyString(this.mUploadId)) {
            InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(((ResumableUploadRequest) this.mRequest).getBucketName(), ((ResumableUploadRequest) this.mRequest).getObjectKey(), ((ResumableUploadRequest) this.mRequest).getMetadata());
            InitiateMultipartUploadResult initResult = this.mApiOperation.initMultipartUpload(init, null).getResult();
            this.mUploadId = initResult.getUploadId();
            if (this.mRecordFile != null) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(this.mRecordFile));
                bw.write(this.mUploadId);
                bw.close();
            }
        }
        ((ResumableUploadRequest) this.mRequest).setUploadId(this.mUploadId);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    public ResumableUploadResult doMultipartUpload() throws IOException, ClientException, ServiceException, InterruptedException {
        long tempUploadedLength = this.mUploadedLength;
        checkCancel();
        int[] partAttr = new int[2];
        checkPartSize(partAttr);
        int readByte = partAttr[0];
        final int partNumber = partAttr[1];
        if (this.mPartETags.size() > 0 && this.mAlreadyUploadIndex.size() > 0) {
            if (this.mUploadedLength > this.mFileLength) {
                throw new ClientException("The uploading file is inconsistent with before");
            }
            if (this.mFirstPartSize != readByte) {
                throw new ClientException("The part size setting is inconsistent with before");
            }
            long revertUploadedLength = this.mUploadedLength;
            if (!TextUtils.isEmpty(this.mSp.getStringValue(this.mUploadId))) {
                revertUploadedLength = Long.valueOf(this.mSp.getStringValue(this.mUploadId)).longValue();
            }
            if (this.mProgressCallback != null) {
                this.mProgressCallback.onProgress(this.mRequest, revertUploadedLength, this.mFileLength);
            }
            this.mSp.removeKey(this.mUploadId);
        }
        for (int i = 0; i < partNumber; i++) {
            if ((this.mAlreadyUploadIndex.size() == 0 || !this.mAlreadyUploadIndex.contains(Integer.valueOf(i + 1))) && this.mPoolExecutor != null) {
                if (i == partNumber - 1) {
                    readByte = (int) Math.min(readByte, this.mFileLength - tempUploadedLength);
                }
                final int byteCount = readByte;
                final int readIndex = i;
                tempUploadedLength += byteCount;
                this.mPoolExecutor.execute(new Runnable() { // from class: com.alibaba.sdk.android.oss.internal.ResumableUploadTask.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ResumableUploadTask.this.uploadPart(readIndex, byteCount, partNumber);
                    }
                });
            }
        }
        if (checkWaitCondition(partNumber)) {
            synchronized (this.mLock) {
                this.mLock.wait();
            }
        }
        checkException();
        CompleteMultipartUploadResult completeResult = completeMultipartUploadResult();
        ResumableUploadResult result = null;
        if (completeResult != null) {
            result = new ResumableUploadResult(completeResult);
        }
        if (this.mRecordFile != null) {
            this.mRecordFile.delete();
        }
        if (this.mCRC64RecordFile != null) {
            this.mCRC64RecordFile.delete();
        }
        releasePool();
        return result;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    public void checkException() throws IOException, ServiceException, ClientException {
        if (this.mContext.getCancellationHandler().isCancelled()) {
            if (((ResumableUploadRequest) this.mRequest).deleteUploadOnCancelling().booleanValue()) {
                abortThisUpload();
                if (this.mRecordFile != null) {
                    this.mRecordFile.delete();
                }
            } else if (this.mPartETags != null && this.mPartETags.size() > 0 && this.mCheckCRC64 && ((ResumableUploadRequest) this.mRequest).getRecordDirectory() != null) {
                Map<Integer, Long> maps = new HashMap<>();
                for (PartETag eTag : this.mPartETags) {
                    maps.put(Integer.valueOf(eTag.getPartNumber()), Long.valueOf(eTag.getCRC64()));
                }
                ObjectOutputStream oot = null;
                try {
                    try {
                        String filePath = ((ResumableUploadRequest) this.mRequest).getRecordDirectory() + File.separator + this.mUploadId;
                        this.mCRC64RecordFile = new File(filePath);
                        if (!this.mCRC64RecordFile.exists()) {
                            this.mCRC64RecordFile.createNewFile();
                        }
                        ObjectOutputStream oot2 = new ObjectOutputStream(new FileOutputStream(this.mCRC64RecordFile));
                        try {
                            oot2.writeObject(maps);
                            if (oot2 != null) {
                                oot2.close();
                            }
                        } catch (IOException e) {
                            e = e;
                            oot = oot2;
                            OSSLog.logThrowable2Local(e);
                            if (oot != null) {
                                oot.close();
                            }
                            super.checkException();
                        } catch (Throwable th) {
                            th = th;
                            oot = oot2;
                            if (oot != null) {
                                oot.close();
                            }
                            throw th;
                        }
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            }
        }
        super.checkException();
    }

    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    protected void abortThisUpload() {
        if (this.mUploadId != null) {
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(((ResumableUploadRequest) this.mRequest).getBucketName(), ((ResumableUploadRequest) this.mRequest).getObjectKey(), this.mUploadId);
            this.mApiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
        }
    }

    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    protected void processException(Exception e) {
        synchronized (this.mLock) {
            this.mPartExceptionCount++;
            if (this.mUploadException == null || !e.getMessage().equals(this.mUploadException.getMessage())) {
                this.mUploadException = e;
            }
            OSSLog.logThrowable2Local(e);
            if (this.mContext.getCancellationHandler().isCancelled() && !this.mIsCancel) {
                this.mIsCancel = true;
                this.mLock.notify();
            }
        }
    }

    @Override // com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask
    protected void uploadPartFinish(PartETag partETag) throws Exception {
        if (this.mContext.getCancellationHandler().isCancelled() && !this.mSp.contains(this.mUploadId)) {
            this.mSp.setStringValue(this.mUploadId, String.valueOf(this.mUploadedLength));
            onProgressCallback(this.mRequest, this.mUploadedLength, this.mFileLength);
        }
    }
}
