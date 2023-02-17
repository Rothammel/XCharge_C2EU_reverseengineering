package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportDelayCountStarted extends JsonBean<ReportDelayCountStarted> {
    private long billId;
    private long time;

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}