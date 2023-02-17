package com.xcharge.charger.protocol.ocpp.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NowLimitContext extends JsonBean<NowLimitContext> {
    private double limit;
    private Long nextTime = null;

    public double getLimit() {
        return this.limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public Long getNextTime() {
        return this.nextTime;
    }

    public void setNextTime(Long nextTime) {
        this.nextTime = nextTime;
    }
}
