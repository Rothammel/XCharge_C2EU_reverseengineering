package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class DeleteBucketRequest extends OSSRequest {
    private String bucketName;

    public DeleteBucketRequest(String bucketName) {
        setBucketName(bucketName);
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return this.bucketName;
    }
}
