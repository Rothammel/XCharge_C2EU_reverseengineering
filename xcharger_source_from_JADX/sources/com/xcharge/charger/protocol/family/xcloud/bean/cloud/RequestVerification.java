package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class RequestVerification extends JsonBean<RequestVerification> {
    private String customer = null;
    private int expireInterval = 0;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getCustomer() {
        return this.customer;
    }

    public void setCustomer(String customer2) {
        this.customer = customer2;
    }

    public int getExpireInterval() {
        return this.expireInterval;
    }

    public void setExpireInterval(int expireInterval2) {
        this.expireInterval = expireInterval2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
