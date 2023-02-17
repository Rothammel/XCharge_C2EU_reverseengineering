package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class LocalSetting extends JsonBean<LocalSetting> {
    private ChargeSetting chargeSetting = new ChargeSetting();
    private ConsoleSetting consoleSetting = null;
    private FeeRateSetting feeRateSetting = null;
    private UserDefineUISetting userDefineUISetting = null;
    private WifiSetting wifiSetting = null;

    public ChargeSetting getChargeSetting() {
        return this.chargeSetting;
    }

    public void setChargeSetting(ChargeSetting chargeSetting2) {
        this.chargeSetting = chargeSetting2;
    }

    public UserDefineUISetting getUserDefineUISetting() {
        return this.userDefineUISetting;
    }

    public void setUserDefineUISetting(UserDefineUISetting userDefineUISetting2) {
        this.userDefineUISetting = userDefineUISetting2;
    }

    public WifiSetting getWifiSetting() {
        return this.wifiSetting;
    }

    public void setWifiSetting(WifiSetting wifiSetting2) {
        this.wifiSetting = wifiSetting2;
    }

    public FeeRateSetting getFeeRateSetting() {
        return this.feeRateSetting;
    }

    public void setFeeRateSetting(FeeRateSetting feeRateSetting2) {
        this.feeRateSetting = feeRateSetting2;
    }

    public ConsoleSetting getConsoleSetting() {
        return this.consoleSetting;
    }

    public void setConsoleSetting(ConsoleSetting consoleSetting2) {
        this.consoleSetting = consoleSetting2;
    }
}
