package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class TriggerMessageReq extends JsonBean<TriggerMessageReq> {
    private Integer connectorId = null;
    private String requestedMessage;

    public String getRequestedMessage() {
        return this.requestedMessage;
    }

    public void setRequestedMessage(String requestedMessage2) {
        this.requestedMessage = requestedMessage2;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId2) {
        this.connectorId = connectorId2;
    }
}
