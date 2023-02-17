package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

public class UploadLog extends JsonBean<UploadLog> {
    private DeviceError error = null;
    private Long sid = null;
    private long time = 0;

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error2) {
        this.error = error2;
    }

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
