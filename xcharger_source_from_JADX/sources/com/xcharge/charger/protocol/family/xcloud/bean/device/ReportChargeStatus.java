package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportChargeStatus extends JsonBean<ReportChargeStatus> {
    private long billId = 0;
    private Object[] data = null;
    private long time = 0;

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId2) {
        this.billId = billId2;
    }

    public Object[] getData() {
        return this.data;
    }

    public void setData(Object[] data2) {
        this.data = data2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
