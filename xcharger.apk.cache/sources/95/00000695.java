package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestStopCharge extends JsonBean<RequestStopCharge> {
    private Long sid = null;
    private long billId = 0;
    private DeviceError cause = null;
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

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause) {
        this.cause = cause;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}