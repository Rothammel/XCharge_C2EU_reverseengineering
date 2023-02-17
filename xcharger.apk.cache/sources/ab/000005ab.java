package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum CHARGE_STOP_CAUSE {
    undefined("undefined"),
    user("user"),
    local_user("local_user"),
    remote_user("remote_user"),
    system_user("system_user"),
    plugout("plugout"),
    car("car"),
    full("full"),
    no_balance("no_balance"),
    user_set("user_set"),
    reboot("reboot"),
    fault("fault");
    
    private String cause;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_STOP_CAUSE[] valuesCustom() {
        CHARGE_STOP_CAUSE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_STOP_CAUSE[] charge_stop_causeArr = new CHARGE_STOP_CAUSE[length];
        System.arraycopy(valuesCustom, 0, charge_stop_causeArr, 0, length);
        return charge_stop_causeArr;
    }

    CHARGE_STOP_CAUSE(String cause) {
        this.cause = null;
        this.cause = cause;
    }

    public String getCause() {
        return this.cause;
    }
}