package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

public class ChargeInfo extends JsonBean<ChargeInfo> {
    private Double amp = null;
    private Integer kwatt = null;
    private Integer park_fee = null;
    private Double power = null;
    private Integer power_fee = null;
    private Integer service_fee = null;
    private Double volt = null;

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power2) {
        this.power = power2;
    }

    public Integer getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Integer kwatt2) {
        this.kwatt = kwatt2;
    }

    public Double getVolt() {
        return this.volt;
    }

    public void setVolt(Double volt2) {
        this.volt = volt2;
    }

    public Double getAmp() {
        return this.amp;
    }

    public void setAmp(Double amp2) {
        this.amp = amp2;
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

    public Integer getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(Integer park_fee2) {
        this.park_fee = park_fee2;
    }
}
