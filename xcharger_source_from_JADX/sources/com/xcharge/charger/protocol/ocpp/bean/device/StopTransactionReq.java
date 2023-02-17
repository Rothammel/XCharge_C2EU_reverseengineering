package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

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

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public int getMeterStop() {
        return this.meterStop;
    }

    public void setMeterStop(int meterStop2) {
        this.meterStop = meterStop2;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId2) {
        this.transactionId = transactionId2;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason2) {
        this.reason = reason2;
    }

    public ArrayList<MeterValue> getTransactionData() {
        return this.transactionData;
    }

    public void setTransactionData(ArrayList<MeterValue> transactionData2) {
        this.transactionData = transactionData2;
    }
}
