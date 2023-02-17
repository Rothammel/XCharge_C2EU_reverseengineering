package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPDelayInfo extends JsonBean<CAPDelayInfo> {
    private String charge_id;
    private int delay_fee;
    private int park_fee;
    private int power_fee;
    private int service_fee;
    private int total_delay;
    private int total_fee;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public int getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(int total_delay) {
        this.total_delay = total_delay;
    }

    public int getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(int delay_fee) {
        this.delay_fee = delay_fee;
    }

    public int getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(int power_fee) {
        this.power_fee = power_fee;
    }

    public int getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(int service_fee) {
        this.service_fee = service_fee;
    }

    public int getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(int park_fee) {
        this.park_fee = park_fee;
    }

    public int getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(int total_fee) {
        this.total_fee = total_fee;
    }
}