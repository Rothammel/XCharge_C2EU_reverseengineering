package com.xcharge.charger.data.bean.type;

public enum CHARGE_PLATFORM {
    xcharge("xcharge"),
    xconsole("xconsole"),
    anyo("anyo"),
    xmsz("xmsz"),
    ptne("ptne"),
    ecw("ecw"),
    yzx("yzx"),
    cddz("cddz"),
    ocpp("ocpp");
    
    private String platform;

    private CHARGE_PLATFORM(String platform2) {
        this.platform = null;
        this.platform = platform2;
    }

    public String getPlatform() {
        return this.platform;
    }
}
