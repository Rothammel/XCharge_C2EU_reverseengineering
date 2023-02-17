package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class FinResponse extends JsonBean<FinResponse> {
    private Integer total_fee = null;
    private Integer power_fee = null;
    private Integer service_fee = null;
    private Integer delay_fee = null;
    private Integer park_fee = null;
    private Integer paid_fee = null;

    public Integer getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(Integer total_fee) {
        this.total_fee = total_fee;
    }

    public Integer getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(Integer power_fee) {
        this.power_fee = power_fee;
    }

    public Integer getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(Integer service_fee) {
        this.service_fee = service_fee;
    }

    public Integer getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(Integer delay_fee) {
        this.delay_fee = delay_fee;
    }

    public Integer getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(Integer park_fee) {
        this.park_fee = park_fee;
    }

    public Integer getPaid_fee() {
        return this.paid_fee;
    }

    public void setPaid_fee(Integer paid_fee) {
        this.paid_fee = paid_fee;
    }
}