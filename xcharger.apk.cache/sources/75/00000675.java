package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargeStopCondition extends JsonBean<ChargeStopCondition> {
    private Integer interval = null;
    private Integer powerPercent = null;
    private Integer power = null;
    private Integer fee = null;

    public Integer getInterval() {
        return this.interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getPowerPercent() {
        return this.powerPercent;
    }

    public void setPowerPercent(Integer powerPercent) {
        this.powerPercent = powerPercent;
    }

    public Integer getPower() {
        return this.power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public Integer getFee() {
        return this.fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }
}