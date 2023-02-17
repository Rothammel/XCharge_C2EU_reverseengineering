package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RemoteStartTransactionReq extends JsonBean<RemoteStartTransactionReq> {
    private ChargingProfile chargingProfile;
    private Integer connectorId = null;
    private String idTag;

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId) {
        this.connectorId = connectorId;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public ChargingProfile getChargingProfile() {
        return this.chargingProfile;
    }

    public void setChargingProfile(ChargingProfile chargingProfile) {
        this.chargingProfile = chargingProfile;
    }
}
