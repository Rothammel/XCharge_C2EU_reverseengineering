package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedule;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GetCompositeScheduleConf extends JsonBean<GetCompositeScheduleConf> {
    private String status;
    private Integer connectorId = null;
    private String scheduleStart = null;
    private ChargingSchedule chargingSchedule = null;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(Integer connectorId) {
        this.connectorId = connectorId;
    }

    public String getScheduleStart() {
        return this.scheduleStart;
    }

    public void setScheduleStart(String scheduleStart) {
        this.scheduleStart = scheduleStart;
    }

    public ChargingSchedule getChargingSchedule() {
        return this.chargingSchedule;
    }

    public void setChargingSchedule(ChargingSchedule chargingSchedule) {
        this.chargingSchedule = chargingSchedule;
    }
}
