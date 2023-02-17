package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportDelayCountStarted extends JsonBean<ReportDelayCountStarted> {
    private long billId;
    private long time;

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId2) {
        this.billId = billId2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
