package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPBillInfo extends JsonBean<CAPBillInfo> {
    private String charge_id = null;
    private BillInfo bill_info = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public BillInfo getBill_info() {
        return this.bill_info;
    }

    public void setBill_info(BillInfo bill_info) {
        this.bill_info = bill_info;
    }
}
