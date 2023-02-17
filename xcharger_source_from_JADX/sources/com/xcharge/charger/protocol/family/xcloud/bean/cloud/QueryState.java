package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class QueryState extends JsonBean<QueryState> {
    private Long sid = null;
    private Long time = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time2) {
        this.time = time2;
    }
}
