package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

/* loaded from: classes.dex */
public enum CHARGE_INIT_TYPE {
    wx_qrcode("wx_qrcode"),
    native_qrcode("native_qrcode"),
    nfc(InitRequest.UTYPE_NFC),
    device_id("device_id"),
    cloud("cloud");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_INIT_TYPE[] valuesCustom() {
        CHARGE_INIT_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_INIT_TYPE[] charge_init_typeArr = new CHARGE_INIT_TYPE[length];
        System.arraycopy(valuesCustom, 0, charge_init_typeArr, 0, length);
        return charge_init_typeArr;
    }

    CHARGE_INIT_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}