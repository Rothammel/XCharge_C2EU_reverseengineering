package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.NFC_OPR_TYPE;
import com.xcharge.common.bean.JsonBean;

public class NFC extends JsonBean<NFC> {
    private boolean arrived = false;
    private Integer balance = null;
    private Integer fee = null;
    private boolean handleStatus = false;
    private String latestCardNo = null;
    private NFC_CARD_TYPE latestCardType = null;
    private ErrorCode latestError = new ErrorCode(200);
    private NFC_OPR_TYPE latestOprType = null;

    /* renamed from: ts */
    private long f54ts = 0;

    public boolean isArrived() {
        return this.arrived;
    }

    public void setArrived(boolean arrived2) {
        this.arrived = arrived2;
    }

    public boolean isHandleStatus() {
        return this.handleStatus;
    }

    public void setHandleStatus(boolean handleStatus2) {
        this.handleStatus = handleStatus2;
    }

    public long getTs() {
        return this.f54ts;
    }

    public void setTs(long ts) {
        this.f54ts = ts;
    }

    public NFC_CARD_TYPE getLatestCardType() {
        return this.latestCardType;
    }

    public void setLatestCardType(NFC_CARD_TYPE latestCardType2) {
        this.latestCardType = latestCardType2;
    }

    public String getLatestCardNo() {
        return this.latestCardNo;
    }

    public void setLatestCardNo(String latestCardNo2) {
        this.latestCardNo = latestCardNo2;
    }

    public NFC_OPR_TYPE getLatestOprType() {
        return this.latestOprType;
    }

    public void setLatestOprType(NFC_OPR_TYPE latestOprType2) {
        this.latestOprType = latestOprType2;
    }

    public ErrorCode getLatestError() {
        return this.latestError;
    }

    public void setLatestError(ErrorCode latestError2) {
        this.latestError = latestError2;
    }

    public Integer getBalance() {
        return this.balance;
    }

    public void setBalance(Integer balance2) {
        this.balance = balance2;
    }

    public Integer getFee() {
        return this.fee;
    }

    public void setFee(Integer fee2) {
        this.fee = fee2;
    }

    public NFC clone() {
        return (NFC) deepClone();
    }
}
