package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class SetChargingProfileReq extends JsonBean<SetChargingProfileReq> {
    private int connectorId;
    private ChargingProfile csChargingProfiles;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public ChargingProfile getCsChargingProfiles() {
        return this.csChargingProfiles;
    }

    public void setCsChargingProfiles(ChargingProfile csChargingProfiles) {
        this.csChargingProfiles = csChargingProfiles;
    }
}