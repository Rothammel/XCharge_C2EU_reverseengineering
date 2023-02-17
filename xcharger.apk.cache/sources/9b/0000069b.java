package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportAutoStopResult extends JsonBean<ReportAutoStopResult> {
    private Long sid = null;
    private long billId = 0;
    private long time = 0;
    private DeviceError error = null;

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

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error) {
        this.error = error;
    }
}