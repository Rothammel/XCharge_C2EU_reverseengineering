package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

public class CAPChargeInfo extends JsonBean<CAPChargeInfo> {
    private String charge_id;
    private ChargeInfo charge_info;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public ChargeInfo getCharge_info() {
        return this.charge_info;
    }

    public void setCharge_info(ChargeInfo charge_info2) {
        this.charge_info = charge_info2;
    }
}
