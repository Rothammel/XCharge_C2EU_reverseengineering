package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class AbortMultipartUploadRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;
    private String uploadId;

    public AbortMultipartUploadRequest(String bucketName, String objectKey, String uploadId) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadId(uploadId);
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

    public String getUploadId() {
        return this.uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
