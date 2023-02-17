package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.NFC_OPR_TYPE;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFC extends JsonBean<NFC> {
    private boolean arrived = false;
    private boolean handleStatus = false;
    private long ts = 0;
    private NFC_CARD_TYPE latestCardType = null;
    private String latestCardNo = null;
    private NFC_OPR_TYPE latestOprType = null;
    private ErrorCode latestError = new ErrorCode(200);
    private Integer balance = null;
    private Integer fee = null;

    public boolean isArrived() {
        return this.arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public boolean isHandleStatus() {
        return this.handleStatus;
    }

    public void setHandleStatus(boolean handleStatus) {
        this.handleStatus = handleStatus;
    }

    public long getTs() {
        return this.ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public NFC_CARD_TYPE getLatestCardType() {
        return this.latestCardType;
    }

    public void setLatestCardType(NFC_CARD_TYPE latestCardType) {
        this.latestCardType = latestCardType;
    }

    public String getLatestCardNo() {
        return this.latestCardNo;
    }

    public void setLatestCardNo(String latestCardNo) {
        this.latestCardNo = latestCardNo;
    }

    public NFC_OPR_TYPE getLatestOprType() {
        return this.latestOprType;
    }

    public void setLatestOprType(NFC_OPR_TYPE latestOprType) {
        this.latestOprType = latestOprType;
    }

    public ErrorCode getLatestError() {
        return this.latestError;
    }

    public void setLatestError(ErrorCode latestError) {
        this.latestError = latestError;
    }

    public Integer getBalance() {
        return this.balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getFee() {
        return this.fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    /* renamed from: clone */
    public NFC m8clone() {
        return deepClone();
    }
}