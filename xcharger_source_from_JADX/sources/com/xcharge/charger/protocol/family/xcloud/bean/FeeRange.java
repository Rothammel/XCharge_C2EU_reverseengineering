package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class FeeRange extends JsonBean<FeeRange> {
    private int delay = 0;
    private int park = 0;
    private int power = 0;
    private int service = 0;
    private int total = 0;

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total2) {
        this.total = total2;
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power2) {
        this.power = power2;
    }

    public int getService() {
        return this.service;
    }

    public void setService(int service2) {
        this.service = service2;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(int delay2) {
        this.delay = delay2;
    }

    public int getPark() {
        return this.park;
    }

    public void setPark(int park2) {
        this.park = park2;
    }
}
