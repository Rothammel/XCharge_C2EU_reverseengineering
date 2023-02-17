package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

public class InitResponse extends JsonBean<InitResponse> {
    private String charge_id = null;
    private Long init_time = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public Long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(Long init_time2) {
        this.init_time = init_time2;
    }
}
