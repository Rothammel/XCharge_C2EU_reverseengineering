package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class ReserveNowReq extends JsonBean<ReserveNowReq> {
    private int connectorId;
    private String expiryDate;
    private String idTag;
    private String parentIdTag;
    private int reservationId;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate2) {
        this.expiryDate = expiryDate2;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag2) {
        this.parentIdTag = parentIdTag2;
    }

    public int getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(int reservationId2) {
        this.reservationId = reservationId2;
    }
}
