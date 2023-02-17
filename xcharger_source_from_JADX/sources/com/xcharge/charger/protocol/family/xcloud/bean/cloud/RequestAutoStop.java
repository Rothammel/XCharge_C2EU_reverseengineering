package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

public class RequestAutoStop extends JsonBean<RequestAutoStop> {
    private ChargeStopCondition autoStopAt = null;
    private long billId = 0;
    private Long sid = null;
    private long time = 0;

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

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt2) {
        this.autoStopAt = autoStopAt2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
