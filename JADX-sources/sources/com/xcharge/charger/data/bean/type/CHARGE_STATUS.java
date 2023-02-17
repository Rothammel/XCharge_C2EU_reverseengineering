package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum CHARGE_STATUS {
    IDLE("IDLE"),
    CHARGE_START_WAITTING("CHARGE_START_WAITTING"),
    CHARGING("CHARGING"),
    CHARGE_STOP_WAITTING("CHARGE_STOP_WAITTING");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_STATUS[] valuesCustom() {
        CHARGE_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_STATUS[] charge_statusArr = new CHARGE_STATUS[length];
        System.arraycopy(valuesCustom, 0, charge_statusArr, 0, length);
        return charge_statusArr;
    }

    CHARGE_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
