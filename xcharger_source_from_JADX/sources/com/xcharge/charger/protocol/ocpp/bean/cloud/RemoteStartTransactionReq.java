package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.common.bean.JsonBean;

public class RemoteStartTransactionReq extends JsonBean<RemoteStartTransactionReq> {
    private ChargingProfile chargingProfile;
    private Integer connectorId = null;
    private String idTag;

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public ChargingProfile getChargingProfile() {
        return this.chargingProfile;
    }

    public void setChargingProfile(ChargingProfile chargingProfile2) {
        this.chargingProfile = chargingProfile2;
    }
}
