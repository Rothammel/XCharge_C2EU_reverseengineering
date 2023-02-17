package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.XCloudChargeBill;

public class ReportChargeEnded extends XCloudChargeBill {
    private long billId = 0;
    private Long sid = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId2) {
        this.billId = billId2;
    }
}
