package com.xcharge.charger.protocol.xmsz.type;

/* loaded from: classes.dex */
public enum XMSZ_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static XMSZ_REQUEST_STATE[] valuesCustom() {
        XMSZ_REQUEST_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        XMSZ_REQUEST_STATE[] xmsz_request_stateArr = new XMSZ_REQUEST_STATE[length];
        System.arraycopy(valuesCustom, 0, xmsz_request_stateArr, 0, length);
        return xmsz_request_stateArr;
    }

    XMSZ_REQUEST_STATE(String status) {
        this.status = "";
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}