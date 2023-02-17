package com.alibaba.sdk.android.oss.model;

import java.io.Serializable;

/* loaded from: classes.dex */
public class Owner implements Serializable {
    private static final long serialVersionUID = -1942759024112448066L;
    private String displayName;
    private String id;

    public Owner() {
        this(null, null);
    }

    public Owner(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String toString() {
        return "Owner [name=" + getDisplayName() + ",id=" + getId() + "]";
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Owner) {
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
            return otherOwnerId.equals(thisOwnerId) && otherOwnerName.equals(thisOwnerName);
        }
        return false;
    }

    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        return 0;
    }
}