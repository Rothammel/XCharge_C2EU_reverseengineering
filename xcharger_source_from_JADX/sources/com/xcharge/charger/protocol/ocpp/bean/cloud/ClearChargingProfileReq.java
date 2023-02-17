package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class ClearChargingProfileReq extends JsonBean<ClearChargingProfileReq> {
    private String chargingProfilePurpose = null;
    private Integer connectorId = null;

    /* renamed from: id */
    private Integer f111id = null;
    private Integer stackLevel = null;

    public Integer getId() {
        return this.f111id;
    }

    public void setId(Integer id) {
        this.f111id = id;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getChargingProfilePurpose() {
        return this.chargingProfilePurpose;
    }

    public void setChargingProfilePurpose(String chargingProfilePurpose2) {
        this.chargingProfilePurpose = chargingProfilePurpose2;
    }

    public Integer getStackLevel() {
        return this.stackLevel;
    }

    public void setStackLevel(Integer stackLevel2) {
        this.stackLevel = stackLevel2;
    }
}
