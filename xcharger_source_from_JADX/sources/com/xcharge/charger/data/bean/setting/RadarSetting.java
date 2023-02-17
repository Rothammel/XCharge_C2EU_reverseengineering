package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class RadarSetting extends JsonBean<RadarSetting> {
    private double distance = 4.0d;
    private boolean enable = false;
    private String workTime = "00:00-24:00";

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable2) {
        this.enable = enable2;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance2) {
        this.distance = distance2;
    }

    public String getWorkTime() {
        return this.workTime;
    }

    public void setWorkTime(String workTime2) {
        this.workTime = workTime2;
    }
}
