package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class SetChargingProfileConf extends JsonBean<SetChargingProfileConf> {
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }
}
