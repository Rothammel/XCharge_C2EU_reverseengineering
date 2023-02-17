package com.xcharge.charger.protocol.ocpp.bean;

import com.xcharge.common.bean.JsonBean;

public class AuthCache extends JsonBean<AuthCache> {
    private String expiryDate;
    private String idTag;
    private String parentIdTag;
    private String status;

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate2) {
        this.expiryDate = expiryDate2;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag2) {
        this.parentIdTag = parentIdTag2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }
}
