package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargePriority;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

public class RequestStartCharge extends JsonBean<RequestStartCharge> {
    private ChargeStopCondition autoStopAt = null;
    private int balance = 0;
    private long billId = 0;
    private Long feePolicyId = null;
    private boolean forcePlugging = false;
    private int port = 1;
    private ChargePriority priority = null;
    private Long sid = null;
    private Long startTime = null;
    private long time = 0;

    public ChargeStopCondition getAutoStopAt() {
        return this.autoStopAt;
    }

    public void setAutoStopAt(ChargeStopCondition autoStopAt2) {
        this.autoStopAt = autoStopAt2;
    }

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

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId2) {
        this.billId = billId2;
    }

    public Long getFeePolicyId() {
        return this.feePolicyId;
    }

    public void setFeePolicyId(Long feePolicyId2) {
        this.feePolicyId = feePolicyId2;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public boolean isForcePlugging() {
        return this.forcePlugging;
    }

    public void setForcePlugging(boolean forcePlugging2) {
        this.forcePlugging = forcePlugging2;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime2) {
        this.startTime = startTime2;
    }

    public ChargePriority getPriority() {
        return this.priority;
    }

    public void setPriority(ChargePriority priority2) {
        this.priority = priority2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
