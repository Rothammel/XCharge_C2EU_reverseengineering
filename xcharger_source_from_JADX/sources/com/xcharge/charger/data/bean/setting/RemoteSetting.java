package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.device.BLN;
import com.xcharge.common.bean.JsonBean;

public class RemoteSetting extends JsonBean<RemoteSetting> {
    private AdvertSetting advertSetting = null;
    private ChargeSetting chargeSetting = new ChargeSetting();
    private CountrySetting countrySetting = null;
    private String defaultBLNColor = BLN.DEFAULT_COLOR_STR;
    private FeeRateSetting feeRateSetting = null;
    private String protocolTimezone = null;
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

    public String getDefaultBLNColor() {
        return this.defaultBLNColor;
    }

    public void setDefaultBLNColor(String defaultBLNColor2) {
        this.defaultBLNColor = defaultBLNColor2;
    }

    public AdvertSetting getAdvertSetting() {
        return this.advertSetting;
    }

    public void setAdvertSetting(AdvertSetting advertSetting2) {
        this.advertSetting = advertSetting2;
    }

    public String getProtocolTimezone() {
        return this.protocolTimezone;
    }

    public void setProtocolTimezone(String protocolTimezone2) {
        this.protocolTimezone = protocolTimezone2;
    }

    public CountrySetting getCountrySetting() {
        return this.countrySetting;
    }

    public void setCountrySetting(CountrySetting countrySetting2) {
        this.countrySetting = countrySetting2;
    }
}
