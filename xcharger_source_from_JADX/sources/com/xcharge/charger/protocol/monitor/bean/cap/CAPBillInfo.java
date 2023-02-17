package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

public class CAPBillInfo extends JsonBean<CAPBillInfo> {
    private BillInfo bill_info = null;
    private String charge_id = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public BillInfo getBill_info() {
        return this.bill_info;
    }

    public void setBill_info(BillInfo bill_info2) {
        this.bill_info = bill_info2;
    }
}
