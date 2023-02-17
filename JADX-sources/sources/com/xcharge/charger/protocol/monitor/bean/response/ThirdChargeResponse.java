package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ThirdChargeResponse extends JsonBean<ThirdChargeResponse> {
    private String charge_id;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }
}
