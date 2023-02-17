package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargeDetail extends JsonBean<ChargeDetail> {
    private long startTime = 0;
    private long endTime = 0;
    private double powerCharged = 0.0d;
    private int delayInterval = 0;
    private int feeTotal = 0;
    private int feePower = 0;
    private int feeService = 0;
    private int feeDelay = 0;
    private int feePark = 0;

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(double powerCharged) {
        this.powerCharged = powerCharged;
    }

    public int getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(int delayInterval) {
        this.delayInterval = delayInterval;
    }

    public int getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(int feeTotal) {
        this.feeTotal = feeTotal;
    }

    public int getFeePower() {
        return this.feePower;
    }

    public void setFeePower(int feePower) {
        this.feePower = feePower;
    }

    public int getFeeService() {
        return this.feeService;
    }

    public void setFeeService(int feeService) {
        this.feeService = feeService;
    }

    public int getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(int feeDelay) {
        this.feeDelay = feeDelay;
    }

    public int getFeePark() {
        return this.feePark;
    }

    public void setFeePark(int feePark) {
        this.feePark = feePark;
    }
}
