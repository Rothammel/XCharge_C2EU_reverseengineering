package com.xcharge.charger.protocol.ocpp.type;

public enum OCPP_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    private OCPP_REQUEST_STATE(String status2) {
        this.status = "";
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
