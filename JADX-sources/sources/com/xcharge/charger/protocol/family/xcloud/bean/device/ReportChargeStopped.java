package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportChargeStopped extends JsonBean<ReportChargeStopped> {
    private Long sid = null;
    private long billId = 0;
    private Object[] data = null;
    private long time = 0;
    private boolean delayCounted = true;
    private DeviceError cause = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause) {
        this.cause = cause;
    }

    public boolean isDelayCounted() {
        return this.delayCounted;
    }

    public void setDelayCounted(boolean delayCounted) {
        this.delayCounted = delayCounted;
    }

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public Object[] getData() {
        return this.data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
