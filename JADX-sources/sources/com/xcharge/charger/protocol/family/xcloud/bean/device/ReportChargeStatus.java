package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportChargeStatus extends JsonBean<ReportChargeStatus> {
    private long billId = 0;
    private Object[] data = null;
    private long time = 0;

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public Object[] getData() {
        return this.data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
