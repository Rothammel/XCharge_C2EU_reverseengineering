package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

public class ReportChargeStopped extends JsonBean<ReportChargeStopped> {
    private long billId = 0;
    private DeviceError cause = null;
    private Object[] data = null;
    private boolean delayCounted = true;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause2) {
        this.cause = cause2;
    }

    public boolean isDelayCounted() {
        return this.delayCounted;
    }

    public void setDelayCounted(boolean delayCounted2) {
        this.delayCounted = delayCounted2;
    }

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId2) {
        this.billId = billId2;
    }

    public Object[] getData() {
        return this.data;
    }

    public void setData(Object[] data2) {
        this.data = data2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
