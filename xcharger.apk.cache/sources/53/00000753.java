package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChangeAvailabilityReq extends JsonBean<ChangeAvailabilityReq> {
    private int connectorId;
    private String type;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}