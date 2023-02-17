package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GetCompositeScheduleReq extends JsonBean<GetCompositeScheduleReq> {
    private String chargingRateUnit = null;
    private int connectorId;
    private int duration;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getChargingRateUnit() {
        return this.chargingRateUnit;
    }

    public void setChargingRateUnit(String chargingRateUnit) {
        this.chargingRateUnit = chargingRateUnit;
    }
}