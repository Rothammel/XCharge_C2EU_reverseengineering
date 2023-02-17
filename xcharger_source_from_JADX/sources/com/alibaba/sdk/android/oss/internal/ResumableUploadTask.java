package com.alibaba.sdk.android.oss.internal;

import android.text.TextUtils;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.OSSSharedPreferences;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ResumableUploadTask extends BaseMultipartUploadTask<ResumableUploadRequest, ResumableUploadResult> implements Callable<ResumableUploadResult> {
    private List<Integer> mAlreadyUploadIndex = new ArrayList();
    private File mCRC64RecordFile;
    private long mFirstPartSize;
    private File mRecordFile;
    private OSSSharedPreferences mSp = OSSSharedPreferences.instance(this.mContext.getApplicationContext());

    public ResumableUploadTask(ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback, ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v94, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v98, resolved type: java.util.Map} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initMultipartUploadId() throws java.io.IOException, com.alibaba.sdk.android.oss.ClientException, com.alibaba.sdk.android.oss.ServiceException {
        /*
            r28 = this;
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r23 = r24.getUploadFilePath()
            r24 = 0
            r0 = r24
            r2 = r28
            r2.mUploadedLength = r0
            java.io.File r24 = new java.io.File
            r0 = r24
            r1 = r23
            r0.<init>(r1)
            r0 = r24
            r1 = r28
            r1.mUploadFile = r0
            r0 = r28
            java.io.File r0 = r0.mUploadFile
            r24 = r0
            long r24 = r24.length()
            r0 = r24
            r2 = r28
            r2.mFileLength = r0
            r0 = r28
            long r0 = r0.mFileLength
            r24 = r0
            r26 = 0
            int r24 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1))
            if (r24 != 0) goto L_0x0047
            com.alibaba.sdk.android.oss.ClientException r24 = new com.alibaba.sdk.android.oss.ClientException
            java.lang.String r25 = "file length must not be 0"
            r24.<init>((java.lang.String) r25)
            throw r24
        L_0x0047:
            r19 = 0
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getRecordDirectory()
            boolean r24 = com.alibaba.sdk.android.oss.common.utils.OSSUtils.isEmptyString(r24)
            if (r24 != 0) goto L_0x02ff
            java.lang.String r8 = com.alibaba.sdk.android.oss.common.utils.BinaryUtil.calculateMd5Str((java.lang.String) r23)
            java.lang.StringBuilder r24 = new java.lang.StringBuilder
            r24.<init>()
            r0 = r24
            java.lang.StringBuilder r25 = r0.append(r8)
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getBucketName()
            r0 = r25
            r1 = r24
            java.lang.StringBuilder r25 = r0.append(r1)
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getObjectKey()
            r0 = r25
            r1 = r24
            java.lang.StringBuilder r25 = r0.append(r1)
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            long r26 = r24.getPartSize()
            java.lang.String r24 = java.lang.String.valueOf(r26)
            r0 = r25
            r1 = r24
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r24 = r24.toString()
            byte[] r24 = r24.getBytes()
            java.lang.String r20 = com.alibaba.sdk.android.oss.common.utils.BinaryUtil.calculateMd5Str((byte[]) r24)
            java.lang.StringBuilder r25 = new java.lang.StringBuilder
            r25.<init>()
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getRecordDirectory()
            r0 = r25
            r1 = r24
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r25 = java.io.File.separator
            java.lang.StringBuilder r24 = r24.append(r25)
            r0 = r24
            r1 = r20
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r21 = r24.toString()
            java.io.File r24 = new java.io.File
            r0 = r24
            r1 = r21
            r0.<init>(r1)
            r0 = r24
            r1 = r28
            r1.mRecordFile = r0
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r24 = r0
            boolean r24 = r24.exists()
            if (r24 == 0) goto L_0x0137
            java.io.BufferedReader r4 = new java.io.BufferedReader
            java.io.FileReader r24 = new java.io.FileReader
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r25 = r0
            r24.<init>(r25)
            r0 = r24
            r4.<init>(r0)
            java.lang.String r24 = r4.readLine()
            r0 = r24
            r1 = r28
            r1.mUploadId = r0
            r4.close()
            java.lang.StringBuilder r24 = new java.lang.StringBuilder
            r24.<init>()
            java.lang.String r25 = "[initUploadId] - Found record file, uploadid: "
            java.lang.StringBuilder r24 = r24.append(r25)
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r25 = r0
            java.lang.StringBuilder r24 = r24.append(r25)
            java.lang.String r24 = r24.toString()
            com.alibaba.sdk.android.oss.common.OSSLog.logDebug(r24)
        L_0x0137:
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r24 = r0
            boolean r24 = com.alibaba.sdk.android.oss.common.utils.OSSUtils.isEmptyString(r24)
            if (r24 != 0) goto L_0x02bb
            r0 = r28
            boolean r0 = r0.mCheckCRC64
            r24 = r0
            if (r24 == 0) goto L_0x01a2
            java.lang.StringBuilder r25 = new java.lang.StringBuilder
            r25.<init>()
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getRecordDirectory()
            r0 = r25
            r1 = r24
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r25 = java.io.File.separator
            java.lang.StringBuilder r24 = r24.append(r25)
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r25 = r0
            java.lang.StringBuilder r24 = r24.append(r25)
            java.lang.String r9 = r24.toString()
            java.io.File r6 = new java.io.File
            r6.<init>(r9)
            boolean r24 = r6.exists()
            if (r24 == 0) goto L_0x01a2
            java.io.FileInputStream r10 = new java.io.FileInputStream
            r10.<init>(r6)
            java.io.ObjectInputStream r15 = new java.io.ObjectInputStream
            r15.<init>(r10)
            java.lang.Object r24 = r15.readObject()     // Catch:{ ClassNotFoundException -> 0x028b }
            r0 = r24
            java.util.Map r0 = (java.util.Map) r0     // Catch:{ ClassNotFoundException -> 0x028b }
            r19 = r0
            r6.delete()     // Catch:{ ClassNotFoundException -> 0x028b }
            if (r15 == 0) goto L_0x019f
            r15.close()
        L_0x019f:
            r6.delete()
        L_0x01a2:
            com.alibaba.sdk.android.oss.model.ListPartsRequest r14 = new com.alibaba.sdk.android.oss.model.ListPartsRequest
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r25 = r24.getBucketName()
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r24 = r24.getObjectKey()
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r26 = r0
            r0 = r25
            r1 = r24
            r2 = r26
            r14.<init>(r0, r1, r2)
            r0 = r28
            com.alibaba.sdk.android.oss.internal.InternalRequestOperation r0 = r0.mApiOperation
            r24 = r0
            r25 = 0
            r0 = r24
            r1 = r25
            com.alibaba.sdk.android.oss.internal.OSSAsyncTask r22 = r0.listParts(r14, r1)
            com.alibaba.sdk.android.oss.model.OSSResult r24 = r22.getResult()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            com.alibaba.sdk.android.oss.model.ListPartsResult r24 = (com.alibaba.sdk.android.oss.model.ListPartsResult) r24     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.util.List r18 = r24.getParts()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r11 = 0
        L_0x01e6:
            int r24 = r18.size()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r24
            if (r11 >= r0) goto L_0x02b8
            r0 = r18
            java.lang.Object r16 = r0.get(r11)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            com.alibaba.sdk.android.oss.model.PartSummary r16 = (com.alibaba.sdk.android.oss.model.PartSummary) r16     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            com.alibaba.sdk.android.oss.model.PartETag r17 = new com.alibaba.sdk.android.oss.model.PartETag     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            int r24 = r16.getPartNumber()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.lang.String r25 = r16.getETag()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r17
            r1 = r24
            r2 = r25
            r0.<init>(r1, r2)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            long r24 = r16.getSize()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r17
            r1 = r24
            r0.setPartSize(r1)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            if (r19 == 0) goto L_0x024b
            int r24 = r19.size()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            if (r24 <= 0) goto L_0x024b
            int r24 = r17.getPartNumber()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.lang.Integer r24 = java.lang.Integer.valueOf(r24)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r19
            r1 = r24
            boolean r24 = r0.containsKey(r1)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            if (r24 == 0) goto L_0x024b
            int r24 = r17.getPartNumber()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.lang.Integer r24 = java.lang.Integer.valueOf(r24)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r19
            r1 = r24
            java.lang.Object r24 = r0.get(r1)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.lang.Long r24 = (java.lang.Long) r24     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            long r24 = r24.longValue()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r17
            r1 = r24
            r0.setCRC64(r1)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
        L_0x024b:
            r0 = r28
            java.util.List r0 = r0.mPartETags     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r24 = r0
            r0 = r24
            r1 = r17
            r0.add(r1)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r28
            long r0 = r0.mUploadedLength     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r24 = r0
            long r26 = r16.getSize()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            long r24 = r24 + r26
            r0 = r24
            r2 = r28
            r2.mUploadedLength = r0     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r28
            java.util.List<java.lang.Integer> r0 = r0.mAlreadyUploadIndex     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r24 = r0
            int r25 = r16.getPartNumber()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            java.lang.Integer r25 = java.lang.Integer.valueOf(r25)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r24.add(r25)     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            if (r11 != 0) goto L_0x0287
            long r24 = r16.getSize()     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
            r0 = r24
            r2 = r28
            r2.mFirstPartSize = r0     // Catch:{ ServiceException -> 0x02a3, ClientException -> 0x02fd }
        L_0x0287:
            int r11 = r11 + 1
            goto L_0x01e6
        L_0x028b:
            r7 = move-exception
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r7)     // Catch:{ all -> 0x0299 }
            if (r15 == 0) goto L_0x0294
            r15.close()
        L_0x0294:
            r6.delete()
            goto L_0x01a2
        L_0x0299:
            r24 = move-exception
            if (r15 == 0) goto L_0x029f
            r15.close()
        L_0x029f:
            r6.delete()
            throw r24
        L_0x02a3:
            r7 = move-exception
            int r24 = r7.getStatusCode()
            r25 = 404(0x194, float:5.66E-43)
            r0 = r24
            r1 = r25
            if (r0 != r1) goto L_0x02fc
            r24 = 0
            r0 = r24
            r1 = r28
            r1.mUploadId = r0
        L_0x02b8:
            r22.waitUntilFinished()
        L_0x02bb:
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r24 = r0
            boolean r24 = r24.exists()
            if (r24 != 0) goto L_0x02ff
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r24 = r0
            boolean r24 = r24.createNewFile()
            if (r24 != 0) goto L_0x02ff
            com.alibaba.sdk.android.oss.ClientException r24 = new com.alibaba.sdk.android.oss.ClientException
            java.lang.StringBuilder r25 = new java.lang.StringBuilder
            r25.<init>()
            java.lang.String r26 = "Can't create file at path: "
            java.lang.StringBuilder r25 = r25.append(r26)
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r26 = r0
            java.lang.String r26 = r26.getAbsolutePath()
            java.lang.StringBuilder r25 = r25.append(r26)
            java.lang.String r26 = "\nPlease make sure the directory exist!"
            java.lang.StringBuilder r25 = r25.append(r26)
            java.lang.String r25 = r25.toString()
            r24.<init>((java.lang.String) r25)
            throw r24
        L_0x02fc:
            throw r7
        L_0x02fd:
            r7 = move-exception
            throw r7
        L_0x02ff:
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r24 = r0
            boolean r24 = com.alibaba.sdk.android.oss.common.utils.OSSUtils.isEmptyString(r24)
            if (r24 == 0) goto L_0x0382
            com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest r12 = new com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r25 = r24.getBucketName()
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            java.lang.String r26 = r24.getObjectKey()
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            com.alibaba.sdk.android.oss.model.ObjectMetadata r24 = r24.getMetadata()
            r0 = r25
            r1 = r26
            r2 = r24
            r12.<init>(r0, r1, r2)
            r0 = r28
            com.alibaba.sdk.android.oss.internal.InternalRequestOperation r0 = r0.mApiOperation
            r24 = r0
            r25 = 0
            r0 = r24
            r1 = r25
            com.alibaba.sdk.android.oss.internal.OSSAsyncTask r24 = r0.initMultipartUpload(r12, r1)
            com.alibaba.sdk.android.oss.model.OSSResult r13 = r24.getResult()
            com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult r13 = (com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult) r13
            java.lang.String r24 = r13.getUploadId()
            r0 = r24
            r1 = r28
            r1.mUploadId = r0
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r24 = r0
            if (r24 == 0) goto L_0x0382
            java.io.BufferedWriter r5 = new java.io.BufferedWriter
            java.io.FileWriter r24 = new java.io.FileWriter
            r0 = r28
            java.io.File r0 = r0.mRecordFile
            r25 = r0
            r24.<init>(r25)
            r0 = r24
            r5.<init>(r0)
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r24 = r0
            r0 = r24
            r5.write(r0)
            r5.close()
        L_0x0382:
            r0 = r28
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r0 = r0.mRequest
            r24 = r0
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r24 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r24
            r0 = r28
            java.lang.String r0 = r0.mUploadId
            r25 = r0
            r24.setUploadId(r25)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.sdk.android.oss.internal.ResumableUploadTask.initMultipartUploadId():void");
    }

    /* access modifiers changed from: protected */
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
            } else if (this.mFirstPartSize != ((long) readByte)) {
                throw new ClientException("The part size setting is inconsistent with before");
            } else {
                long revertUploadedLength = this.mUploadedLength;
                if (!TextUtils.isEmpty(this.mSp.getStringValue(this.mUploadId))) {
                    revertUploadedLength = Long.valueOf(this.mSp.getStringValue(this.mUploadId)).longValue();
                }
                if (this.mProgressCallback != null) {
                    this.mProgressCallback.onProgress(this.mRequest, revertUploadedLength, this.mFileLength);
                }
                this.mSp.removeKey(this.mUploadId);
            }
        }
        for (int i = 0; i < partNumber; i++) {
            if ((this.mAlreadyUploadIndex.size() == 0 || !this.mAlreadyUploadIndex.contains(Integer.valueOf(i + 1))) && this.mPoolExecutor != null) {
                if (i == partNumber - 1) {
                    readByte = (int) Math.min((long) readByte, this.mFileLength - tempUploadedLength);
                }
                final int byteCount = readByte;
                final int readIndex = i;
                tempUploadedLength += (long) byteCount;
                this.mPoolExecutor.execute(new Runnable() {
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkException() throws java.io.IOException, com.alibaba.sdk.android.oss.ServiceException, com.alibaba.sdk.android.oss.ClientException {
        /*
            r10 = this;
            com.alibaba.sdk.android.oss.network.ExecutionContext r6 = r10.mContext
            com.alibaba.sdk.android.oss.network.CancellationHandler r6 = r6.getCancellationHandler()
            boolean r6 = r6.isCancelled()
            if (r6 == 0) goto L_0x0026
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r6 = r10.mRequest
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r6 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r6
            java.lang.Boolean r6 = r6.deleteUploadOnCancelling()
            boolean r6 = r6.booleanValue()
            if (r6 == 0) goto L_0x002a
            r10.abortThisUpload()
            java.io.File r6 = r10.mRecordFile
            if (r6 == 0) goto L_0x0026
            java.io.File r6 = r10.mRecordFile
            r6.delete()
        L_0x0026:
            super.checkException()
            return
        L_0x002a:
            java.util.List r6 = r10.mPartETags
            if (r6 == 0) goto L_0x0026
            java.util.List r6 = r10.mPartETags
            int r6 = r6.size()
            if (r6 <= 0) goto L_0x0026
            boolean r6 = r10.mCheckCRC64
            if (r6 == 0) goto L_0x0026
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r6 = r10.mRequest
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r6 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r6
            java.lang.String r6 = r6.getRecordDirectory()
            if (r6 == 0) goto L_0x0026
            java.util.HashMap r3 = new java.util.HashMap
            r3.<init>()
            java.util.List r6 = r10.mPartETags
            java.util.Iterator r6 = r6.iterator()
        L_0x004f:
            boolean r7 = r6.hasNext()
            if (r7 == 0) goto L_0x006f
            java.lang.Object r1 = r6.next()
            com.alibaba.sdk.android.oss.model.PartETag r1 = (com.alibaba.sdk.android.oss.model.PartETag) r1
            int r7 = r1.getPartNumber()
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            long r8 = r1.getCRC64()
            java.lang.Long r8 = java.lang.Long.valueOf(r8)
            r3.put(r7, r8)
            goto L_0x004f
        L_0x006f:
            r4 = 0
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x00bb }
            r7.<init>()     // Catch:{ IOException -> 0x00bb }
            com.alibaba.sdk.android.oss.model.MultipartUploadRequest r6 = r10.mRequest     // Catch:{ IOException -> 0x00bb }
            com.alibaba.sdk.android.oss.model.ResumableUploadRequest r6 = (com.alibaba.sdk.android.oss.model.ResumableUploadRequest) r6     // Catch:{ IOException -> 0x00bb }
            java.lang.String r6 = r6.getRecordDirectory()     // Catch:{ IOException -> 0x00bb }
            java.lang.StringBuilder r6 = r7.append(r6)     // Catch:{ IOException -> 0x00bb }
            java.lang.String r7 = java.io.File.separator     // Catch:{ IOException -> 0x00bb }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x00bb }
            java.lang.String r7 = r10.mUploadId     // Catch:{ IOException -> 0x00bb }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x00bb }
            java.lang.String r2 = r6.toString()     // Catch:{ IOException -> 0x00bb }
            java.io.File r6 = new java.io.File     // Catch:{ IOException -> 0x00bb }
            r6.<init>(r2)     // Catch:{ IOException -> 0x00bb }
            r10.mCRC64RecordFile = r6     // Catch:{ IOException -> 0x00bb }
            java.io.File r6 = r10.mCRC64RecordFile     // Catch:{ IOException -> 0x00bb }
            boolean r6 = r6.exists()     // Catch:{ IOException -> 0x00bb }
            if (r6 != 0) goto L_0x00a5
            java.io.File r6 = r10.mCRC64RecordFile     // Catch:{ IOException -> 0x00bb }
            r6.createNewFile()     // Catch:{ IOException -> 0x00bb }
        L_0x00a5:
            java.io.ObjectOutputStream r5 = new java.io.ObjectOutputStream     // Catch:{ IOException -> 0x00bb }
            java.io.FileOutputStream r6 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x00bb }
            java.io.File r7 = r10.mCRC64RecordFile     // Catch:{ IOException -> 0x00bb }
            r6.<init>(r7)     // Catch:{ IOException -> 0x00bb }
            r5.<init>(r6)     // Catch:{ IOException -> 0x00bb }
            r5.writeObject(r3)     // Catch:{ IOException -> 0x00d0, all -> 0x00cd }
            if (r5 == 0) goto L_0x0026
            r5.close()
            goto L_0x0026
        L_0x00bb:
            r0 = move-exception
        L_0x00bc:
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r0)     // Catch:{ all -> 0x00c6 }
            if (r4 == 0) goto L_0x0026
            r4.close()
            goto L_0x0026
        L_0x00c6:
            r6 = move-exception
        L_0x00c7:
            if (r4 == 0) goto L_0x00cc
            r4.close()
        L_0x00cc:
            throw r6
        L_0x00cd:
            r6 = move-exception
            r4 = r5
            goto L_0x00c7
        L_0x00d0:
            r0 = move-exception
            r4 = r5
            goto L_0x00bc
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.sdk.android.oss.internal.ResumableUploadTask.checkException():void");
    }

    /* access modifiers changed from: protected */
    public void abortThisUpload() {
        if (this.mUploadId != null) {
            this.mApiOperation.abortMultipartUpload(new AbortMultipartUploadRequest(((ResumableUploadRequest) this.mRequest).getBucketName(), ((ResumableUploadRequest) this.mRequest).getObjectKey(), this.mUploadId), (OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult>) null).waitUntilFinished();
        }
    }

    /* access modifiers changed from: protected */
    public void processException(Exception e) {
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

    /* access modifiers changed from: protected */
    public void uploadPartFinish(PartETag partETag) throws Exception {
        if (this.mContext.getCancellationHandler().isCancelled() && !this.mSp.contains(this.mUploadId)) {
            this.mSp.setStringValue(this.mUploadId, String.valueOf(this.mUploadedLength));
            onProgressCallback(this.mRequest, this.mUploadedLength, this.mFileLength);
        }
    }
}
