package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class GetBucketACLResult extends OSSResult {
    private CannedAccessControlList bucketACL;
    private Owner bucketOwner = new Owner();

    public Owner getOwner() {
        return this.bucketOwner;
    }

    public void setBucketOwner(String ownerName) {
        this.bucketOwner.setDisplayName(ownerName);
    }

    public String getBucketOwner() {
        return this.bucketOwner.getDisplayName();
    }

    public void setBucketOwnerID(String id) {
        this.bucketOwner.setId(id);
    }

    public String getBucketOwnerID() {
        return this.bucketOwner.getId();
    }

    public void setBucketACL(String bucketACL) {
        this.bucketACL = CannedAccessControlList.parseACL(bucketACL);
    }

    public String getBucketACL() {
        if (this.bucketACL == null) {
            return null;
        }
        String acl = this.bucketACL.toString();
        return acl;
    }
}
