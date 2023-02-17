package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargeInfo extends JsonBean<ChargeInfo> {
    private Double power = null;
    private Integer kwatt = null;
    private Double volt = null;
    private Double amp = null;
    private Integer power_fee = null;
    private Integer service_fee = null;
    private Integer park_fee = null;

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Integer getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Integer kwatt) {
        this.kwatt = kwatt;
    }

    public Double getVolt() {
        return this.volt;
    }

    public void setVolt(Double volt) {
        this.volt = volt;
    }

    public Double getAmp() {
        return this.amp;
    }

    public void setAmp(Double amp) {
        this.amp = amp;
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

    public Integer getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(Integer park_fee) {
        this.park_fee = park_fee;
    }
}