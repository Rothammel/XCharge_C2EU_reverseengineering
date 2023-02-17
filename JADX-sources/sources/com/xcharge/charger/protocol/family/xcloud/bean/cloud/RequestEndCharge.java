package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestEndCharge extends JsonBean<RequestEndCharge> {
    private Long sid = null;
    private String subnode = null;
    private Long billId = null;
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

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
