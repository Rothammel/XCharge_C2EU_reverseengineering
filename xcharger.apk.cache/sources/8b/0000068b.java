package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ConfirmChargeStarted extends JsonBean<ConfirmChargeStarted> {
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