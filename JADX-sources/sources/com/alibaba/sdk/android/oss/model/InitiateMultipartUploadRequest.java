package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class InitiateMultipartUploadRequest extends OSSRequest {
    private String bucketName;
    private ObjectMetadata metadata;
    private String objectKey;

    public InitiateMultipartUploadRequest(String bucketName, String objectKey) {
        this(bucketName, objectKey, null);
    }

    public InitiateMultipartUploadRequest(String bucketName, String objectKey, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setMetadata(metadata);
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

    public ObjectMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
