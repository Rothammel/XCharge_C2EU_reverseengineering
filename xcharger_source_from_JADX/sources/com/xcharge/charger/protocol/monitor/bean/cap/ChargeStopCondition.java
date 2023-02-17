package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

public class ChargeStopCondition extends JsonBean<ChargeStopCondition> {
    public static final String TYPE_AUTO = "auto";
    public static final String TYPE_FEE = "fee";
    public static final String TYPE_POWER = "power";
    public static final String TYPE_TIME = "time";
    private String type = TYPE_AUTO;
    private String value = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }
}
