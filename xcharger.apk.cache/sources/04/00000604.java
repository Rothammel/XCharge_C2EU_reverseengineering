package com.xcharge.charger.device.c2.nfc.charge.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
public enum NFC_CHARGE_STATUS {
    idle(YZXProperty.CHARGE_STATUS_IDLE),
    auth_sended("auth_sended"),
    init_sended("init_sended"),
    inited("inited"),
    charging(YZXProperty.CHARGE_STATUS_CHARGING),
    stop_sended("stop_sended"),
    stopped(YZXProperty.CHARGE_STATUS_STOPPED),
    fin_sended("fin_sended");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static NFC_CHARGE_STATUS[] valuesCustom() {
        NFC_CHARGE_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        NFC_CHARGE_STATUS[] nfc_charge_statusArr = new NFC_CHARGE_STATUS[length];
        System.arraycopy(valuesCustom, 0, nfc_charge_statusArr, 0, length);
        return nfc_charge_statusArr;
    }

    NFC_CHARGE_STATUS(String status) {
        this.status = YZXProperty.CHARGE_STATUS_IDLE;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}