package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

public class RequestChargeWithIDCard extends JsonBean<RequestChargeWithIDCard> {
    private String authInfo = null;
    private ChargeStopCondition autoStopAt = null;
    private String nonce = null;
    private int port = 1;
    private Long sid = null;
    private String signature = null;
    private String sourceId = null;
    private Long startTime = null;
    private String subnode = null;
    private long time = 0;
    private String timestamp = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(String sourceId2) {
        this.sourceId = sourceId2;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce2) {
        this.nonce = nonce2;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature2) {
        this.signature = signature2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode2) {
        this.subnode = subnode2;
    }

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt2) {
        this.autoStopAt = autoStopAt2;
    }

    public String getAuthInfo() {
        return this.authInfo;
    }

    public void setAuthInfo(String authInfo2) {
        this.authInfo = authInfo2;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime2) {
        this.startTime = startTime2;
    }
}
