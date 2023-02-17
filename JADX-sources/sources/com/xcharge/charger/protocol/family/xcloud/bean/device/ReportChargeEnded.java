package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.XCloudChargeBill;

/* loaded from: classes.dex */
public class ReportChargeEnded extends XCloudChargeBill {
    private Long sid = null;
    private long billId = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }
}
