package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class PortSetting extends JsonBean<PortSetting> {
    private boolean enable = true;
    private RadarSetting radarSetting = new RadarSetting();
    private ParkLockSetting parkLockSetting = new ParkLockSetting();
    private GunLockSetting gunLockSetting = new GunLockSetting();
    private Integer ampPercent = null;

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public RadarSetting getRadarSetting() {
        return this.radarSetting;
    }

    public void setRadarSetting(RadarSetting radarSetting) {
        this.radarSetting = radarSetting;
    }

    public ParkLockSetting getParkLockSetting() {
        return this.parkLockSetting;
    }

    public void setParkLockSetting(ParkLockSetting parkLockSetting) {
        this.parkLockSetting = parkLockSetting;
    }

    public GunLockSetting getGunLockSetting() {
        return this.gunLockSetting;
    }

    public void setGunLockSetting(GunLockSetting gunLockSetting) {
        this.gunLockSetting = gunLockSetting;
    }

    public Integer getAmpPercent() {
        return this.ampPercent;
    }

    public void setAmpPercent(Integer ampPercent) {
        this.ampPercent = ampPercent;
    }
}