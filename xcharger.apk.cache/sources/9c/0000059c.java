package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class SystemSetting extends JsonBean<SystemSetting> {
    public static final String DEFAULT_DEVICE_SERVICE_CLASS = "com.xcharge.charger.device.c2.service.C2DeviceService";
    public static final String DEFAULT_UI_SERVICE_CLASS = "com.xcharge.charger.ui.adpter.c2.UIService";
    private boolean enableAutoTime = false;
    private boolean enableAutoZone = false;
    private int dnsOkCacheTime = 86400;
    private int dnsFailCacheTime = 0;
    private int screenBrightMode = 1;
    private String deviceServiceClass = DEFAULT_DEVICE_SERVICE_CLASS;
    private String uiServiceClass = DEFAULT_UI_SERVICE_CLASS;
    private CHARGE_PLATFORM chargePlatform = null;
    private PLATFORM_CUSTOMER platformCustomer = null;
    private HashMap<String, String> platformCustomizedData = null;
    private HashMap<String, SwipeCardPermission> portsSwipeCardPermission = null;
    private boolean isMobileRoaming = true;
    private String country = "cn";
    private boolean isPlug2Charge = false;
    private Boolean isWWlanPolling = null;
    private Boolean isCPWait = null;
    private String uiBackgroundColor = null;
    private boolean isYZXMonitor = false;
    private boolean usingXChargeLogo = false;

    public boolean isEnableAutoTime() {
        return this.enableAutoTime;
    }

    public void setEnableAutoTime(boolean enableAutoTime) {
        this.enableAutoTime = enableAutoTime;
    }

    public boolean isEnableAutoZone() {
        return this.enableAutoZone;
    }

    public void setEnableAutoZone(boolean enableAutoZone) {
        this.enableAutoZone = enableAutoZone;
    }

    public int getDnsOkCacheTime() {
        return this.dnsOkCacheTime;
    }

    public void setDnsOkCacheTime(int dnsOkCacheTime) {
        this.dnsOkCacheTime = dnsOkCacheTime;
    }

    public int getDnsFailCacheTime() {
        return this.dnsFailCacheTime;
    }

    public void setDnsFailCacheTime(int dnsFailCacheTime) {
        this.dnsFailCacheTime = dnsFailCacheTime;
    }

    public int getScreenBrightMode() {
        return this.screenBrightMode;
    }

    public void setScreenBrightMode(int screenBrightMode) {
        this.screenBrightMode = screenBrightMode;
    }

    public String getDeviceServiceClass() {
        return this.deviceServiceClass;
    }

    public void setDeviceServiceClass(String deviceServiceClass) {
        this.deviceServiceClass = deviceServiceClass;
    }

    public String getUiServiceClass() {
        return this.uiServiceClass;
    }

    public void setUiServiceClass(String uiServiceClass) {
        this.uiServiceClass = uiServiceClass;
    }

    public CHARGE_PLATFORM getChargePlatform() {
        return this.chargePlatform;
    }

    public void setChargePlatform(CHARGE_PLATFORM chargePlatform) {
        this.chargePlatform = chargePlatform;
    }

    public PLATFORM_CUSTOMER getPlatformCustomer() {
        return this.platformCustomer;
    }

    public void setPlatformCustomer(PLATFORM_CUSTOMER platformCustomer) {
        this.platformCustomer = platformCustomer;
    }

    public HashMap<String, String> getPlatformCustomizedData() {
        return this.platformCustomizedData;
    }

    public void setPlatformCustomizedData(HashMap<String, String> platformCustomizedData) {
        this.platformCustomizedData = platformCustomizedData;
    }

    public HashMap<String, SwipeCardPermission> getPortsSwipeCardPermission() {
        return this.portsSwipeCardPermission;
    }

    public void setPortsSwipeCardPermission(HashMap<String, SwipeCardPermission> portsSwipeCardPermission) {
        this.portsSwipeCardPermission = portsSwipeCardPermission;
    }

    public boolean isMobileRoaming() {
        return this.isMobileRoaming;
    }

    public void setMobileRoaming(boolean isMobileRoaming) {
        this.isMobileRoaming = isMobileRoaming;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isPlug2Charge() {
        return this.isPlug2Charge;
    }

    public void setPlug2Charge(boolean isPlug2Charge) {
        this.isPlug2Charge = isPlug2Charge;
    }

    public Boolean isWWlanPolling() {
        return this.isWWlanPolling;
    }

    public void setWWlanPolling(Boolean isWWlanPolling) {
        this.isWWlanPolling = isWWlanPolling;
    }

    public Boolean isCPWait() {
        return this.isCPWait;
    }

    public void setCPWait(Boolean isCPWait) {
        this.isCPWait = isCPWait;
    }

    public String getUiBackgroundColor() {
        return this.uiBackgroundColor;
    }

    public void setUiBackgroundColor(String uiBackgroundColor) {
        this.uiBackgroundColor = uiBackgroundColor;
    }

    public boolean isYZXMonitor() {
        return this.isYZXMonitor;
    }

    public void setYZXMonitor(boolean isYZXMonitor) {
        this.isYZXMonitor = isYZXMonitor;
    }

    public boolean isUsingXChargeLogo() {
        return this.usingXChargeLogo;
    }

    public void setUsingXChargeLogo(boolean usingXChargeLogo) {
        this.usingXChargeLogo = usingXChargeLogo;
    }
}