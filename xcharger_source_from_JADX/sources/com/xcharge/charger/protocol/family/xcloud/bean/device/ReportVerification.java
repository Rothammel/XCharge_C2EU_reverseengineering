package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportVerification extends JsonBean<ReportVerification> {
    private Long sid = null;
    private long time = 0;

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
