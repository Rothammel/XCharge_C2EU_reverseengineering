package com.alibaba.sdk.android.oss.model;

public class CreateBucketRequest extends OSSRequest {
    private CannedAccessControlList bucketACL;
    private String bucketName;
    private String locationConstraint;

    public CreateBucketRequest(String bucketName2) {
        setBucketName(bucketName2);
    }

    public void setBucketName(String bucketName2) {
        this.bucketName = bucketName2;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public void setLocationConstraint(String locationConstraint2) {
        this.locationConstraint = locationConstraint2;
    }

    public String getLocationConstraint() {
        return this.locationConstraint;
    }

    public void setBucketACL(CannedAccessControlList bucketACL2) {
        this.bucketACL = bucketACL2;
    }

    public CannedAccessControlList getBucketACL() {
        return this.bucketACL;
    }
}
