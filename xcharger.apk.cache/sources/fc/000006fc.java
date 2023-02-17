package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPChargeInfo extends JsonBean<CAPChargeInfo> {
    private String charge_id;
    private ChargeInfo charge_info;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public ChargeInfo getCharge_info() {
        return this.charge_info;
    }

    public void setCharge_info(ChargeInfo charge_info) {
        this.charge_info = charge_info;
    }
}