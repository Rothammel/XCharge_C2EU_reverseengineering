package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class GetCompositeScheduleReq extends JsonBean<GetCompositeScheduleReq> {
    private String chargingRateUnit = null;
    private int connectorId;
    private int duration;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration2) {
        this.duration = duration2;
    }

    public String getChargingRateUnit() {
        return this.chargingRateUnit;
    }

    public void setChargingRateUnit(String chargingRateUnit2) {
        this.chargingRateUnit = chargingRateUnit2;
    }
}
