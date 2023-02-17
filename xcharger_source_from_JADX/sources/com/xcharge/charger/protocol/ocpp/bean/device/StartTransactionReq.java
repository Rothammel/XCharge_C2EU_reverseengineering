package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class StartTransactionReq extends JsonBean<StartTransactionReq> {
    private int connectorId;
    private String idTag;
    private int meterStart;
    private Integer reservationId = null;
    private String timestamp;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public int getMeterStart() {
        return this.meterStart;
    }

    public void setMeterStart(int meterStart2) {
        this.meterStart = meterStart2;
    }

    public Integer getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(Integer reservationId2) {
        this.reservationId = reservationId2;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }
}
