package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class RequestUpdateStartTime extends JsonBean<RequestUpdateStartTime> {
    private Long billId = null;
    private Long sid = null;
    private Long startTime = null;
    private String subnode = null;
    private Long time = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode2) {
        this.subnode = subnode2;
    }

    public Long getBillId() {
        return this.billId;
    }

    public void setBillId(Long billId2) {
        this.billId = billId2;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime2) {
        this.startTime = startTime2;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time2) {
        this.time = time2;
    }
}
