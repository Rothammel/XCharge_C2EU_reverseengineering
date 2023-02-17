package com.xcharge.charger.data.bean.type;

public enum CHARGE_STATUS {
    IDLE("IDLE"),
    CHARGE_START_WAITTING("CHARGE_START_WAITTING"),
    CHARGING("CHARGING"),
    CHARGE_STOP_WAITTING("CHARGE_STOP_WAITTING");
    
    private String status;

    private CHARGE_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
