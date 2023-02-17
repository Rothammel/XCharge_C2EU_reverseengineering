package com.xcharge.charger.protocol.anyo.type;

public enum ANYO_PORT_STATE {
    not_login("not_login"),
    logined("logined");
    
    private String status;

    private ANYO_PORT_STATE(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
