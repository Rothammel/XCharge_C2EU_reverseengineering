package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.setting.AdvertSetting;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.RemoteSetting;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.setting.WifiSetting;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class RemoteSettingCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.setting";
    private static RemoteSettingCacheProvider instance = null;
    private Context context = null;
    private String rootPath = null;
    private ContentResolver resolver = null;
    private SharedPreferences preferences = null;
    private AtomicReference<RemoteSetting> cache = new AtomicReference<>(new RemoteSetting());
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.xcharge.charger.data.provider.RemoteSettingCacheProvider.1
        {
            RemoteSettingCacheProvider.this = this;
        }

        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if (!RemoteSettingCacheProvider.this.rootPath.equals(key)) {
                return;
            }
            RemoteSettingCacheProvider.this.load2Cache();
            RemoteSettingCacheProvider.this.notifyChange(RemoteSettingCacheProvider.this.getUriFor(null));
        }
    };

    public static RemoteSettingCacheProvider getInstance() {
        if (instance == null) {
            instance = new RemoteSettingCacheProvider();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.rootPath = "remote";
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (platform != null) {
            this.rootPath = String.valueOf(this.rootPath) + platform.getPlatform();
        }
        this.resolver = this.context.getContentResolver();
        this.preferences = this.context.getSharedPreferences("com.xcharge.charger.data.provider.setting", 0);
        RemoteSetting setting = loadSetting();
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

    public synchronized RemoteSetting loadSetting() {
        String remoteSetting;
        remoteSetting = this.preferences.getString(this.rootPath, null);
        return !TextUtils.isEmpty(remoteSetting) ? new RemoteSetting().fromJson(remoteSetting) : new RemoteSetting();
    }

    public synchronized void load2Cache() {
        this.cache.set(loadSetting());
    }

    public synchronized boolean PersistSetting(RemoteSetting setting) {
        return this.preferences.edit().putString(this.rootPath, setting.toJson()).commit();
    }

    public synchronized boolean persist() {
        return PersistSetting(this.cache.get());
    }

    public synchronized RemoteSetting getRemoteSetting() {
        return this.cache.get();
    }

    public synchronized boolean hasRemoteSetting() {
        String remoteSetting;
        remoteSetting = this.preferences.getString(this.rootPath, null);
        return !TextUtils.isEmpty(remoteSetting);
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
        feeRateSetting.setPortsFeeRate(portsFeeRate);
        this.cache.get().setFeeRateSetting(feeRateSetting);
        notifyChange(getUriFor(String.valueOf(PortFeeRate.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + port));
        return true;
    }

    public synchronized AdvertSetting getAdvertSetting() {
        return this.cache.get().getAdvertSetting();
    }

    public synchronized boolean updateAdvertSetting(AdvertSetting setting) {
        this.cache.get().setAdvertSetting(setting);
        notifyChange(getUriFor(AdvertSetting.class.getSimpleName()));
        return true;
    }

    public synchronized String getDefaultBLNColor() {
        return this.cache.get().getDefaultBLNColor();
    }

    public synchronized boolean updateDefaultBLNColor(String color) {
        String oldColor = new String(this.cache.get().getDefaultBLNColor());
        this.cache.get().setDefaultBLNColor(color);
        if (!oldColor.equals(color)) {
            notifyChange(getUriFor("defaultBLNColor"));
        }
        return true;
    }

    public synchronized String getWelcomeContent() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getWelcome() : null;
    }

    public synchronized boolean updateWelcomeContent(String welcome) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setWelcome(welcome);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/welcome"));
        return true;
    }

    public synchronized String getScanHintTitleContent() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getScanHintTitle() : null;
    }

    public synchronized boolean updateScanHintTitleContent(String scanHintTitle) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setScanHintTitle(scanHintTitle);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/scanHintTitle"));
        return true;
    }

    public synchronized String getScanHintDescContent() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getScanHintDesc() : null;
    }

    public synchronized boolean updateScanHintDescContent(String scanHintDesc) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setScanHintDesc(scanHintDesc);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/scanHintDesc"));
        return true;
    }

    public synchronized String getCompanyResouce() {
        UserDefineUISetting userDefineUISetting;
        ContentItem company;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return (userDefineUISetting == null || (company = userDefineUISetting.getCompany()) == null) ? null : company.getLocalPath();
    }

    public synchronized boolean updateCompanyResouce(String path) {
        boolean z;
        ContentItem company;
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null || (company = userDefineUISetting.getCompany()) == null) {
            z = false;
        } else {
            company.setLocalPath(path);
            notifyChange(getUriFor("resource/company"));
            z = true;
        }
        return z;
    }

    public synchronized ContentItem getCompanyContent() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getCompany() : null;
    }

    public synchronized boolean updateCompanyContent(ContentItem company) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setCompany(company);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/company"));
        return true;
    }

    public synchronized String getLogoResouce() {
        UserDefineUISetting userDefineUISetting;
        ContentItem logo;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return (userDefineUISetting == null || (logo = userDefineUISetting.getLogo()) == null) ? null : logo.getLocalPath();
    }

    public synchronized boolean updateLogoResouce(String path) {
        boolean z;
        ContentItem logo;
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null || (logo = userDefineUISetting.getLogo()) == null) {
            z = false;
        } else {
            logo.setLocalPath(path);
            notifyChange(getUriFor("resource/logo"));
            z = true;
        }
        return z;
    }

    public synchronized ContentItem getLogoContent() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getLogo() : null;
    }

    public synchronized boolean updateLogoContent(ContentItem logo) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setLogo(logo);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/logo"));
        return true;
    }

    public synchronized ArrayList<ContentItem> getAdvertContent(ADVERT_POLICY type) {
        AdvertSetting advertSetting;
        HashMap<String, ArrayList<ContentItem>> content;
        advertSetting = this.cache.get().getAdvertSetting();
        return (advertSetting == null || (content = advertSetting.getContent()) == null) ? null : content.get(type.getPolicy());
    }

    public synchronized boolean updateAdvertContent(ADVERT_POLICY type, ArrayList<ContentItem> content) {
        AdvertSetting advertSetting = this.cache.get().getAdvertSetting();
        if (advertSetting == null) {
            advertSetting = new AdvertSetting();
        }
        HashMap<String, ArrayList<ContentItem>> contents = advertSetting.getContent();
        if (contents == null) {
            contents = new HashMap<>();
        }
        contents.put(type.getPolicy(), content);
        advertSetting.setContent(contents);
        this.cache.get().setAdvertSetting(advertSetting);
        notifyChange(getUriFor("content/advert/" + type.getPolicy()));
        return true;
    }

    public synchronized boolean updateAdvertResouce(ADVERT_POLICY type, int index, String path) {
        boolean z;
        ArrayList<ContentItem> contentItemArray = getAdvertContent(type);
        if (contentItemArray == null || contentItemArray.size() <= index) {
            z = false;
        } else {
            ContentItem contentItem = contentItemArray.get(index);
            contentItem.setLocalPath(path);
            notifyChange(getUriFor("resource/advert/" + type.getPolicy() + MqttTopic.TOPIC_LEVEL_SEPARATOR + index));
            z = true;
        }
        return z;
    }

    public synchronized boolean updateAdvertContent(ADVERT_POLICY type, int index, ContentItem contentItem) {
        boolean z;
        if (getAdvertContent(type) == null && !updateAdvertContent(type, new ArrayList<>())) {
            z = false;
        } else {
            ArrayList<ContentItem> contentItemArray = getAdvertContent(type);
            if (contentItemArray.size() > 0) {
                contentItemArray.set(index, contentItem);
            } else {
                contentItemArray.add(index, contentItem);
            }
            notifyChange(getUriFor("content/advert/" + type.getPolicy() + MqttTopic.TOPIC_LEVEL_SEPARATOR + index));
            z = true;
        }
        return z;
    }

    public synchronized boolean updateUIDeviceCode(String deviceCode) {
        UserDefineUISetting userDefineUISetting = this.cache.get().getUserDefineUISetting();
        if (userDefineUISetting == null) {
            userDefineUISetting = new UserDefineUISetting();
        }
        userDefineUISetting.setDeviceCode(deviceCode);
        this.cache.get().setUserDefineUISetting(userDefineUISetting);
        notifyChange(getUriFor("content/deviceCode"));
        return true;
    }

    public synchronized String getUIDeviceCode() {
        UserDefineUISetting userDefineUISetting;
        userDefineUISetting = this.cache.get().getUserDefineUISetting();
        return userDefineUISetting != null ? userDefineUISetting.getDeviceCode() : null;
    }

    public synchronized String getProtocolTimezone() {
        return this.cache.get().getProtocolTimezone();
    }

    public synchronized boolean updateProtocolTimezone(String tz) {
        String oldTimezone = null;
        if (!TextUtils.isEmpty(this.cache.get().getProtocolTimezone())) {
            String oldTimezone2 = new String(this.cache.get().getProtocolTimezone());
            oldTimezone = oldTimezone2;
        }
        this.cache.get().setProtocolTimezone(tz);
        if (!tz.equals(oldTimezone)) {
            notifyChange(getUriFor("protocolTimezone"));
        }
        return true;
    }

    public synchronized CountrySetting getCountrySetting() {
        return this.cache.get().getCountrySetting();
    }

    public synchronized boolean updateCountrySetting(CountrySetting setting) {
        this.cache.get().setCountrySetting(setting);
        notifyChange(getUriFor("countrySetting"));
        return true;
    }
}
