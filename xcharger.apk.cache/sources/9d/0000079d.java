package com.xcharge.charger.protocol.ocpp.bean.types;

/* loaded from: classes.dex */
public class IdTagInfo {
    private String expiryDate;
    private String parentIdTag;
    private String status;

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag) {
        this.parentIdTag = parentIdTag;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}