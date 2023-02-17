package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class DeleteObjectRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;

    public DeleteObjectRequest(String bucketName, String objectKey) {
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
}
