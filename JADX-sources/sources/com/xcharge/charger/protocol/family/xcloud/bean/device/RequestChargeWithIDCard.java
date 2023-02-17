package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestChargeWithIDCard extends JsonBean<RequestChargeWithIDCard> {
    private Long sid = null;
    private String subnode = null;
    private int port = 1;
    private ChargeStopCondition autoStopAt = null;
    private String authInfo = null;
    private String sourceId = null;
    private String timestamp = null;
    private String nonce = null;
    private String signature = null;
    private Long startTime = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode) {
        this.subnode = subnode;
    }

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt) {
        this.autoStopAt = autoStopAt;
    }

    public String getAuthInfo() {
        return this.authInfo;
    }

    public void setAuthInfo(String authInfo) {
        this.authInfo = authInfo;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
