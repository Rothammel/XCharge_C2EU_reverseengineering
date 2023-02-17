package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestUpdateStartTime extends JsonBean<RequestUpdateStartTime> {
    private Long sid = null;
    private String subnode = null;
    private Long billId = null;
    private Long startTime = null;
    private Long time = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode) {
        this.subnode = subnode;
    }

    public Long getBillId() {
        return this.billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}