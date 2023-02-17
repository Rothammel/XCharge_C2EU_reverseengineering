package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class UpdateFirmwareReq extends JsonBean<UpdateFirmwareReq> {
    private String location = null;
    private Integer retries = null;
    private String retrieveDate = null;
    private Integer retryInterval = null;

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getRetries() {
        return this.retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getRetrieveDate() {
        return this.retrieveDate;
    }

    public void setRetrieveDate(String retrieveDate) {
        this.retrieveDate = retrieveDate;
    }

    public Integer getRetryInterval() {
        return this.retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }
}
