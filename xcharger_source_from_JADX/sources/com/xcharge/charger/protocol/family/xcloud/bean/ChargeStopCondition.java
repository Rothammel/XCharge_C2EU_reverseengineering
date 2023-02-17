package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class ChargeStopCondition extends JsonBean<ChargeStopCondition> {
    private Integer fee = null;
    private Integer interval = null;
    private Integer power = null;
    private Integer powerPercent = null;

    public Integer getInterval() {
        return this.interval;
    }

    public void setInterval(Integer interval2) {
        this.interval = interval2;
    }

    public Integer getPowerPercent() {
        return this.powerPercent;
    }

    public void setPowerPercent(Integer powerPercent2) {
        this.powerPercent = powerPercent2;
    }

    public Integer getPower() {
        return this.power;
    }

    public void setPower(Integer power2) {
        this.power = power2;
    }

    public Integer getFee() {
        return this.fee;
    }

    public void setFee(Integer fee2) {
        this.fee = fee2;
    }
}
