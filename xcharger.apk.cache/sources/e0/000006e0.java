package com.xcharge.charger.protocol.family.xcloud.type;

/* loaded from: classes.dex */
public enum XCLOUD_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static XCLOUD_REQUEST_STATE[] valuesCustom() {
        XCLOUD_REQUEST_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        XCLOUD_REQUEST_STATE[] xcloud_request_stateArr = new XCLOUD_REQUEST_STATE[length];
        System.arraycopy(valuesCustom, 0, xcloud_request_stateArr, 0, length);
        return xcloud_request_stateArr;
    }

    XCLOUD_REQUEST_STATE(String status) {
        this.status = "";
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}