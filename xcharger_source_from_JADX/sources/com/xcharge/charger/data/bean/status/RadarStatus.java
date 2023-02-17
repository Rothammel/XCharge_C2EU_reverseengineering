package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

public class RadarStatus extends JsonBean<RadarStatus> {
    private boolean calibration = false;
    private double distance = 0.0d;
    private SWITCH_STATUS radarStatus = SWITCH_STATUS.disable;

    public SWITCH_STATUS getRadarStatus() {
        return this.radarStatus;
    }

    public void setRadarStatus(SWITCH_STATUS radarStatus2) {
        this.radarStatus = radarStatus2;
    }

    public boolean isCalibration() {
        return this.calibration;
    }

    public void setCalibration(boolean calibration2) {
        this.calibration = calibration2;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance2) {
        this.distance = distance2;
    }
}
