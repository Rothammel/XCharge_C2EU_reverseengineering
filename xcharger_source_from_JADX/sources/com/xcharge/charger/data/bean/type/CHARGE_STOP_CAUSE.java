package com.xcharge.charger.data.bean.type;

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

    private CHARGE_STOP_CAUSE(String cause2) {
        this.cause = null;
        this.cause = cause2;
    }

    public String getCause() {
        return this.cause;
    }
}
