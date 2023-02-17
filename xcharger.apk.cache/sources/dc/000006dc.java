package com.xcharge.charger.protocol.family.xcloud.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
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

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static EnumChargeBillStatus[] valuesCustom() {
        EnumChargeBillStatus[] valuesCustom = values();
        int length = valuesCustom.length;
        EnumChargeBillStatus[] enumChargeBillStatusArr = new EnumChargeBillStatus[length];
        System.arraycopy(valuesCustom, 0, enumChargeBillStatusArr, 0, length);
        return enumChargeBillStatusArr;
    }

    EnumChargeBillStatus(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}