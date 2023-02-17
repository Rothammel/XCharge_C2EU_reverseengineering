package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportPlugStatus extends JsonBean<ReportPlugStatus> {
    private boolean connected = false;
    private Integer port = null;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port2) {
        this.port = port2;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected2) {
        this.connected = connected2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
