package com.xcharge.charger.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import java.util.List;

/* loaded from: classes.dex */
public class MultiProtocolCharger extends Application {
    public static final String ACTION_TERMINATE_APP = "com.xcharge.charger.application.ACTION_TERMINATE_APP";
    public static final String CLASS_BOOT_SERVICE = "com.xcharge.charger.boot.service.BootService";
    public static final String processName = "com.xcharge.charger";
    private APPMessageReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class APPMessageReceiver extends BroadcastReceiver {
        private APPMessageReceiver() {
        }

        /* synthetic */ APPMessageReceiver(MultiProtocolCharger multiProtocolCharger, APPMessageReceiver aPPMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MultiProtocolCharger.ACTION_TERMINATE_APP)) {
                MultiProtocolCharger.this.destroy();
                System.exit(0);
            }
        }
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        String processName2 = getProcessName(this, Process.myPid());
        if (!TextUtils.isEmpty(processName2) && processName.equals(processName2)) {
            init();
        }
    }

    @Override // android.app.Application
    public void onTerminate() {
        destroy();
        Log.w("MultiProtocolCharger.onTerminate", "onTerminate");
        LogUtils.syslog("application com.xcharge.charger onTerminate");
        super.onTerminate();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onLowMemory() {
        Log.w("MultiProtocolCharger.onLowMemory", "onLowMemory");
        LogUtils.syslog("application com.xcharge.charger onLowMemory");
        super.onLowMemory();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
        Log.w("MultiProtocolCharger.onTrimMemory", "onTrimMemory: " + level);
        LogUtils.syslog("application com.xcharge.charger onTrimMemory: " + level);
        super.onTrimMemory(level);
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        Log.w("MultiProtocolCharger.onConfigurationChanged", "newConfig: " + JsonBean.getGsonBuilder().create().toJson(newConfig));
        LogUtils.syslog("application com.xcharge.charger onConfigurationChanged: " + JsonBean.getGsonBuilder().create().toJson(newConfig));
        CountrySettingCacheProvider.getInstance().setAppLang(this);
        super.onConfigurationChanged(newConfig);
    }

    private void init() {
        this.receiver = new APPMessageReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TERMINATE_APP);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.receiver, filter);
        startBootService();
        logAllAppsInSystem();
    }

    public void destroy() {
        stopBootService();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
    }

    private String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(UIEventMessage.TYPE_UI_ACTIVITY);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    private void startBootService() {
        try {
            Intent intent = new Intent(this, Class.forName(CLASS_BOOT_SERVICE));
            startService(intent);
        } catch (ClassNotFoundException e) {
            Log.e("MultiProtocolCharger.startBootService", Log.getStackTraceString(e));
        }
    }

    private void stopBootService() {
        try {
            Intent intent = new Intent(this, Class.forName(CLASS_BOOT_SERVICE));
            stopService(intent);
        } catch (ClassNotFoundException e) {
            Log.e("MultiProtocolCharger.stopBootService", Log.getStackTraceString(e));
        }
    }

    private void logAllAppsInSystem() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(UIEventMessage.TYPE_UI_ACTIVITY);
            List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : infoList) {
                String processName2 = info.processName;
                int pid = info.pid;
                int importance = info.importance;
                LogUtils.syslog("processName: " + processName2 + ",  pid: " + pid + ",  importance: " + importance);
            }
        } catch (Exception e) {
            Log.e("MultiProtocolCharger.logAllAppsInSystem", Log.getStackTraceString(e));
        }
    }
}