package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class Radar extends JsonBean<Radar> {
    private SWITCH_STATUS status = SWITCH_STATUS.disable;
    private boolean calibration = false;
    private Integer calibrateDist = null;
    private Integer detectDist = null;

    public SWITCH_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(SWITCH_STATUS status) {
        this.status = status;
    }

    public Integer getCalibrateDist() {
        return this.calibrateDist;
    }

    public void setCalibrateDist(Integer calibrateDist) {
        this.calibrateDist = calibrateDist;
    }

    public Integer getDetectDist() {
        return this.detectDist;
    }

    public void setDetectDist(Integer detectDist) {
        this.detectDist = detectDist;
    }

    public boolean isCalibration() {
        return this.calibration;
    }

    public void setCalibration(boolean calibration) {
        this.calibration = calibration;
    }
}