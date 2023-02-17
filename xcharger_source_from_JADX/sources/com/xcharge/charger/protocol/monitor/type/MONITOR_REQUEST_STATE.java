package com.xcharge.charger.protocol.monitor.type;

public enum MONITOR_REQUEST_STATE {
    sending("sending"),
    sended("sended"),
    responsed("responsed");
    
    private String status;

    private MONITOR_REQUEST_STATE(String status2) {
        this.status = "";
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
