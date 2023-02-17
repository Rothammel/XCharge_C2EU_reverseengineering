package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class GetDiagnosticsReq extends JsonBean<GetDiagnosticsReq> {
    private String location = null;
    private Integer retries = null;
    private Integer retryInterval = null;
    private String startTime = null;
    private String stopTime = null;

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

    public Integer getRetryInterval() {
        return this.retryInterval;
    }

    public void setRetryInterval(Integer retryInterval2) {
        this.retryInterval = retryInterval2;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime2) {
        this.startTime = startTime2;
    }

    public String getStopTime() {
        return this.stopTime;
    }

    public void setStopTime(String stopTime2) {
        this.stopTime = stopTime2;
    }
}
