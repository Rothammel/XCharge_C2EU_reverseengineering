package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

public class DAPStopCondition extends JsonBean<DAPStopCondition> {
    private String charge_id = null;
    private ChargeStopCondition stop_condition = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public ChargeStopCondition getStop_condition() {
        return this.stop_condition;
    }

    public void setStop_condition(ChargeStopCondition stop_condition2) {
        this.stop_condition = stop_condition2;
    }
}
