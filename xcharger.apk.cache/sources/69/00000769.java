package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class TriggerMessageReq extends JsonBean<TriggerMessageReq> {
    private Integer connectorId = null;
    private String requestedMessage;

    public String getRequestedMessage() {
        return this.requestedMessage;
    }

    public void setRequestedMessage(String requestedMessage) {
        this.requestedMessage = requestedMessage;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId) {
        this.connectorId = connectorId;
    }
}