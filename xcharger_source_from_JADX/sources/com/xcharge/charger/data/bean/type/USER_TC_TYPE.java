package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;

public enum USER_TC_TYPE {
    auto(ChargeStopCondition.TYPE_AUTO),
    fee(ChargeStopCondition.TYPE_FEE),
    power(ChargeStopCondition.TYPE_POWER),
    time(ChargeStopCondition.TYPE_TIME),
    toc("toc");
    
    private String type;

    private USER_TC_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
