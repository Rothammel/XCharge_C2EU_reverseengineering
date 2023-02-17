package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public enum CannedAccessControlList {
    Private("private"),
    PublicRead("public-read"),
    PublicReadWrite("public-read-write");
    
    private String ACLString;

    CannedAccessControlList(String acl) {
        this.ACLString = acl;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.ACLString;
    }

    public static CannedAccessControlList parseACL(String aclStr) {
        CannedAccessControlList[] values;
        for (CannedAccessControlList acl : values()) {
            if (acl.toString().equals(aclStr)) {
                return acl;
            }
        }
        return null;
    }
}