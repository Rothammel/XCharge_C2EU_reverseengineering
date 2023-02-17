package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class ConfirmChargeEnded extends JsonBean<ConfirmChargeEnded> {
    private Long billId = null;
    private boolean paid = false;
    private Long sid = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public Long getBillId() {
        return this.billId;
    }

    public void setBillId(Long billId2) {
        this.billId = billId2;
    }

    public boolean isPaid() {
        return this.paid;
    }

    public void setPaid(boolean paid2) {
        this.paid = paid2;
    }
}
