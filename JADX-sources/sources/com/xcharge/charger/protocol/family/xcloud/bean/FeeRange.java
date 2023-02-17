package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class FeeRange extends JsonBean<FeeRange> {
    private int total = 0;
    private int power = 0;
    private int service = 0;
    private int delay = 0;
    private int park = 0;

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getService() {
        return this.service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getPark() {
        return this.park;
    }

    public void setPark(int park) {
        this.park = park;
    }
}
