package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedule;
import com.xcharge.common.bean.JsonBean;

public class GetCompositeScheduleConf extends JsonBean<GetCompositeScheduleConf> {
    private ChargingSchedule chargingSchedule = null;
    private Integer connectorId = null;
    private String scheduleStart = null;
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getScheduleStart() {
        return this.scheduleStart;
    }

    public void setScheduleStart(String scheduleStart2) {
        this.scheduleStart = scheduleStart2;
    }

    public ChargingSchedule getChargingSchedule() {
        return this.chargingSchedule;
    }

    public void setChargingSchedule(ChargingSchedule chargingSchedule2) {
        this.chargingSchedule = chargingSchedule2;
    }
}
