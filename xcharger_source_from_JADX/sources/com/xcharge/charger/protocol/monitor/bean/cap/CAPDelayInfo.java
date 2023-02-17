package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

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

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public int getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(int total_delay2) {
        this.total_delay = total_delay2;
    }

    public int getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(int delay_fee2) {
        this.delay_fee = delay_fee2;
    }

    public int getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(int power_fee2) {
        this.power_fee = power_fee2;
    }

    public int getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(int service_fee2) {
        this.service_fee = service_fee2;
    }

    public int getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(int park_fee2) {
        this.park_fee = park_fee2;
    }

    public int getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(int total_fee2) {
        this.total_fee = total_fee2;
    }
}
