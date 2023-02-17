package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class CreateBucketRequest extends OSSRequest {
    private CannedAccessControlList bucketACL;
    private String bucketName;
    private String locationConstraint;

    public CreateBucketRequest(String bucketName) {
        setBucketName(bucketName);
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public void setLocationConstraint(String locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    public String getLocationConstraint() {
        return this.locationConstraint;
    }

    public void setBucketACL(CannedAccessControlList bucketACL) {
        this.bucketACL = bucketACL;
    }

    public CannedAccessControlList getBucketACL() {
        return this.bucketACL;
    }
}
