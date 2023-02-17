package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class PortSetting extends JsonBean<PortSetting> {
    private Integer ampPercent = null;
    private boolean enable = true;
    private GunLockSetting gunLockSetting = new GunLockSetting();
    private ParkLockSetting parkLockSetting = new ParkLockSetting();
    private RadarSetting radarSetting = new RadarSetting();

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable2) {
        this.enable = enable2;
    }

    public RadarSetting getRadarSetting() {
        return this.radarSetting;
    }

    public void setRadarSetting(RadarSetting radarSetting2) {
        this.radarSetting = radarSetting2;
    }

    public ParkLockSetting getParkLockSetting() {
        return this.parkLockSetting;
    }

    public void setParkLockSetting(ParkLockSetting parkLockSetting2) {
        this.parkLockSetting = parkLockSetting2;
    }

    public GunLockSetting getGunLockSetting() {
        return this.gunLockSetting;
    }

    public void setGunLockSetting(GunLockSetting gunLockSetting2) {
        this.gunLockSetting = gunLockSetting2;
    }

    public Integer getAmpPercent() {
        return this.ampPercent;
    }

    public void setAmpPercent(Integer ampPercent2) {
        this.ampPercent = ampPercent2;
    }
}
