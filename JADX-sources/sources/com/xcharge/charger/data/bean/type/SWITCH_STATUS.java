package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum SWITCH_STATUS {
    disable("disable"),
    on("on"),
    off("off"),
    fault("fault");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static SWITCH_STATUS[] valuesCustom() {
        SWITCH_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        SWITCH_STATUS[] switch_statusArr = new SWITCH_STATUS[length];
        System.arraycopy(valuesCustom, 0, switch_statusArr, 0, length);
        return switch_statusArr;
    }

    SWITCH_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
