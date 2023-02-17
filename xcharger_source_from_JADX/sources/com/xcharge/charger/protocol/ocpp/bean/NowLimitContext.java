package com.xcharge.charger.protocol.ocpp.bean;

import com.xcharge.common.bean.JsonBean;

public class NowLimitContext extends JsonBean<NowLimitContext> {
    private double limit;
    private Long nextTime = null;

    public double getLimit() {
        return this.limit;
    }

    public void setLimit(double limit2) {
        this.limit = limit2;
    }

    public Long getNextTime() {
        return this.nextTime;
    }

    public void setNextTime(Long nextTime2) {
        this.nextTime = nextTime2;
    }
}
