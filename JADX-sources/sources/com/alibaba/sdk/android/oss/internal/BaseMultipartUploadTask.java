package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public abstract class BaseMultipartUploadTask<Request extends MultipartUploadRequest, Result extends CompleteMultipartUploadResult> implements Callable<Result> {
    protected final int CPU_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected final int KEEP_ALIVE_TIME;
    protected final int MAX_CORE_POOL_SIZE;
    protected final int MAX_IMUM_POOL_SIZE;
    protected final int MAX_QUEUE_SIZE;
    protected InternalRequestOperation mApiOperation;
    protected boolean mCheckCRC64;
    protected OSSCompletedCallback<Request, Result> mCompletedCallback;
    protected ExecutionContext mContext;
    protected long mFileLength;
    protected boolean mIsCancel;
    protected Object mLock;
    protected List<PartETag> mPartETags;
    protected int mPartExceptionCount;
    protected ThreadPoolExecutor mPoolExecutor;
    protected OSSProgressCallback<Request> mProgressCallback;
    protected Request mRequest;
    protected int mRunPartTaskCount;
    protected Exception mUploadException;
    protected File mUploadFile;
    protected String mUploadId;
    protected long mUploadedLength;

    protected abstract void abortThisUpload();

    protected abstract Result doMultipartUpload() throws IOException, ServiceException, ClientException, InterruptedException;

    protected abstract void initMultipartUploadId() throws IOException, ClientException, ServiceException;

    protected abstract void processException(Exception exc);

    public BaseMultipartUploadTask(InternalRequestOperation operation, Request request, OSSCompletedCallback<Request, Result> completedCallback, ExecutionContext context) {
        this.MAX_CORE_POOL_SIZE = this.CPU_SIZE < 5 ? this.CPU_SIZE : 5;
        this.MAX_IMUM_POOL_SIZE = this.CPU_SIZE;
        this.KEEP_ALIVE_TIME = 3000;
        this.MAX_QUEUE_SIZE = 5000;
        this.mPoolExecutor = new ThreadPoolExecutor(this.MAX_CORE_POOL_SIZE, this.MAX_IMUM_POOL_SIZE, 3000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5000), new ThreadFactory() { // from class: com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask.1
            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "oss-android-multipart-thread");
            }
        });
        this.mPartETags = new ArrayList();
        this.mLock = new Object();
        this.mUploadedLength = 0L;
        this.mCheckCRC64 = false;
        this.mApiOperation = operation;
        this.mRequest = request;
        this.mProgressCallback = request.getProgressCallback();
        this.mCompletedCallback = completedCallback;
        this.mContext = context;
        this.mCheckCRC64 = request.getCRC64() == OSSRequest.CRC64Config.YES;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkCancel() throws ClientException {
        if (this.mContext.getCancellationHandler().isCancelled()) {
            IOException e = new IOException("multipart cancel");
            throw new ClientException(e.getMessage(), e);
        }
    }

    protected void preUploadPart(int readIndex, int byteCount, int partNumber) throws Exception {
    }

    protected void uploadPartFinish(PartETag partETag) throws Exception {
    }

    @Override // java.util.concurrent.Callable
    public Result call() throws Exception {
        try {
            initMultipartUploadId();
            Result result = doMultipartUpload();
            if (this.mCompletedCallback != null) {
                this.mCompletedCallback.onSuccess(this.mRequest, result);
            }
            return result;
        } catch (ServiceException e) {
            if (this.mCompletedCallback != null) {
                this.mCompletedCallback.onFailure(this.mRequest, null, e);
            }
            throw e;
        } catch (Exception e2) {
            ClientException temp = new ClientException(e2.toString(), e2);
            if (this.mCompletedCallback != null) {
                this.mCompletedCallback.onFailure(this.mRequest, temp, null);
            }
            throw temp;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void uploadPart(int readIndex, int byteCount, int partNumber) {
        RandomAccessFile raf = null;
        try {
            try {
                if (this.mContext.getCancellationHandler().isCancelled()) {
                    this.mPoolExecutor.getQueue().clear();
                    if (0 != 0) {
                        try {
                            raf.close();
                            return;
                        } catch (IOException e) {
                            OSSLog.logThrowable2Local(e);
                            return;
                        }
                    }
                    return;
                }
                synchronized (this.mLock) {
                    this.mRunPartTaskCount++;
                }
                preUploadPart(readIndex, byteCount, partNumber);
                RandomAccessFile raf2 = new RandomAccessFile(this.mUploadFile, "r");
                try {
                    UploadPartRequest uploadPart = new UploadPartRequest(this.mRequest.getBucketName(), this.mRequest.getObjectKey(), this.mUploadId, readIndex + 1);
                    long skip = readIndex * this.mRequest.getPartSize();
                    byte[] partContent = new byte[byteCount];
                    raf2.seek(skip);
                    raf2.readFully(partContent, 0, byteCount);
                    uploadPart.setPartContent(partContent);
                    uploadPart.setMd5Digest(BinaryUtil.calculateBase64Md5(partContent));
                    uploadPart.setCRC64(this.mRequest.getCRC64());
                    UploadPartResult uploadPartResult = this.mApiOperation.syncUploadPart(uploadPart);
                    synchronized (this.mLock) {
                        PartETag partETag = new PartETag(uploadPart.getPartNumber(), uploadPartResult.getETag());
                        partETag.setPartSize(byteCount);
                        if (this.mCheckCRC64) {
                            partETag.setCRC64(uploadPartResult.getClientCRC().longValue());
                        }
                        this.mPartETags.add(partETag);
                        this.mUploadedLength += byteCount;
                        uploadPartFinish(partETag);
                        if (!this.mContext.getCancellationHandler().isCancelled()) {
                            if (this.mPartETags.size() == partNumber - this.mPartExceptionCount) {
                                notifyMultipartThread();
                            }
                            onProgressCallback(this.mRequest, this.mUploadedLength, this.mFileLength);
                        } else if (this.mPartETags.size() == this.mRunPartTaskCount - this.mPartExceptionCount) {
                            IOException e2 = new IOException("multipart cancel");
                            throw new ClientException(e2.getMessage(), e2);
                        }
                    }
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        } catch (IOException e3) {
                            OSSLog.logThrowable2Local(e3);
                        }
                    }
                } catch (Exception e4) {
                    e = e4;
                    raf = raf2;
                    processException(e);
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e5) {
                            OSSLog.logThrowable2Local(e5);
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    raf = raf2;
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e6) {
                            OSSLog.logThrowable2Local(e6);
                        }
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CompleteMultipartUploadResult completeMultipartUploadResult() throws ClientException, ServiceException {
        CompleteMultipartUploadResult completeResult = null;
        if (this.mPartETags.size() > 0) {
            Collections.sort(this.mPartETags, new Comparator<PartETag>() { // from class: com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask.2
                @Override // java.util.Comparator
                public int compare(PartETag lhs, PartETag rhs) {
                    if (lhs.getPartNumber() < rhs.getPartNumber()) {
                        return -1;
                    }
                    if (lhs.getPartNumber() > rhs.getPartNumber()) {
                        return 1;
                    }
                    return 0;
                }
            });
            CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(this.mRequest.getBucketName(), this.mRequest.getObjectKey(), this.mUploadId, this.mPartETags);
            complete.setMetadata(this.mRequest.getMetadata());
            if (this.mRequest.getCallbackParam() != null) {
                complete.setCallbackParam(this.mRequest.getCallbackParam());
            }
            if (this.mRequest.getCallbackVars() != null) {
                complete.setCallbackVars(this.mRequest.getCallbackVars());
            }
            complete.setCRC64(this.mRequest.getCRC64());
            completeResult = this.mApiOperation.syncCompleteMultipartUpload(complete);
        }
        this.mUploadedLength = 0L;
        return completeResult;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void releasePool() {
        if (this.mPoolExecutor != null) {
            this.mPoolExecutor.getQueue().clear();
            this.mPoolExecutor.shutdown();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkException() throws IOException, ServiceException, ClientException {
        if (this.mUploadException != null) {
            releasePool();
            if (this.mUploadException instanceof IOException) {
                throw ((IOException) this.mUploadException);
            }
            if (this.mUploadException instanceof ServiceException) {
                throw ((ServiceException) this.mUploadException);
            }
            if (this.mUploadException instanceof ClientException) {
                throw ((ClientException) this.mUploadException);
            }
            ClientException clientException = new ClientException(this.mUploadException.getMessage(), this.mUploadException);
            throw clientException;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean checkWaitCondition(int partNum) {
        return this.mPartETags.size() != partNum;
    }

    protected void notifyMultipartThread() {
        this.mLock.notify();
        this.mPartExceptionCount = 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkPartSize(int[] partAttr) {
        long partSize = this.mRequest.getPartSize();
        int partNumber = (int) (this.mFileLength / partSize);
        if (this.mFileLength % partSize != 0) {
            partNumber++;
        }
        if (partNumber > 5000) {
            partSize = this.mFileLength / 5000;
        }
        partAttr[0] = (int) partSize;
        partAttr[1] = partNumber;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onProgressCallback(Request request, long currentSize, long totalSize) {
        if (this.mProgressCallback != null) {
            this.mProgressCallback.onProgress(request, currentSize, totalSize);
        }
    }
}
