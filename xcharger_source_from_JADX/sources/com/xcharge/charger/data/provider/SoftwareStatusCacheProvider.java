package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.device.Software;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpgrade;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SoftwareStatusCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.device";
    public static final Uri CONTENT_URI = Uri.parse("content://com.xcharge.charger.data.provider.device/software");
    public static final String PATH = "software";
    private static SoftwareStatusCacheProvider instance = null;
    private AtomicReference<Software> cache = new AtomicReference<>(new Software());
    private Context context = null;
    private ContentResolver resolver = null;

    public static SoftwareStatusCacheProvider getInstance() {
        if (instance == null) {
            instance = new SoftwareStatusCacheProvider();
        }
        return instance;
    }

    public void init(Context context2, Software status) {
        this.context = context2;
        this.resolver = this.context.getContentResolver();
        if (status != null) {
            this.cache.set(status);
        }
    }

    public void destroy() {
    }

    public Uri getUriFor(String subPath) {
        String path = PATH;
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.device/" + path);
    }

    private void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, (ContentObserver) null);
    }

    public synchronized Software getSoftwareStatus() {
        return this.cache.get();
    }

    public synchronized String getOs() {
        return this.cache.get().getOs();
    }

    public synchronized boolean updateOs(String os) {
        this.cache.get().setOs(os);
        notifyChange(getUriFor(RequestUpgrade.COM_OS));
        return true;
    }

    public synchronized String getOsVer() {
        return this.cache.get().getOsVer();
    }

    public synchronized boolean updateOsVer(String ver) {
        this.cache.get().setOsVer(ver);
        notifyChange(getUriFor("os/ver"));
        return true;
    }

    public synchronized String getAppVer() {
        return this.cache.get().getAppVer();
    }

    public synchronized boolean updateAppVer(String ver) {
        this.cache.get().setAppVer(ver);
        notifyChange(getUriFor("app/ver"));
        return true;
    }

    public synchronized String getFirewareVer() {
        return this.cache.get().getFirewareVer();
    }

    public synchronized boolean updateFirewareVer(String ver) {
        this.cache.get().setFirewareVer(ver);
        notifyChange(getUriFor("fireware/ver"));
        return true;
    }

    public synchronized UpgradeProgress getUpgradeProgress() {
        return this.cache.get().getUpgradeProgress();
    }

    public synchronized boolean updateUpgradeProgress(UpgradeProgress progress) {
        this.cache.get().setUpgradeProgress(progress);
        notifyChange(getUriFor("upgrade"));
        return true;
    }
}
