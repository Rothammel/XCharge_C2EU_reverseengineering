package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestAutoStop extends JsonBean<RequestAutoStop> {
    private Long sid = null;
    private long billId = 0;
    private ChargeStopCondition autoStopAt = null;
    private long time = 0;

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

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt) {
        this.autoStopAt = autoStopAt;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}