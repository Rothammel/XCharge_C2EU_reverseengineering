package com.xcharge.charger.protocol.anyo.type;

/* loaded from: classes.dex */
public enum ANYO_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static ANYO_REQUEST_STATE[] valuesCustom() {
        ANYO_REQUEST_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        ANYO_REQUEST_STATE[] anyo_request_stateArr = new ANYO_REQUEST_STATE[length];
        System.arraycopy(valuesCustom, 0, anyo_request_stateArr, 0, length);
        return anyo_request_stateArr;
    }

    ANYO_REQUEST_STATE(String status) {
        this.status = "";
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}