package com.xcharge.charger.protocol.ocpp.type;

/* loaded from: classes.dex */
public enum OCPP_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static OCPP_REQUEST_STATE[] valuesCustom() {
        OCPP_REQUEST_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        OCPP_REQUEST_STATE[] ocpp_request_stateArr = new OCPP_REQUEST_STATE[length];
        System.arraycopy(valuesCustom, 0, ocpp_request_stateArr, 0, length);
        return ocpp_request_stateArr;
    }

    OCPP_REQUEST_STATE(String status) {
        this.status = "";
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
