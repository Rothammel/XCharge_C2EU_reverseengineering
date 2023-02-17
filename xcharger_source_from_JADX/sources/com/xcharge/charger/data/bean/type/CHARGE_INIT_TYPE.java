package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

public enum CHARGE_INIT_TYPE {
    wx_qrcode("wx_qrcode"),
    native_qrcode("native_qrcode"),
    nfc(InitRequest.UTYPE_NFC),
    device_id("device_id"),
    cloud("cloud");
    
    private String type;

    private CHARGE_INIT_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
