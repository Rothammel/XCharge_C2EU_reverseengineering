package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class ResumableUploadResult extends CompleteMultipartUploadResult {
    public ResumableUploadResult(CompleteMultipartUploadResult completeResult) {
        setRequestId(completeResult.getRequestId());
        setResponseHeader(completeResult.getResponseHeader());
        setStatusCode(completeResult.getStatusCode());
        setClientCRC(completeResult.getClientCRC());
        setServerCRC(completeResult.getServerCRC());
        setBucketName(completeResult.getBucketName());
        setObjectKey(completeResult.getObjectKey());
        setETag(completeResult.getETag());
        setLocation(completeResult.getLocation());
        setServerCallbackReturnBody(completeResult.getServerCallbackReturnBody());
    }
}
