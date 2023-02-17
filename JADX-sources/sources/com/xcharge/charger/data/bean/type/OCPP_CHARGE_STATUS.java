package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum OCPP_CHARGE_STATUS {
    SESSION_STARTED("SESSION_STARTED"),
    TRANSACTION_STARTED("TRANSACTION_STARTED"),
    ENERGY_OFFER("ENERGY_OFFER"),
    ENERGY_TRANSFER("ENERGY_TRANSFER"),
    ENERGY_OFFER_SUSPEND("ENERGY_OFFER_SUSPEND"),
    TRANSACTION_ENDED("TRANSACTION_ENDED"),
    SESSION_ENDED("\tSESSION_ENDED");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static OCPP_CHARGE_STATUS[] valuesCustom() {
        OCPP_CHARGE_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        OCPP_CHARGE_STATUS[] ocpp_charge_statusArr = new OCPP_CHARGE_STATUS[length];
        System.arraycopy(valuesCustom, 0, ocpp_charge_statusArr, 0, length);
        return ocpp_charge_statusArr;
    }

    OCPP_CHARGE_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
