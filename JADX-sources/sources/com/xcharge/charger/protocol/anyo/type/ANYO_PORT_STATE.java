package com.xcharge.charger.protocol.anyo.type;

/* loaded from: classes.dex */
public enum ANYO_PORT_STATE {
    not_login("not_login"),
    logined("logined");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static ANYO_PORT_STATE[] valuesCustom() {
        ANYO_PORT_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        ANYO_PORT_STATE[] anyo_port_stateArr = new ANYO_PORT_STATE[length];
        System.arraycopy(valuesCustom, 0, anyo_port_stateArr, 0, length);
        return anyo_port_stateArr;
    }

    ANYO_PORT_STATE(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
