package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.common.utils.ContextUtils;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class CountrySettingCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.setting";
    private static final String configFileName = "country_cfg.json";
    private static CountrySettingCacheProvider instance = null;
    private AtomicReference<CountrySetting> cache = new AtomicReference<>(new CountrySetting());
    private Context context = null;
    private ContentResolver resolver = null;
    private String rootPath = null;

    public static CountrySettingCacheProvider getInstance() {
        if (instance == null) {
            instance = new CountrySettingCacheProvider();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
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
        this.resolver.notifyChange(uri, (ContentObserver) null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.xcharge.charger.data.bean.setting.CountrySetting} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.xcharge.charger.data.bean.setting.CountrySetting} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.xcharge.charger.data.bean.setting.CountrySetting loadSetting() {
        /*
            r10 = this;
            r3 = 0
            java.lang.String r8 = "country_cfg.json"
            android.content.Context r9 = r10.context     // Catch:{ Exception -> 0x0063 }
            java.lang.String r1 = com.xcharge.common.utils.ContextUtils.readFileData(r8, r9)     // Catch:{ Exception -> 0x0063 }
            boolean r8 = android.text.TextUtils.isEmpty(r1)     // Catch:{ Exception -> 0x0063 }
            if (r8 == 0) goto L_0x006e
            android.content.Context r8 = r10.context     // Catch:{ Exception -> 0x0063 }
            int r9 = com.xcharge.charger.data.C0228R.raw.country_list     // Catch:{ Exception -> 0x0063 }
            java.lang.String r6 = com.xcharge.common.utils.ContextUtils.getRawFileToString(r8, r9)     // Catch:{ Exception -> 0x0063 }
            r2 = 0
            com.xcharge.charger.data.provider.SystemSettingCacheProvider r8 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0063 }
            java.util.HashMap r7 = r8.getPlatformCustomizedData()     // Catch:{ Exception -> 0x0063 }
            if (r7 == 0) goto L_0x002a
            java.lang.String r8 = "country"
            java.lang.Object r2 = r7.get(r8)     // Catch:{ Exception -> 0x0063 }
            java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0063 }
        L_0x002a:
            if (r2 != 0) goto L_0x005b
            com.xcharge.charger.data.provider.SystemSettingCacheProvider r8 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0063 }
            java.lang.String r2 = r8.getCountry()     // Catch:{ Exception -> 0x0063 }
        L_0x0034:
            com.google.gson.GsonBuilder r8 = com.xcharge.common.bean.JsonBean.getGsonBuilder()     // Catch:{ Exception -> 0x0063 }
            com.google.gson.Gson r8 = r8.create()     // Catch:{ Exception -> 0x0063 }
            com.xcharge.charger.data.provider.CountrySettingCacheProvider$1 r9 = new com.xcharge.charger.data.provider.CountrySettingCacheProvider$1     // Catch:{ Exception -> 0x0063 }
            r9.<init>()     // Catch:{ Exception -> 0x0063 }
            java.lang.reflect.Type r9 = r9.getType()     // Catch:{ Exception -> 0x0063 }
            java.lang.Object r5 = r8.fromJson((java.lang.String) r6, (java.lang.reflect.Type) r9)     // Catch:{ Exception -> 0x0063 }
            java.util.HashMap r5 = (java.util.HashMap) r5     // Catch:{ Exception -> 0x0063 }
            java.lang.Object r8 = r5.get(r2)     // Catch:{ Exception -> 0x0063 }
            r0 = r8
            com.xcharge.charger.data.bean.setting.CountrySetting r0 = (com.xcharge.charger.data.bean.setting.CountrySetting) r0     // Catch:{ Exception -> 0x0063 }
            r3 = r0
        L_0x0053:
            if (r3 != 0) goto L_0x005a
            com.xcharge.charger.data.bean.setting.CountrySetting r3 = new com.xcharge.charger.data.bean.setting.CountrySetting
            r3.<init>()
        L_0x005a:
            return r3
        L_0x005b:
            com.xcharge.charger.data.provider.SystemSettingCacheProvider r8 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0063 }
            r8.setCountry(r2)     // Catch:{ Exception -> 0x0063 }
            goto L_0x0034
        L_0x0063:
            r4 = move-exception
            java.lang.String r8 = "CountrySettingCacheProvider.loadSetting"
            java.lang.String r9 = android.util.Log.getStackTraceString(r4)
            android.util.Log.w(r8, r9)
            goto L_0x0053
        L_0x006e:
            com.xcharge.charger.data.bean.setting.CountrySetting r8 = new com.xcharge.charger.data.bean.setting.CountrySetting     // Catch:{ Exception -> 0x0063 }
            r8.<init>()     // Catch:{ Exception -> 0x0063 }
            java.lang.Object r8 = r8.fromJson(r1)     // Catch:{ Exception -> 0x0063 }
            r0 = r8
            com.xcharge.charger.data.bean.setting.CountrySetting r0 = (com.xcharge.charger.data.bean.setting.CountrySetting) r0     // Catch:{ Exception -> 0x0063 }
            r3 = r0
            goto L_0x0053
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.CountrySettingCacheProvider.loadSetting():com.xcharge.charger.data.bean.setting.CountrySetting");
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

    public void setAppLang(Context context2) {
        try {
            String lang = getLang();
            Configuration config = context2.getResources().getConfiguration();
            DisplayMetrics dm = context2.getResources().getDisplayMetrics();
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
            context2.getResources().updateConfiguration(config, dm);
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
        if ("iw".equals(getLang())) {
            return true;
        }
        return false;
    }
}
