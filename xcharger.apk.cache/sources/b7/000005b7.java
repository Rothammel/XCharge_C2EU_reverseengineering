package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
public enum PARK_STATUS {
    unknown("unknown"),
    idle(YZXProperty.CHARGE_STATUS_IDLE),
    occupied("occupied");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static PARK_STATUS[] valuesCustom() {
        PARK_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        PARK_STATUS[] park_statusArr = new PARK_STATUS[length];
        System.arraycopy(valuesCustom, 0, park_statusArr, 0, length);
        return park_statusArr;
    }

    PARK_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}