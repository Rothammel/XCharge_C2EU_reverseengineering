package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargingSchedulePeriod extends JsonBean<ChargingSchedulePeriod> {
    private double limit;
    private int numberPhases = 3;
    private int startPeriod;

    public int getStartPeriod() {
        return this.startPeriod;
    }

    public void setStartPeriod(int startPeriod) {
        this.startPeriod = startPeriod;
    }

    public double getLimit() {
        return this.limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public int getNumberPhases() {
        return this.numberPhases;
    }

    public void setNumberPhases(int numberPhases) {
        this.numberPhases = numberPhases;
    }
}