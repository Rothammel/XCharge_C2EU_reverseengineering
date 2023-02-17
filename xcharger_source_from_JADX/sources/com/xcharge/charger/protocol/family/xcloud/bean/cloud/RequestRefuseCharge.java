package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

public class RequestRefuseCharge extends JsonBean<RequestRefuseCharge> {
    private DeviceError cause = null;
    private long port = 1;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public long getPort() {
        return this.port;
    }

    public void setPort(long port2) {
        this.port = port2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause2) {
        this.cause = cause2;
    }
}
