package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ChargingSchedule extends JsonBean<ChargingSchedule> {
    private Integer duration = null;
    private String startSchedule = null;
    private String chargingRateUnit = null;
    private ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod = new ArrayList<>();
    private Double minChargingRate = null;

    public Integer getDuration() {
        return this.duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStartSchedule() {
        return this.startSchedule;
    }

    public void setStartSchedule(String startSchedule) {
        this.startSchedule = startSchedule;
    }

    public String getChargingRateUnit() {
        return this.chargingRateUnit;
    }

    public void setChargingRateUnit(String chargingRateUnit) {
        this.chargingRateUnit = chargingRateUnit;
    }

    public ArrayList<ChargingSchedulePeriod> getChargingSchedulePeriod() {
        return this.chargingSchedulePeriod;
    }

    public void setChargingSchedulePeriod(ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod) {
        this.chargingSchedulePeriod = chargingSchedulePeriod;
    }

    public Double getMinChargingRate() {
        return this.minChargingRate;
    }

    public void setMinChargingRate(Double minChargingRate) {
        this.minChargingRate = minChargingRate;
    }
}
