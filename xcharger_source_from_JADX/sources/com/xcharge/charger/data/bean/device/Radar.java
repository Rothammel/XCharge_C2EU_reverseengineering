package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

public class Radar extends JsonBean<Radar> {
    private Integer calibrateDist = null;
    private boolean calibration = false;
    private Integer detectDist = null;
    private SWITCH_STATUS status = SWITCH_STATUS.disable;

    public SWITCH_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(SWITCH_STATUS status2) {
        this.status = status2;
    }

    public Integer getCalibrateDist() {
        return this.calibrateDist;
    }

    public void setCalibrateDist(Integer calibrateDist2) {
        this.calibrateDist = calibrateDist2;
    }

    public Integer getDetectDist() {
        return this.detectDist;
    }

    public void setDetectDist(Integer detectDist2) {
        this.detectDist = detectDist2;
    }

    public boolean isCalibration() {
        return this.calibration;
    }

    public void setCalibration(boolean calibration2) {
        this.calibration = calibration2;
    }
}
