package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.device.BLN;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RemoteSetting extends JsonBean<RemoteSetting> {
    private ChargeSetting chargeSetting = new ChargeSetting();
    private FeeRateSetting feeRateSetting = null;
    private UserDefineUISetting userDefineUISetting = null;
    private WifiSetting wifiSetting = null;
    private String defaultBLNColor = BLN.DEFAULT_COLOR_STR;
    private AdvertSetting advertSetting = null;
    private String protocolTimezone = null;
    private CountrySetting countrySetting = null;

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

    public String getDefaultBLNColor() {
        return this.defaultBLNColor;
    }

    public void setDefaultBLNColor(String defaultBLNColor) {
        this.defaultBLNColor = defaultBLNColor;
    }

    public AdvertSetting getAdvertSetting() {
        return this.advertSetting;
    }

    public void setAdvertSetting(AdvertSetting advertSetting) {
        this.advertSetting = advertSetting;
    }

    public String getProtocolTimezone() {
        return this.protocolTimezone;
    }

    public void setProtocolTimezone(String protocolTimezone) {
        this.protocolTimezone = protocolTimezone;
    }

    public CountrySetting getCountrySetting() {
        return this.countrySetting;
    }

    public void setCountrySetting(CountrySetting countrySetting) {
        this.countrySetting = countrySetting;
    }
}