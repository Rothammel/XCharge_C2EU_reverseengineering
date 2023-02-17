package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

/* loaded from: classes.dex */
public class GetObjectRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;
    private OSSProgressCallback progressListener;
    private Range range;
    private String xOssProcess;

    public GetObjectRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return this.objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Range getRange() {
        return this.range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getxOssProcess() {
        return this.xOssProcess;
    }

    public void setxOssProcess(String xOssProcess) {
        this.xOssProcess = xOssProcess;
    }

    public OSSProgressCallback getProgressListener() {
        return this.progressListener;
    }

    public void setProgressListener(OSSProgressCallback<GetObjectRequest> progressListener) {
        this.progressListener = progressListener;
    }
}
