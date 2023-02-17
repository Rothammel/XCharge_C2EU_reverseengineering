package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPStopCondition extends JsonBean<DAPStopCondition> {
    private String charge_id = null;
    private ChargeStopCondition stop_condition = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public ChargeStopCondition getStop_condition() {
        return this.stop_condition;
    }

    public void setStop_condition(ChargeStopCondition stop_condition) {
        this.stop_condition = stop_condition;
    }
}