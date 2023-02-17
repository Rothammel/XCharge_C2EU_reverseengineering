package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

public enum PARK_STATUS {
    unknown("unknown"),
    idle(YZXProperty.CHARGE_STATUS_IDLE),
    occupied("occupied");
    
    private String status;

    private PARK_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
