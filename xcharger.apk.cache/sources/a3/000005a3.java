package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RadarStatus extends JsonBean<RadarStatus> {
    private SWITCH_STATUS radarStatus = SWITCH_STATUS.disable;
    private boolean calibration = false;
    private double distance = 0.0d;

    public SWITCH_STATUS getRadarStatus() {
        return this.radarStatus;
    }

    public void setRadarStatus(SWITCH_STATUS radarStatus) {
        this.radarStatus = radarStatus;
    }

    public boolean isCalibration() {
        return this.calibration;
    }

    public void setCalibration(boolean calibration) {
        this.calibration = calibration;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}