package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class ChangeAvailabilityReq extends JsonBean<ChangeAvailabilityReq> {
    private int connectorId;
    private String type;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }
}
