package com.xcharge.charger.protocol.family.xcloud.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

public enum EnumChargeBillStatus {
    created("created"),
    cancelled("cancelled"),
    charging(YZXProperty.CHARGE_STATUS_CHARGING),
    paused("paused"),
    stopped(YZXProperty.CHARGE_STATUS_STOPPED),
    delayBilling("delayBilling"),
    ended("ended"),
    paid("paid"),
    unfinished("unfinished");
    
    private String status;

    private EnumChargeBillStatus(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
