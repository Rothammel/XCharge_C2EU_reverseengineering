package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.cap.BillInfo;
import com.xcharge.common.bean.JsonBean;

public class FinRequest extends JsonBean<FinRequest> {
    private BillInfo bill_info;
    private String charge_id;

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
