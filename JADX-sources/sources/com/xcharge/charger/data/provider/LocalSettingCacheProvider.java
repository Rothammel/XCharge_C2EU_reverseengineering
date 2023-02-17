package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.LocalSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.setting.WifiSetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class LocalSettingCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.setting";
    private static LocalSettingCacheProvider instance = null;
    private Context context = null;
    private String rootPath = null;
    private ContentResolver resolver = null;
    private SharedPreferences preferences = null;
    private AtomicReference<LocalSetting> cache = new AtomicReference<>(new LocalSetting());
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.xcharge.charger.data.provider.LocalSettingCacheProvider.1
        {
            LocalSettingCacheProvider.this = this;
        }

        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if (!LocalSettingCacheProvider.this.rootPath.equals(key)) {
                return;
            }
            LocalSettingCacheProvider.this.load2Cache();
            LocalSettingCacheProvider.this.notifyChange(LocalSettingCacheProvider.this.getUriFor(null));
        }
    };

    public static LocalSettingCacheProvider getInstance() {
        if (instance == null) {
            instance = new LocalSettingCacheProvider();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.rootPath = "local";
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (platform != null) {
            this.rootPath = String.valueOf(this.rootPath) + platform.getPlatform();
        }
        this.resolver = this.context.getContentResolver();
        this.preferences = this.context.getSharedPreferences("com.xcharge.charger.data.provider.setting", 0);
        LocalSetting setting = loadSetting();
        if (setting != null) {
            this.cache.set(setting);
        }
        this.preferences.registerOnSharedPreferenceChangeListener(this.onSharedPreferenceChangeListener);
    }

    public void destroy() {
        this.preferences.unregisterOnSharedPreferenceChangeListener(this.onSharedPreferenceChangeListener);
    }

    public Uri getUriFor(String subPath) {
        String path = this.rootPath;
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.setting/" + path);
    }

    public void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, null);
    }

    public synchronized LocalSetting loadSetting() {
        String localSetting;
        localSetting = this.preferences.getString(this.rootPath, null);
        return !TextUtils.isEmpty(localSetting) ? new LocalSetting().fromJson(localSetting) : new LocalSetting();
    }

    public synchronized void load2Cache() {
        this.cache.set(loadSetting());
    }

    public synchronized boolean PersistSetting(LocalSetting setting) {
        return this.preferences.edit().putString(this.rootPath, setting.toJson()).commit();
    }

    public synchronized boolean persist() {
        return PersistSetting(this.cache.get());
    }

    public synchronized LocalSetting getLocalSetting() {
        return this.cache.get();
    }

    public synchronized boolean hasLocalSetting() {
        String localSetting;
        localSetting = this.preferences.getString(this.rootPath, null);
        return !TextUtils.isEmpty(localSetting);
    }

    public synchronized ChargeSetting getChargeSetting() {
        return this.cache.get().getChargeSetting();
    }

    public synchronized boolean updateChargeSetting(ChargeSetting setting) {
        this.cache.get().setChargeSetting(setting);
        notifyChange(getUriFor(ChargeSetting.class.getSimpleName()));
        return true;
    }

    public synchronized HashMap<String, PortSetting> getChargePortsSetting() {
        return this.cache.get().getChargeSetting().getPortsSetting();
    }

    public synchronized boolean updateChargePortsSetting(HashMap<String, PortSetting> setting) {
        this.cache.get().getChargeSetting().setPortsSetting(setting);
        notifyChange(getUriFor(String.valueOf(ChargeSetting.class.getSimpleName()) + "/ports"));
        return true;
    }

    public synchronized PortSetting getChargePortSetting(String port) {
        HashMap<String, PortSetting> chargePortsSetting;
        chargePortsSetting = this.cache.get().getChargeSetting().getPortsSetting();
        return chargePortsSetting != null ? chargePortsSetting.get(port) : null;
    }

    public synchronized boolean updateChargePortSetting(String port, PortSetting setting) {
        HashMap<String, PortSetting> portsSetting = this.cache.get().getChargeSetting().getPortsSetting();
        if (portsSetting == null) {
            portsSetting = new HashMap<>();
        }
        portsSetting.put(port, setting);
        this.cache.get().getChargeSetting().setPortsSetting(portsSetting);
        notifyChange(getUriFor(String.valueOf(ChargeSetting.class.getSimpleName()) + "/ports/" + port));
        return true;
    }

    public synchronized UserDefineUISetting getUserDefineUISetting() {
        return this.cache.get().getUserDefineUISetting();
    }

    public synchronized boolean updateUserDefineUISetting(UserDefineUISetting setting) {
        this.cache.get().setUserDefineUISetting(setting);
        notifyChange(getUriFor(UserDefineUISetting.class.getSimpleName()));
        return true;
    }

    public synchronized WifiSetting getWifiSetting() {
        return this.cache.get().getWifiSetting();
    }

    public synchronized boolean updateWifiSetting(WifiSetting setting) {
        this.cache.get().setWifiSetting(setting);
        notifyChange(getUriFor(WifiSetting.class.getSimpleName()));
        return true;
    }

    public synchronized FeeRateSetting getFeeRateSetting() {
        return this.cache.get().getFeeRateSetting();
    }

    public synchronized PortFeeRate getPortFeeRate(String port) {
        FeeRateSetting feeRateSetting;
        HashMap<String, PortFeeRate> portsFeeRate;
        feeRateSetting = this.cache.get().getFeeRateSetting();
        return (feeRateSetting == null || (portsFeeRate = feeRateSetting.getPortsFeeRate()) == null) ? null : portsFeeRate.get(port);
    }

    public synchronized boolean updateFeeRateSetting(FeeRateSetting setting) {
        this.cache.get().setFeeRateSetting(setting);
        notifyChange(getUriFor(FeeRateSetting.class.getSimpleName()));
        return true;
    }

    public synchronized boolean updatePortFeeRate(String port, PortFeeRate feeRate) {
        FeeRateSetting feeRateSetting = this.cache.get().getFeeRateSetting();
        if (feeRateSetting == null) {
            feeRateSetting = new FeeRateSetting();
        }
        HashMap<String, PortFeeRate> portsFeeRate = feeRateSetting.getPortsFeeRate();
        if (portsFeeRate == null) {
            portsFeeRate = new HashMap<>();
        }
        portsFeeRate.put(port, feeRate);
        this.cache.get().setFeeRateSetting(feeRateSetting);
        notifyChange(getUriFor(String.valueOf(PortFeeRate.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + port));
        return true;
    }

    public synchronized ConsoleSetting getConsoleSetting() {
        return this.cache.get().getConsoleSetting();
    }

    public synchronized boolean updateConsoleSetting(ConsoleSetting setting) {
        this.cache.get().setConsoleSetting(setting);
        notifyChange(getUriFor(ConsoleSetting.class.getSimpleName()));
        return true;
    }
}
