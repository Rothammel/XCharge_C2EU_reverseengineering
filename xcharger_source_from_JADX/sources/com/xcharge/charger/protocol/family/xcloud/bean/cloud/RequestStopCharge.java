package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

public class RequestStopCharge extends JsonBean<RequestStopCharge> {
    private long billId = 0;
    private DeviceError cause = null;
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

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause2) {
        this.cause = cause2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
