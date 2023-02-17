package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class ChargingSchedule extends JsonBean<ChargingSchedule> {
    private String chargingRateUnit = null;
    private ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod = new ArrayList<>();
    private Integer duration = null;
    private Double minChargingRate = null;
    private String startSchedule = null;

    public Integer getDuration() {
        return this.duration;
    }

    public void setDuration(Integer duration2) {
        this.duration = duration2;
    }

    public String getStartSchedule() {
        return this.startSchedule;
    }

    public void setStartSchedule(String startSchedule2) {
        this.startSchedule = startSchedule2;
    }

    public String getChargingRateUnit() {
        return this.chargingRateUnit;
    }

    public void setChargingRateUnit(String chargingRateUnit2) {
        this.chargingRateUnit = chargingRateUnit2;
    }

    public ArrayList<ChargingSchedulePeriod> getChargingSchedulePeriod() {
        return this.chargingSchedulePeriod;
    }

    public void setChargingSchedulePeriod(ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod2) {
        this.chargingSchedulePeriod = chargingSchedulePeriod2;
    }

    public Double getMinChargingRate() {
        return this.minChargingRate;
    }

    public void setMinChargingRate(Double minChargingRate2) {
        this.minChargingRate = minChargingRate2;
    }
}
