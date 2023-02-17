package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class LocalSetting extends JsonBean<LocalSetting> {
    private ChargeSetting chargeSetting = new ChargeSetting();
    private FeeRateSetting feeRateSetting = null;
    private UserDefineUISetting userDefineUISetting = null;
    private WifiSetting wifiSetting = null;
    private ConsoleSetting consoleSetting = null;

    public ChargeSetting getChargeSetting() {
        return this.chargeSetting;
    }

    public void setChargeSetting(ChargeSetting chargeSetting) {
        this.chargeSetting = chargeSetting;
    }

    public UserDefineUISetting getUserDefineUISetting() {
        return this.userDefineUISetting;
    }

    public void setUserDefineUISetting(UserDefineUISetting userDefineUISetting) {
        this.userDefineUISetting = userDefineUISetting;
    }

    public WifiSetting getWifiSetting() {
        return this.wifiSetting;
    }

    public void setWifiSetting(WifiSetting wifiSetting) {
        this.wifiSetting = wifiSetting;
    }

    public FeeRateSetting getFeeRateSetting() {
        return this.feeRateSetting;
    }

    public void setFeeRateSetting(FeeRateSetting feeRateSetting) {
        this.feeRateSetting = feeRateSetting;
    }

    public ConsoleSetting getConsoleSetting() {
        return this.consoleSetting;
    }

    public void setConsoleSetting(ConsoleSetting consoleSetting) {
        this.consoleSetting = consoleSetting;
    }
}
