package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum CHARGE_USER_STATUS {
    normal("normal"),
    need_pay("need_pay"),
    need_queue("need_queue"),
    need_rsrv("need_rsrv"),
    illegal("illegal");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_USER_STATUS[] valuesCustom() {
        CHARGE_USER_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_USER_STATUS[] charge_user_statusArr = new CHARGE_USER_STATUS[length];
        System.arraycopy(valuesCustom, 0, charge_user_statusArr, 0, length);
        return charge_user_statusArr;
    }

    CHARGE_USER_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
