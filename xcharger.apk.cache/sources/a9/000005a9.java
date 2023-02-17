package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
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

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_PLATFORM[] valuesCustom() {
        CHARGE_PLATFORM[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_PLATFORM[] charge_platformArr = new CHARGE_PLATFORM[length];
        System.arraycopy(valuesCustom, 0, charge_platformArr, 0, length);
        return charge_platformArr;
    }

    CHARGE_PLATFORM(String platform) {
        this.platform = null;
        this.platform = platform;
    }

    public String getPlatform() {
        return this.platform;
    }
}