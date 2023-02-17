package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class HeartbeatConf extends JsonBean<HeartbeatConf> {
    private String currentTime;

    public String getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(String currentTime2) {
        this.currentTime = currentTime2;
    }
}
