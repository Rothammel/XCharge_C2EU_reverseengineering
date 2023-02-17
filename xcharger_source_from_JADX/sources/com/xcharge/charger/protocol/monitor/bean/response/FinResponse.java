package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

public class FinResponse extends JsonBean<FinResponse> {
    private Integer delay_fee = null;
    private Integer paid_fee = null;
    private Integer park_fee = null;
    private Integer power_fee = null;
    private Integer service_fee = null;
    private Integer total_fee = null;

    public Integer getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(Integer total_fee2) {
        this.total_fee = total_fee2;
    }

    public Integer getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(Integer power_fee2) {
        this.power_fee = power_fee2;
    }

    public Integer getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(Integer service_fee2) {
        this.service_fee = service_fee2;
    }

    public Integer getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(Integer delay_fee2) {
        this.delay_fee = delay_fee2;
    }

    public Integer getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(Integer park_fee2) {
        this.park_fee = park_fee2;
    }

    public Integer getPaid_fee() {
        return this.paid_fee;
    }

    public void setPaid_fee(Integer paid_fee2) {
        this.paid_fee = paid_fee2;
    }
}
