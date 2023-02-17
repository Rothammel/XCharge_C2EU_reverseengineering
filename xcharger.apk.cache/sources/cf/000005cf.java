package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.setting.SystemSetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class SystemSettingCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.setting";
    public static final String PATH = "system";
    public static final Uri CONTENT_URI = Uri.parse("content://com.xcharge.charger.data.provider.setting/system");
    private static SystemSettingCacheProvider instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private SharedPreferences preferences = null;
    private AtomicReference<SystemSetting> cache = new AtomicReference<>(new SystemSetting());
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.xcharge.charger.data.provider.SystemSettingCacheProvider.1
        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if (!SystemSettingCacheProvider.PATH.equals(key)) {
                return;
            }
            SystemSettingCacheProvider.this.load2Cache();
            SystemSettingCacheProvider.this.notifyChange(SystemSettingCacheProvider.this.getUriFor(null));
        }
    };

    public static SystemSettingCacheProvider getInstance() {
        if (instance == null) {
            instance = new SystemSettingCacheProvider();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        this.preferences = this.context.getSharedPreferences("com.xcharge.charger.data.provider.setting", 0);
        SystemSetting setting = loadSetting();
        if (setting != null) {
            this.cache.set(setting);
        }
        this.preferences.registerOnSharedPreferenceChangeListener(this.onSharedPreferenceChangeListener);
    }

    public void destroy() {
        this.preferences.unregisterOnSharedPreferenceChangeListener(this.onSharedPreferenceChangeListener);
    }

    public Uri getUriFor(String subPath) {
        String path = PATH;
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(PATH) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.setting/" + path);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, null);
    }

    public synchronized SystemSetting loadSetting() {
        String systemSetting;
        systemSetting = this.preferences.getString(PATH, null);
        return !TextUtils.isEmpty(systemSetting) ? new SystemSetting().fromJson(systemSetting) : new SystemSetting();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void load2Cache() {
        this.cache.set(loadSetting());
    }

    public synchronized boolean PersistSetting(SystemSetting setting) {
        return this.preferences.edit().putString(PATH, setting.toJson()).commit();
    }

    public synchronized boolean persist() {
        return PersistSetting(this.cache.get());
    }

    public synchronized SystemSetting getSystemSetting() {
        return this.cache.get();
    }

    public synchronized boolean getEnableAutoTime() {
        return this.cache.get().isEnableAutoTime();
    }

    public synchronized boolean updatetEnableAutoTime(boolean enable) {
        this.cache.get().setEnableAutoTime(enable);
        notifyChange(getUriFor("enableAutoTime"));
        return true;
    }

    public synchronized boolean getEnableAutoZone() {
        return this.cache.get().isEnableAutoZone();
    }

    public synchronized boolean updatetEnableAutoZone(boolean enable) {
        this.cache.get().setEnableAutoZone(enable);
        notifyChange(getUriFor("enableAutoZone"));
        return true;
    }

    public synchronized int getDnsOkCacheTime() {
        return this.cache.get().getDnsOkCacheTime();
    }

    public synchronized boolean updatetDnsOkCacheTime(int time) {
        this.cache.get().setDnsOkCacheTime(time);
        notifyChange(getUriFor("dnsOkCacheTime"));
        return true;
    }

    public synchronized int getDnsFailCacheTime() {
        return this.cache.get().getDnsFailCacheTime();
    }

    public synchronized boolean updatetDnsFailCacheTime(int time) {
        this.cache.get().setDnsFailCacheTime(time);
        notifyChange(getUriFor("dnsFailCacheTime"));
        return true;
    }

    public synchronized int getScreenBrightMode() {
        return this.cache.get().getScreenBrightMode();
    }

    public synchronized boolean updatetScreenBrightMode(int mode) {
        this.cache.get().setScreenBrightMode(mode);
        notifyChange(getUriFor("screenBrightMode"));
        return true;
    }

    public synchronized String getDeviceServiceClass() {
        return this.cache.get().getDeviceServiceClass();
    }

    public synchronized boolean updatetDeviceServiceClass(String name) {
        this.cache.get().setDeviceServiceClass(name);
        notifyChange(getUriFor("deviceServiceClass"));
        return true;
    }

    public synchronized String getUiServiceClass() {
        return this.cache.get().getUiServiceClass();
    }

    public synchronized boolean updatetUiServiceClass(String name) {
        this.cache.get().setUiServiceClass(name);
        notifyChange(getUriFor("uiServiceClass"));
        return true;
    }

    public synchronized CHARGE_PLATFORM getChargePlatform() {
        return this.cache.get().getChargePlatform();
    }

    public synchronized boolean updatetChargePlatform(CHARGE_PLATFORM platform) {
        this.cache.get().setChargePlatform(platform);
        notifyChange(getUriFor("chargePlatform"));
        return true;
    }

    public synchronized PLATFORM_CUSTOMER getPlatformCustomer() {
        return this.cache.get().getPlatformCustomer();
    }

    public synchronized boolean updatetPlatformCustomer(PLATFORM_CUSTOMER customer) {
        this.cache.get().setPlatformCustomer(customer);
        notifyChange(getUriFor("platformCustomer"));
        return true;
    }

    public synchronized HashMap<String, String> getPlatformCustomizedData() {
        return this.cache.get().getPlatformCustomizedData();
    }

    public synchronized boolean updatePlatformCustomizedData(HashMap<String, String> data) {
        this.cache.get().setPlatformCustomizedData(data);
        notifyChange(getUriFor("platformCustomizedData"));
        return true;
    }

    public synchronized HashMap<String, SwipeCardPermission> getPortsSwipeCardPermission() {
        return this.cache.get().getPortsSwipeCardPermission();
    }

    public synchronized boolean updatePortsSwipeCardPermission(HashMap<String, SwipeCardPermission> permission) {
        this.cache.get().setPortsSwipeCardPermission(permission);
        notifyChange(getUriFor("card/permission/swipe"));
        return true;
    }

    public synchronized SwipeCardPermission getPortSwipeCardPermission(String port) {
        HashMap<String, SwipeCardPermission> portsSwipeCardPermission;
        portsSwipeCardPermission = this.cache.get().getPortsSwipeCardPermission();
        return portsSwipeCardPermission != null ? portsSwipeCardPermission.get(port) : null;
    }

    public synchronized boolean updatePortSwipeCardPermission(String port, SwipeCardPermission permission) {
        HashMap<String, SwipeCardPermission> portsSwipeCardPermission = this.cache.get().getPortsSwipeCardPermission();
        if (portsSwipeCardPermission == null) {
            portsSwipeCardPermission = new HashMap<>();
        }
        portsSwipeCardPermission.put(port, permission);
        this.cache.get().setPortsSwipeCardPermission(portsSwipeCardPermission);
        notifyChange(getUriFor("card/permission/swipe/" + port));
        return true;
    }

    public synchronized boolean isMobileRoaming() {
        return this.cache.get().isMobileRoaming();
    }

    public synchronized boolean setMobileRoaming(boolean isMobileRoaming) {
        this.cache.get().setMobileRoaming(isMobileRoaming);
        notifyChange(getUriFor("mobileRoaming"));
        return true;
    }

    public synchronized String getCountry() {
        return this.cache.get().getCountry();
    }

    public synchronized boolean setCountry(String country) {
        this.cache.get().setCountry(country);
        notifyChange(getUriFor("country"));
        return true;
    }

    public synchronized boolean isPlug2Charge() {
        return this.cache.get().isPlug2Charge();
    }

    public synchronized boolean setPlug2Charge(boolean isPlug2Charge) {
        this.cache.get().setPlug2Charge(isPlug2Charge);
        notifyChange(getUriFor("plug2Charge"));
        return true;
    }

    public synchronized boolean isYZXMonitor() {
        return this.cache.get().isYZXMonitor();
    }

    public synchronized boolean setYZXMonitor(boolean isYZXMonitor) {
        this.cache.get().setYZXMonitor(isYZXMonitor);
        notifyChange(getUriFor("yzxMonitor"));
        return true;
    }

    public synchronized Boolean isWWlanPolling() {
        return this.cache.get().isWWlanPolling();
    }

    public synchronized boolean setWWlanPolling(Boolean isWWlanPolling) {
        this.cache.get().setWWlanPolling(isWWlanPolling);
        notifyChange(getUriFor("wwlanPoll"));
        return true;
    }

    public synchronized Boolean isCPWait() {
        return this.cache.get().isCPWait();
    }

    public synchronized boolean setCPWait(Boolean isCPWait) {
        this.cache.get().setCPWait(isCPWait);
        notifyChange(getUriFor("cpWait"));
        return true;
    }

    public synchronized String getUiBackgroundColor() {
        return this.cache.get().getUiBackgroundColor();
    }

    public synchronized boolean setUiBackgroundColor(String uiBackgroundColor) {
        this.cache.get().setUiBackgroundColor(uiBackgroundColor);
        notifyChange(getUriFor("uiBackgroundColor"));
        return true;
    }

    public synchronized boolean isUsingXChargeLogo() {
        return this.cache.get().isUsingXChargeLogo();
    }

    public synchronized boolean setUsingXChargeLogo(boolean isUsingXChargeLogo) {
        this.cache.get().setUsingXChargeLogo(isUsingXChargeLogo);
        notifyChange(getUriFor("isUsingXChargeLogo"));
        return true;
    }
}