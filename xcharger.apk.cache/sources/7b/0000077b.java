package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class MeterValuesReq extends JsonBean<MeterValuesReq> {
    private int connectorId;
    private Integer transactionId = null;
    private ArrayList<MeterValue> meterValue = new ArrayList<>();

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public Integer getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public ArrayList<MeterValue> getMeterValue() {
        return this.meterValue;
    }

    public void setMeterValue(ArrayList<MeterValue> meterValue) {
        this.meterValue = meterValue;
    }
}