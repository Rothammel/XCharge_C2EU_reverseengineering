package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class MeterValuesReq extends JsonBean<MeterValuesReq> {
    private int connectorId;
    private ArrayList<MeterValue> meterValue = new ArrayList<>();
    private Integer transactionId = null;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public Integer getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(Integer transactionId2) {
        this.transactionId = transactionId2;
    }

    public ArrayList<MeterValue> getMeterValue() {
        return this.meterValue;
    }

    public void setMeterValue(ArrayList<MeterValue> meterValue2) {
        this.meterValue = meterValue2;
    }
}
