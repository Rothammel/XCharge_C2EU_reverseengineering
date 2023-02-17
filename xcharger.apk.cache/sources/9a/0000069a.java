package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportActionResult extends JsonBean<ReportActionResult> {
    private Long sid = null;
    private long time = 0;
    private String status = null;
    private DeviceError error = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error) {
        this.error = error;
    }
}