package com.xcharge.charger.protocol.monitor.type;

/* loaded from: classes.dex */
public enum MONITOR_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static MONITOR_REQUEST_STATE[] valuesCustom() {
        MONITOR_REQUEST_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        MONITOR_REQUEST_STATE[] monitor_request_stateArr = new MONITOR_REQUEST_STATE[length];
        System.arraycopy(valuesCustom, 0, monitor_request_stateArr, 0, length);
        return monitor_request_stateArr;
    }

    MONITOR_REQUEST_STATE(String status) {
        this.status = "";
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
