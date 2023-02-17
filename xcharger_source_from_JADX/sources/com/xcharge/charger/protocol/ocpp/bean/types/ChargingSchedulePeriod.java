package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

public class ChargingSchedulePeriod extends JsonBean<ChargingSchedulePeriod> {
    private double limit;
    private int numberPhases = 3;
    private int startPeriod;

    public int getStartPeriod() {
        return this.startPeriod;
    }

    public void setStartPeriod(int startPeriod2) {
        this.startPeriod = startPeriod2;
    }

    public double getLimit() {
        return this.limit;
    }

    public void setLimit(double limit2) {
        this.limit = limit2;
    }

    public int getNumberPhases() {
        return this.numberPhases;
    }

    public void setNumberPhases(int numberPhases2) {
        this.numberPhases = numberPhases2;
    }
}
