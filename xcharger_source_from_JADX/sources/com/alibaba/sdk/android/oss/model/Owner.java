package com.alibaba.sdk.android.oss.model;

import java.io.Serializable;

public class Owner implements Serializable {
    private static final long serialVersionUID = -1942759024112448066L;
    private String displayName;

    /* renamed from: id */
    private String f8id;

    public Owner() {
        this((String) null, (String) null);
    }

    public Owner(String id, String displayName2) {
        this.f8id = id;
        this.displayName = displayName2;
    }

    public String toString() {
        return "Owner [name=" + getDisplayName() + ",id=" + getId() + "]";
    }

    public String getId() {
        return this.f8id;
    }

    public void setId(String id) {
        this.f8id = id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Owner)) {
            return false;
        }
        Owner otherOwner = (Owner) obj;
        String otherOwnerId = otherOwner.getId();
        String otherOwnerName = otherOwner.getDisplayName();
        String thisOwnerId = getId();
        String thisOwnerName = getDisplayName();
        if (otherOwnerId == null) {
            otherOwnerId = "";
        }
        if (otherOwnerName == null) {
            otherOwnerName = "";
        }
        if (thisOwnerId == null) {
            thisOwnerId = "";
        }
        if (thisOwnerName == null) {
            thisOwnerName = "";
        }
        if (!otherOwnerId.equals(thisOwnerId) || !otherOwnerName.equals(thisOwnerName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.f8id != null) {
            return this.f8id.hashCode();
        }
        return 0;
    }
}
