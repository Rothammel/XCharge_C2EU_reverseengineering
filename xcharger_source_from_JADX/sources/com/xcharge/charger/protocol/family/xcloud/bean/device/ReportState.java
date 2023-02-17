package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportState extends JsonBean<ReportState> {
    Object data = null;
    private Long sid = null;
    Long time = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data2) {
        this.data = data2;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time2) {
        this.time = time2;
    }
}
