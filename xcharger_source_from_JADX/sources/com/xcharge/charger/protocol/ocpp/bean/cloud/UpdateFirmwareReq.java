package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class UpdateFirmwareReq extends JsonBean<UpdateFirmwareReq> {
    private String location = null;
    private Integer retries = null;
    private String retrieveDate = null;
    private Integer retryInterval = null;

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location2) {
        this.location = location2;
    }

    public Integer getRetries() {
        return this.retries;
    }

    public void setRetries(Integer retries2) {
        this.retries = retries2;
    }

    public String getRetrieveDate() {
        return this.retrieveDate;
    }

    public void setRetrieveDate(String retrieveDate2) {
        this.retrieveDate = retrieveDate2;
    }

    public Integer getRetryInterval() {
        return this.retryInterval;
    }

    public void setRetryInterval(Integer retryInterval2) {
        this.retryInterval = retryInterval2;
    }
}
