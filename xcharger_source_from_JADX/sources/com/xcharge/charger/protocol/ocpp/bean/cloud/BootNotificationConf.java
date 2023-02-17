package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class BootNotificationConf extends JsonBean<BootNotificationConf> {
    private String currentTime;
    private int interval;
    private String status;

    public String getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(String currentTime2) {
        this.currentTime = currentTime2;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval2) {
        this.interval = interval2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }
}
