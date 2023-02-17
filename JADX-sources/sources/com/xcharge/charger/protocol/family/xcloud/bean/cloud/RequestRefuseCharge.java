package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestRefuseCharge extends JsonBean<RequestRefuseCharge> {
    private Long sid = null;
    private long port = 1;
    private DeviceError cause = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public long getPort() {
        return this.port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause) {
        this.cause = cause;
    }
}
