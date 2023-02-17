package com.xcharge.charger.data.bean.type;

public enum CHARGE_USER_STATUS {
    normal("normal"),
    need_pay("need_pay"),
    need_queue("need_queue"),
    need_rsrv("need_rsrv"),
    illegal("illegal");
    
    private String status;

    private CHARGE_USER_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
