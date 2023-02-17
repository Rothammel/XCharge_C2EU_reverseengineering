package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseMultipartUploadTask<Request extends MultipartUploadRequest, Result extends CompleteMultipartUploadResult> implements Callable<Result> {
    protected final int CPU_SIZE = (Runtime.getRuntime().availableProcessors() * 2);
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

    /* access modifiers changed from: protected */
    public abstract void abortThisUpload();

    /* access modifiers changed from: protected */
    public abstract Result doMultipartUpload() throws IOException, ServiceException, ClientException, InterruptedException;

    /* access modifiers changed from: protected */
    public abstract void initMultipartUploadId() throws IOException, ClientException, ServiceException;

    /* access modifiers changed from: protected */
    public abstract void processException(Exception exc);

    public BaseMultipartUploadTask(InternalRequestOperation operation, Request request, OSSCompletedCallback<Request, Result> completedCallback, ExecutionContext context) {
        boolean z;
        int i = 5;
        this.MAX_CORE_POOL_SIZE = this.CPU_SIZE < 5 ? this.CPU_SIZE : i;
        this.MAX_IMUM_POOL_SIZE = this.CPU_SIZE;
        this.KEEP_ALIVE_TIME = 3000;
        this.MAX_QUEUE_SIZE = 5000;
        this.mPoolExecutor = new ThreadPoolExecutor(this.MAX_CORE_POOL_SIZE, this.MAX_IMUM_POOL_SIZE, 3000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5000), new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "oss-android-multipart-thread");
            }
        });
        this.mPartETags = new ArrayList();
        this.mLock = new Object();
        this.mUploadedLength = 0;
        this.mCheckCRC64 = false;
        this.mApiOperation = operation;
        this.mRequest = request;
        this.mProgressCallback = request.getProgressCallback();
        this.mCompletedCallback = completedCallback;
        this.mContext = context;
        if (request.getCRC64() == OSSRequest.CRC64Config.YES) {
            z = true;
        } else {
            z = false;
        }
        this.mCheckCRC64 = z;
    }

    /* access modifiers changed from: protected */
    public void checkCancel() throws ClientException {
        if (this.mContext.getCancellationHandler().isCancelled()) {
            IOException e = new IOException("multipart cancel");
            throw new ClientException(e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public void preUploadPart(int readIndex, int byteCount, int partNumber) throws Exception {
    }

    /* access modifiers changed from: protected */
    public void uploadPartFinish(PartETag partETag) throws Exception {
    }

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
                this.mCompletedCallback.onFailure(this.mRequest, (ClientException) null, e);
            }
            throw e;
        } catch (Exception e2) {
            ClientException temp = new ClientException(e2.toString(), e2);
            if (this.mCompletedCallback != null) {
                this.mCompletedCallback.onFailure(this.mRequest, temp, (ServiceException) null);
            }
            throw temp;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x015d A[SYNTHETIC, Splitter:B:68:0x015d] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void uploadPart(int r19, int r20, int r21) {
        /*
            r18 = this;
            r11 = 0
            r0 = r18
            com.alibaba.sdk.android.oss.network.ExecutionContext r2 = r0.mContext     // Catch:{ Exception -> 0x0124 }
            com.alibaba.sdk.android.oss.network.CancellationHandler r2 = r2.getCancellationHandler()     // Catch:{ Exception -> 0x0124 }
            boolean r2 = r2.isCancelled()     // Catch:{ Exception -> 0x0124 }
            if (r2 == 0) goto L_0x0025
            r0 = r18
            java.util.concurrent.ThreadPoolExecutor r2 = r0.mPoolExecutor     // Catch:{ Exception -> 0x0124 }
            java.util.concurrent.BlockingQueue r2 = r2.getQueue()     // Catch:{ Exception -> 0x0124 }
            r2.clear()     // Catch:{ Exception -> 0x0124 }
            if (r11 == 0) goto L_0x001f
            r11.close()     // Catch:{ IOException -> 0x0020 }
        L_0x001f:
            return
        L_0x0020:
            r8 = move-exception
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r8)
            goto L_0x001f
        L_0x0025:
            r0 = r18
            java.lang.Object r3 = r0.mLock     // Catch:{ Exception -> 0x0124 }
            monitor-enter(r3)     // Catch:{ Exception -> 0x0124 }
            r0 = r18
            int r2 = r0.mRunPartTaskCount     // Catch:{ all -> 0x0121 }
            int r2 = r2 + 1
            r0 = r18
            r0.mRunPartTaskCount = r2     // Catch:{ all -> 0x0121 }
            monitor-exit(r3)     // Catch:{ all -> 0x0121 }
            r18.preUploadPart(r19, r20, r21)     // Catch:{ Exception -> 0x0124 }
            java.io.RandomAccessFile r12 = new java.io.RandomAccessFile     // Catch:{ Exception -> 0x0124 }
            r0 = r18
            java.io.File r2 = r0.mUploadFile     // Catch:{ Exception -> 0x0124 }
            java.lang.String r3 = "r"
            r12.<init>(r2, r3)     // Catch:{ Exception -> 0x0124 }
            com.alibaba.sdk.android.oss.model.UploadPartRequest r13 = new com.alibaba.sdk.android.oss.model.UploadPartRequest     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            Request r2 = r0.mRequest     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            java.lang.String r2 = r2.getBucketName()     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            Request r3 = r0.mRequest     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            java.lang.String r3 = r3.getObjectKey()     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            java.lang.String r4 = r0.mUploadId     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            int r5 = r19 + 1
            r13.<init>(r2, r3, r4, r5)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r19
            long r2 = (long) r0     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            Request r4 = r0.mRequest     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            long r4 = r4.getPartSize()     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            long r14 = r2 * r4
            r0 = r20
            byte[] r9 = new byte[r0]     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r12.seek(r14)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r2 = 0
            r0 = r20
            r12.readFully(r9, r2, r0)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r13.setPartContent(r9)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            java.lang.String r2 = com.alibaba.sdk.android.oss.common.utils.BinaryUtil.calculateBase64Md5((byte[]) r9)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r13.setMd5Digest(r2)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            Request r2 = r0.mRequest     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            java.lang.Enum r2 = r2.getCRC64()     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r13.setCRC64(r2)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            com.alibaba.sdk.android.oss.internal.InternalRequestOperation r2 = r0.mApiOperation     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            com.alibaba.sdk.android.oss.model.UploadPartResult r16 = r2.syncUploadPart(r13)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r0 = r18
            java.lang.Object r0 = r0.mLock     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            r17 = r0
            monitor-enter(r17)     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
            com.alibaba.sdk.android.oss.model.PartETag r10 = new com.alibaba.sdk.android.oss.model.PartETag     // Catch:{ all -> 0x010a }
            int r2 = r13.getPartNumber()     // Catch:{ all -> 0x010a }
            java.lang.String r3 = r16.getETag()     // Catch:{ all -> 0x010a }
            r10.<init>(r2, r3)     // Catch:{ all -> 0x010a }
            r0 = r20
            long r2 = (long) r0     // Catch:{ all -> 0x010a }
            r10.setPartSize(r2)     // Catch:{ all -> 0x010a }
            r0 = r18
            boolean r2 = r0.mCheckCRC64     // Catch:{ all -> 0x010a }
            if (r2 == 0) goto L_0x00c0
            java.lang.Long r2 = r16.getClientCRC()     // Catch:{ all -> 0x010a }
            long r2 = r2.longValue()     // Catch:{ all -> 0x010a }
            r10.setCRC64(r2)     // Catch:{ all -> 0x010a }
        L_0x00c0:
            r0 = r18
            java.util.List<com.alibaba.sdk.android.oss.model.PartETag> r2 = r0.mPartETags     // Catch:{ all -> 0x010a }
            r2.add(r10)     // Catch:{ all -> 0x010a }
            r0 = r18
            long r2 = r0.mUploadedLength     // Catch:{ all -> 0x010a }
            r0 = r20
            long r4 = (long) r0     // Catch:{ all -> 0x010a }
            long r2 = r2 + r4
            r0 = r18
            r0.mUploadedLength = r2     // Catch:{ all -> 0x010a }
            r0 = r18
            r0.uploadPartFinish(r10)     // Catch:{ all -> 0x010a }
            r0 = r18
            com.alibaba.sdk.android.oss.network.ExecutionContext r2 = r0.mContext     // Catch:{ all -> 0x010a }
            com.alibaba.sdk.android.oss.network.CancellationHandler r2 = r2.getCancellationHandler()     // Catch:{ all -> 0x010a }
            boolean r2 = r2.isCancelled()     // Catch:{ all -> 0x010a }
            if (r2 == 0) goto L_0x0126
            r0 = r18
            java.util.List<com.alibaba.sdk.android.oss.model.PartETag> r2 = r0.mPartETags     // Catch:{ all -> 0x010a }
            int r2 = r2.size()     // Catch:{ all -> 0x010a }
            r0 = r18
            int r3 = r0.mRunPartTaskCount     // Catch:{ all -> 0x010a }
            r0 = r18
            int r4 = r0.mPartExceptionCount     // Catch:{ all -> 0x010a }
            int r3 = r3 - r4
            if (r2 != r3) goto L_0x014a
            java.io.IOException r8 = new java.io.IOException     // Catch:{ all -> 0x010a }
            java.lang.String r2 = "multipart cancel"
            r8.<init>(r2)     // Catch:{ all -> 0x010a }
            com.alibaba.sdk.android.oss.ClientException r2 = new com.alibaba.sdk.android.oss.ClientException     // Catch:{ all -> 0x010a }
            java.lang.String r3 = r8.getMessage()     // Catch:{ all -> 0x010a }
            r2.<init>(r3, r8)     // Catch:{ all -> 0x010a }
            throw r2     // Catch:{ all -> 0x010a }
        L_0x010a:
            r2 = move-exception
            monitor-exit(r17)     // Catch:{ all -> 0x010a }
            throw r2     // Catch:{ Exception -> 0x010d, all -> 0x0166 }
        L_0x010d:
            r8 = move-exception
            r11 = r12
        L_0x010f:
            r0 = r18
            r0.processException(r8)     // Catch:{ all -> 0x015a }
            if (r11 == 0) goto L_0x001f
            r11.close()     // Catch:{ IOException -> 0x011b }
            goto L_0x001f
        L_0x011b:
            r8 = move-exception
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r8)
            goto L_0x001f
        L_0x0121:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0121 }
            throw r2     // Catch:{ Exception -> 0x0124 }
        L_0x0124:
            r8 = move-exception
            goto L_0x010f
        L_0x0126:
            r0 = r18
            java.util.List<com.alibaba.sdk.android.oss.model.PartETag> r2 = r0.mPartETags     // Catch:{ all -> 0x010a }
            int r2 = r2.size()     // Catch:{ all -> 0x010a }
            r0 = r18
            int r3 = r0.mPartExceptionCount     // Catch:{ all -> 0x010a }
            int r3 = r21 - r3
            if (r2 != r3) goto L_0x0139
            r18.notifyMultipartThread()     // Catch:{ all -> 0x010a }
        L_0x0139:
            r0 = r18
            Request r3 = r0.mRequest     // Catch:{ all -> 0x010a }
            r0 = r18
            long r4 = r0.mUploadedLength     // Catch:{ all -> 0x010a }
            r0 = r18
            long r6 = r0.mFileLength     // Catch:{ all -> 0x010a }
            r2 = r18
            r2.onProgressCallback(r3, r4, r6)     // Catch:{ all -> 0x010a }
        L_0x014a:
            monitor-exit(r17)     // Catch:{ all -> 0x010a }
            if (r12 == 0) goto L_0x0150
            r12.close()     // Catch:{ IOException -> 0x0153 }
        L_0x0150:
            r11 = r12
            goto L_0x001f
        L_0x0153:
            r8 = move-exception
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r8)
            r11 = r12
            goto L_0x001f
        L_0x015a:
            r2 = move-exception
        L_0x015b:
            if (r11 == 0) goto L_0x0160
            r11.close()     // Catch:{ IOException -> 0x0161 }
        L_0x0160:
            throw r2
        L_0x0161:
            r8 = move-exception
            com.alibaba.sdk.android.oss.common.OSSLog.logThrowable2Local(r8)
            goto L_0x0160
        L_0x0166:
            r2 = move-exception
            r11 = r12
            goto L_0x015b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.sdk.android.oss.internal.BaseMultipartUploadTask.uploadPart(int, int, int):void");
    }

    /* access modifiers changed from: protected */
    public CompleteMultipartUploadResult completeMultipartUploadResult() throws ClientException, ServiceException {
        CompleteMultipartUploadResult completeResult = null;
        if (this.mPartETags.size() > 0) {
            Collections.sort(this.mPartETags, new Comparator<PartETag>() {
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
        this.mUploadedLength = 0;
        return completeResult;
    }

    /* access modifiers changed from: protected */
    public void releasePool() {
        if (this.mPoolExecutor != null) {
            this.mPoolExecutor.getQueue().clear();
            this.mPoolExecutor.shutdown();
        }
    }

    /* access modifiers changed from: protected */
    public void checkException() throws IOException, ServiceException, ClientException {
        if (this.mUploadException != null) {
            releasePool();
            if (this.mUploadException instanceof IOException) {
                throw ((IOException) this.mUploadException);
            } else if (this.mUploadException instanceof ServiceException) {
                throw ((ServiceException) this.mUploadException);
            } else if (this.mUploadException instanceof ClientException) {
                throw ((ClientException) this.mUploadException);
            } else {
                throw new ClientException(this.mUploadException.getMessage(), this.mUploadException);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkWaitCondition(int partNum) {
        if (this.mPartETags.size() == partNum) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyMultipartThread() {
        this.mLock.notify();
        this.mPartExceptionCount = 0;
    }

    /* access modifiers changed from: protected */
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

    /* access modifiers changed from: protected */
    public void onProgressCallback(Request request, long currentSize, long totalSize) {
        if (this.mProgressCallback != null) {
            this.mProgressCallback.onProgress(request, currentSize, totalSize);
        }
    }
}
