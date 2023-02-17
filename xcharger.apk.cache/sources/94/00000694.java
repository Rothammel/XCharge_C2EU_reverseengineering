package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargePriority;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestStartCharge extends JsonBean<RequestStartCharge> {
    private Long sid = null;
    private int port = 1;
    private long billId = 0;
    private Long feePolicyId = null;
    private ChargeStopCondition autoStopAt = null;
    private Long startTime = null;
    private ChargePriority priority = null;
    private int balance = 0;
    private boolean forcePlugging = false;
    private long time = 0;

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt) {
        this.autoStopAt = autoStopAt;
    }

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

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public Long getFeePolicyId() {
        return this.feePolicyId;
    }

    public void setFeePolicyId(Long feePolicyId) {
        this.feePolicyId = feePolicyId;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public boolean isForcePlugging() {
        return this.forcePlugging;
    }

    public void setForcePlugging(boolean forcePlugging) {
        this.forcePlugging = forcePlugging;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public ChargePriority getPriority() {
        return this.priority;
    }

    public void setPriority(ChargePriority priority) {
        this.priority = priority;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}