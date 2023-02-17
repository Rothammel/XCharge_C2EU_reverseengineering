package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CancelAutoStop extends JsonBean<CancelAutoStop> {
    private Long sid = null;
    private Long billId = null;
    private ChargeStopCondition autoStopAt = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public Long getBillId() {
        return this.billId;
    }

    public void setBillId(Long billId) {
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
