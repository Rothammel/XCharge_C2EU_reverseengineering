package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class ChargeDetail extends JsonBean<ChargeDetail> {
    private int delayInterval = 0;
    private long endTime = 0;
    private int feeDelay = 0;
    private int feePark = 0;
    private int feePower = 0;
    private int feeService = 0;
    private int feeTotal = 0;
    private double powerCharged = 0.0d;
    private long startTime = 0;

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime2) {
        this.startTime = startTime2;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime2) {
        this.endTime = endTime2;
    }

    public double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(double powerCharged2) {
        this.powerCharged = powerCharged2;
    }

    public int getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(int delayInterval2) {
        this.delayInterval = delayInterval2;
    }

    public int getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(int feeTotal2) {
        this.feeTotal = feeTotal2;
    }

    public int getFeePower() {
        return this.feePower;
    }

    public void setFeePower(int feePower2) {
        this.feePower = feePower2;
    }

    public int getFeeService() {
        return this.feeService;
    }

    public void setFeeService(int feeService2) {
        this.feeService = feeService2;
    }

    public int getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(int feeDelay2) {
        this.feeDelay = feeDelay2;
    }

    public int getFeePark() {
        return this.feePark;
    }

    public void setFeePark(int feePark2) {
        this.feePark = feePark2;
    }
}
