package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class InitResponse extends JsonBean<InitResponse> {
    private String charge_id = null;
    private Long init_time = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public Long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(Long init_time) {
        this.init_time = init_time;
    }
}