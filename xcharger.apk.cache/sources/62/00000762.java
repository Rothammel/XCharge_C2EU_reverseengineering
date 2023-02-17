package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReserveNowReq extends JsonBean<ReserveNowReq> {
    private int connectorId;
    private String expiryDate;
    private String idTag;
    private String parentIdTag;
    private int reservationId;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag) {
        this.parentIdTag = parentIdTag;
    }

    public int getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
}