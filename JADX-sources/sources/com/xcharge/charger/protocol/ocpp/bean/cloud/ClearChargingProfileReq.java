package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ClearChargingProfileReq extends JsonBean<ClearChargingProfileReq> {
    private Integer id = null;
    private Integer connectorId = null;
    private String chargingProfilePurpose = null;
    private Integer stackLevel = null;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId) {
        this.connectorId = connectorId;
    }

    public String getChargingProfilePurpose() {
        return this.chargingProfilePurpose;
    }

    public void setChargingProfilePurpose(String chargingProfilePurpose) {
        this.chargingProfilePurpose = chargingProfilePurpose;
    }

    public Integer getStackLevel() {
        return this.stackLevel;
    }

    public void setStackLevel(Integer stackLevel) {
        this.stackLevel = stackLevel;
    }
}
