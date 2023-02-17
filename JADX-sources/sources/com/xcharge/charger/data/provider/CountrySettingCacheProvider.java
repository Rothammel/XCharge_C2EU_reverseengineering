package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.data.R;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class CountrySettingCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.setting";
    private static final String configFileName = "country_cfg.json";
    private static CountrySettingCacheProvider instance = null;
    private Context context = null;
    private String rootPath = null;
    private ContentResolver resolver = null;
    private AtomicReference<CountrySetting> cache = new AtomicReference<>(new CountrySetting());

    public static CountrySettingCacheProvider getInstance() {
        if (instance == null) {
            instance = new CountrySettingCacheProvider();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.rootPath = "country";
        this.resolver = this.context.getContentResolver();
        CountrySetting setting = loadSetting();
        if (setting != null) {
            this.cache.set(setting);
        }
    }

    public void destroy() {
    }

    public Uri getUriFor(String subPath) {
        String path = this.rootPath;
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.setting/" + path);
    }

    private void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, null);
    }

    private CountrySetting loadSetting() {
        CountrySetting countrySetting = null;
        try {
            String cfg = ContextUtils.readFileData(configFileName, this.context);
            if (TextUtils.isEmpty(cfg)) {
                String list = ContextUtils.getRawFileToString(this.context, R.raw.country_list);
                String country = null;
                HashMap<String, String> pdata = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
                if (pdata != null) {
                    String country2 = pdata.get("country");
                    country = country2;
                }
                if (country == null) {
                    country = SystemSettingCacheProvider.getInstance().getCountry();
                } else {
                    SystemSettingCacheProvider.getInstance().setCountry(country);
                }
                HashMap<String, CountrySetting> hashMap = (HashMap) JsonBean.getGsonBuilder().create().fromJson(list, new TypeToken<HashMap<String, CountrySetting>>() { // from class: com.xcharge.charger.data.provider.CountrySettingCacheProvider.1
                }.getType());
                countrySetting = hashMap.get(country);
            } else {
                countrySetting = new CountrySetting().fromJson(cfg);
            }
        } catch (Exception e) {
            Log.w("CountrySettingCacheProvider.loadSetting", Log.getStackTraceString(e));
        }
        if (countrySetting == null) {
            CountrySetting countrySetting2 = new CountrySetting();
            return countrySetting2;
        }
        return countrySetting;
    }

    public synchronized CountrySetting getCountrySetting() {
        return this.cache.get();
    }

    public synchronized void updateCountrySetting(CountrySetting setting) {
        this.cache.set(setting);
    }

    public synchronized void persist() {
        ContextUtils.writeFileData(configFileName, this.cache.get().toJson(), this.context);
    }

    public synchronized void persist(CountrySetting setting) {
        this.cache.set(setting);
        persist();
    }

    public synchronized String getZone() {
        return this.cache.get().getZone();
    }

    public synchronized boolean updateZone(String tz) {
        this.cache.get().setZone(tz);
        notifyChange(getUriFor("zone"));
        return true;
    }

    public synchronized boolean isUseDaylightTime() {
        return this.cache.get().isUseDaylightTime();
    }

    public synchronized boolean updateUseDaylightTime(boolean use) {
        this.cache.get().setUseDaylightTime(use);
        notifyChange(getUriFor("useDaylightTime"));
        return true;
    }

    public synchronized String getLang() {
        return this.cache.get().getLang();
    }

    public synchronized boolean updateLang(String lang) {
        this.cache.get().setLang(lang);
        notifyChange(getUriFor("lang"));
        return true;
    }

    public synchronized String getMoney() {
        return this.cache.get().getMoney();
    }

    public synchronized boolean updateMoney(String money) {
        this.cache.get().setMoney(money);
        notifyChange(getUriFor("money"));
        return true;
    }

    public synchronized String getMoneyDisp() {
        return this.cache.get().getMoneyDisp();
    }

    public synchronized boolean updateMoneyDisp(String disp) {
        this.cache.get().setMoneyDisp(disp);
        notifyChange(getUriFor("moneyDisp"));
        return true;
    }

    public void setAppLang(Context context) {
        try {
            String lang = getLang();
            Configuration config = context.getResources().getConfiguration();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            if ("en".equals(lang)) {
                config.locale = Locale.ENGLISH;
            } else if ("zh".equals(lang)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            } else if ("de".equals(lang)) {
                config.locale = Locale.GERMANY;
            } else if ("iw".equals(lang)) {
                config.locale = new Locale("iw");
            } else {
                config.locale = Locale.ENGLISH;
            }
            context.getResources().updateConfiguration(config, dm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String format(String format, Object... args) {
        String lang = getLang();
        Locale l = Locale.ENGLISH;
        if ("zh".equals(lang)) {
            l = Locale.SIMPLIFIED_CHINESE;
        } else if ("de".equals(lang)) {
            l = Locale.GERMANY;
        }
        return String.format(l, format, args);
    }

    public boolean isSetRTL() {
        String lang = getLang();
        return "iw".equals(lang);
    }
}
