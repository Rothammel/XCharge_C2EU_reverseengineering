package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class SystemSetting extends JsonBean<SystemSetting> {
    public static final String DEFAULT_DEVICE_SERVICE_CLASS = "com.xcharge.charger.device.c2.service.C2DeviceService";
    public static final String DEFAULT_UI_SERVICE_CLASS = "com.xcharge.charger.ui.adpter.c2.UIService";
    private CHARGE_PLATFORM chargePlatform = null;
    private String country = "cn";
    private String deviceServiceClass = DEFAULT_DEVICE_SERVICE_CLASS;
    private int dnsFailCacheTime = 0;
    private int dnsOkCacheTime = 86400;
    private boolean enableAutoTime = false;
    private boolean enableAutoZone = false;
    private Boolean isCPWait = null;
    private boolean isMobileRoaming = true;
    private boolean isPlug2Charge = false;
    private Boolean isWWlanPolling = null;
    private boolean isYZXMonitor = false;
    private PLATFORM_CUSTOMER platformCustomer = null;
    private HashMap<String, String> platformCustomizedData = null;
    private HashMap<String, SwipeCardPermission> portsSwipeCardPermission = null;
    private int screenBrightMode = 1;
    private String uiBackgroundColor = null;
    private String uiServiceClass = DEFAULT_UI_SERVICE_CLASS;
    private boolean usingXChargeLogo = false;

    public boolean isEnableAutoTime() {
        return this.enableAutoTime;
    }

    public void setEnableAutoTime(boolean enableAutoTime2) {
        this.enableAutoTime = enableAutoTime2;
    }

    public boolean isEnableAutoZone() {
        return this.enableAutoZone;
    }

    public void setEnableAutoZone(boolean enableAutoZone2) {
        this.enableAutoZone = enableAutoZone2;
    }

    public int getDnsOkCacheTime() {
        return this.dnsOkCacheTime;
    }

    public void setDnsOkCacheTime(int dnsOkCacheTime2) {
        this.dnsOkCacheTime = dnsOkCacheTime2;
    }

    public int getDnsFailCacheTime() {
        return this.dnsFailCacheTime;
    }

    public void setDnsFailCacheTime(int dnsFailCacheTime2) {
        this.dnsFailCacheTime = dnsFailCacheTime2;
    }

    public int getScreenBrightMode() {
        return this.screenBrightMode;
    }

    public void setScreenBrightMode(int screenBrightMode2) {
        this.screenBrightMode = screenBrightMode2;
    }

    public String getDeviceServiceClass() {
        return this.deviceServiceClass;
    }

    public void setDeviceServiceClass(String deviceServiceClass2) {
        this.deviceServiceClass = deviceServiceClass2;
    }

    public String getUiServiceClass() {
        return this.uiServiceClass;
    }

    public void setUiServiceClass(String uiServiceClass2) {
        this.uiServiceClass = uiServiceClass2;
    }

    public CHARGE_PLATFORM getChargePlatform() {
        return this.chargePlatform;
    }

    public void setChargePlatform(CHARGE_PLATFORM chargePlatform2) {
        this.chargePlatform = chargePlatform2;
    }

    public PLATFORM_CUSTOMER getPlatformCustomer() {
        return this.platformCustomer;
    }

    public void setPlatformCustomer(PLATFORM_CUSTOMER platformCustomer2) {
        this.platformCustomer = platformCustomer2;
    }

    public HashMap<String, String> getPlatformCustomizedData() {
        return this.platformCustomizedData;
    }

    public void setPlatformCustomizedData(HashMap<String, String> platformCustomizedData2) {
        this.platformCustomizedData = platformCustomizedData2;
    }

    public HashMap<String, SwipeCardPermission> getPortsSwipeCardPermission() {
        return this.portsSwipeCardPermission;
    }

    public void setPortsSwipeCardPermission(HashMap<String, SwipeCardPermission> portsSwipeCardPermission2) {
        this.portsSwipeCardPermission = portsSwipeCardPermission2;
    }

    public boolean isMobileRoaming() {
        return this.isMobileRoaming;
    }

    public void setMobileRoaming(boolean isMobileRoaming2) {
        this.isMobileRoaming = isMobileRoaming2;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country2) {
        this.country = country2;
    }

    public boolean isPlug2Charge() {
        return this.isPlug2Charge;
    }

    public void setPlug2Charge(boolean isPlug2Charge2) {
        this.isPlug2Charge = isPlug2Charge2;
    }

    public Boolean isWWlanPolling() {
        return this.isWWlanPolling;
    }

    public void setWWlanPolling(Boolean isWWlanPolling2) {
        this.isWWlanPolling = isWWlanPolling2;
    }

    public Boolean isCPWait() {
        return this.isCPWait;
    }

    public void setCPWait(Boolean isCPWait2) {
        this.isCPWait = isCPWait2;
    }

    public String getUiBackgroundColor() {
        return this.uiBackgroundColor;
    }

    public void setUiBackgroundColor(String uiBackgroundColor2) {
        this.uiBackgroundColor = uiBackgroundColor2;
    }

    public boolean isYZXMonitor() {
        return this.isYZXMonitor;
    }

    public void setYZXMonitor(boolean isYZXMonitor2) {
        this.isYZXMonitor = isYZXMonitor2;
    }

    public boolean isUsingXChargeLogo() {
        return this.usingXChargeLogo;
    }

    public void setUsingXChargeLogo(boolean usingXChargeLogo2) {
        this.usingXChargeLogo = usingXChargeLogo2;
    }
}
