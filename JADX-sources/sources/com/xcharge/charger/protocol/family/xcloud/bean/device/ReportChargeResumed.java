package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportChargeResumed extends JsonBean<ReportChargeResumed> {
    private long billId = 0;
    private DeviceError cause = null;
    private long time = 0;

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
