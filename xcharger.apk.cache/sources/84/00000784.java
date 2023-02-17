package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class StopTransactionReq extends JsonBean<StopTransactionReq> {
    private String idTag;
    private int meterStop;
    private String reason;
    private String timestamp;
    private ArrayList<MeterValue> transactionData;
    private int transactionId;

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public int getMeterStop() {
        return this.meterStop;
    }

    public void setMeterStop(int meterStop) {
        this.meterStop = meterStop;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ArrayList<MeterValue> getTransactionData() {
        return this.transactionData;
    }

    public void setTransactionData(ArrayList<MeterValue> transactionData) {
        this.transactionData = transactionData;
    }
}