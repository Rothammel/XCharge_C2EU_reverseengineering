package com.xcharge.charger.device.p005c2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.p000v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.device.api.DeviceServiceProxy;
import com.xcharge.charger.device.network.NetworkController;
import com.xcharge.charger.device.p005c2.nfc.C2NFCAgent;
import com.xcharge.common.utils.FtpUtils;
import com.xcharge.common.utils.HttpDownloadManager;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.device.c2.service.C2DeviceService */
public class C2DeviceService extends Service {
    public static final String ACTION_DEVICE_EVENT = "android.intent.chargerhd.EVENT";
    public static final int MSG_CANCEL_U1_BIND = 8194;
    public static final int MSG_DEVICE_EVENT = 8193;
    private DeviceMessageReceiver deviceMessageReceiver = null;
    /* access modifiers changed from: private */
    public DeviceEventHandler handler = null;
    private LocalDeviceMessageReceiver localDeviceMessageReceiver = null;
    private NetworkStatusObserver networkStatusObserver = null;

    /* renamed from: com.xcharge.charger.device.c2.service.C2DeviceService$DeviceMessageReceiver */
    private class DeviceMessageReceiver extends BroadcastReceiver {
        private DeviceMessageReceiver() {
        }

        /* synthetic */ DeviceMessageReceiver(C2DeviceService c2DeviceService, DeviceMessageReceiver deviceMessageReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(C2DeviceService.ACTION_DEVICE_EVENT)) {
                C2DeviceService.this.handler.sendMessage(C2DeviceService.this.handler.obtainMessage(C2DeviceService.MSG_DEVICE_EVENT, intent.getStringExtra("event")));
            }
        }
    }

    /* renamed from: com.xcharge.charger.device.c2.service.C2DeviceService$LocalDeviceMessageReceiver */
    private class LocalDeviceMessageReceiver extends BroadcastReceiver {
        private LocalDeviceMessageReceiver() {
        }

        /* synthetic */ LocalDeviceMessageReceiver(C2DeviceService c2DeviceService, LocalDeviceMessageReceiver localDeviceMessageReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DCAPProxy.ACTION_CANCEL_U1_BIND_EVENT)) {
                C2DeviceService.this.handler.sendMessage(C2DeviceService.this.handler.obtainMessage(8194, intent.getStringExtra(ContentDB.ChargeTable.PORT)));
            }
        }
    }

    /* renamed from: com.xcharge.charger.device.c2.service.C2DeviceService$DeviceEventHandler */
    private class DeviceEventHandler extends Handler {
        private DeviceEventHandler() {
        }

        /* synthetic */ DeviceEventHandler(C2DeviceService c2DeviceService, DeviceEventHandler deviceEventHandler) {
            this();
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r7) {
            /*
                r6 = this;
                int r3 = r7.what     // Catch:{ Exception -> 0x0015 }
                switch(r3) {
                    case 8193: goto L_0x0009;
                    case 8194: goto L_0x0045;
                    case 135169: goto L_0x005b;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r7)
                return
            L_0x0009:
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r4 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x0015 }
                java.lang.Object r3 = r7.obj     // Catch:{ Exception -> 0x0015 }
                java.lang.String r3 = (java.lang.String) r3     // Catch:{ Exception -> 0x0015 }
                r4.dispatchEvent(r6, r3)     // Catch:{ Exception -> 0x0015 }
                goto L_0x0005
            L_0x0015:
                r0 = move-exception
                java.lang.String r3 = "C2DeviceService.handleMessage"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                java.lang.String r5 = "except: "
                r4.<init>(r5)
                java.lang.String r5 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r4 = r4.append(r5)
                java.lang.String r4 = r4.toString()
                android.util.Log.e(r3, r4)
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "C2DeviceService handleMessage exception: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                com.xcharge.common.utils.LogUtils.syslog(r3)
                goto L_0x0005
            L_0x0045:
                java.lang.Object r1 = r7.obj     // Catch:{ Exception -> 0x0015 }
                java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x0015 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r3 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r1)     // Catch:{ Exception -> 0x0015 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r4 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r1)     // Catch:{ Exception -> 0x0015 }
                r5 = 24582(0x6006, float:3.4447E-41)
                android.os.Message r4 = r4.obtainMessage(r5)     // Catch:{ Exception -> 0x0015 }
                r3.sendMessage(r4)     // Catch:{ Exception -> 0x0015 }
                goto L_0x0005
            L_0x005b:
                java.lang.Object r2 = r7.obj     // Catch:{ Exception -> 0x0015 }
                android.net.Uri r2 = (android.net.Uri) r2     // Catch:{ Exception -> 0x0015 }
                com.xcharge.charger.device.c2.service.C2DeviceService r3 = com.xcharge.charger.device.p005c2.service.C2DeviceService.this     // Catch:{ Exception -> 0x0015 }
                r3.handleNetworkStatusChanged(r2)     // Catch:{ Exception -> 0x0015 }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.p005c2.service.C2DeviceService.DeviceEventHandler.handleMessage(android.os.Message):void");
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        init();
        DeviceServiceProxy.getInstance().sendDeviceServiceEvent("created");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        destroy();
        super.onDestroy();
        DeviceServiceProxy.getInstance().sendDeviceServiceEvent("destroyed");
    }

    private void init() {
        Context context = getApplicationContext();
        System.loadLibrary("chargerhd_jni");
        if (CHARGE_PLATFORM.cddz.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            System.loadLibrary("EchargenetJni");
        }
        DeviceServiceProxy.getInstance().init(context);
        C2DeviceProxy.getInstance().init(context);
        initDeviceCache(context);
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                C2NFCAgent.getInstance(port).init(context);
            }
        }
        C2DeviceEventDispatcher.getInstance().init(context);
        this.handler = new DeviceEventHandler(this, (DeviceEventHandler) null);
        this.networkStatusObserver = new NetworkStatusObserver(context, this.handler);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        NetworkController.getInstance().init(context);
        this.deviceMessageReceiver = new DeviceMessageReceiver(this, (DeviceMessageReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEVICE_EVENT);
        registerReceiver(this.deviceMessageReceiver, filter);
        this.localDeviceMessageReceiver = new LocalDeviceMessageReceiver(this, (LocalDeviceMessageReceiver) null);
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(DCAPProxy.ACTION_CANCEL_U1_BIND_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.localDeviceMessageReceiver, localFilter);
    }

    private void destroy() {
        unregisterReceiver(this.deviceMessageReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.localDeviceMessageReceiver);
        NetworkController.getInstance().destroy();
        getContentResolver().unregisterContentObserver(this.networkStatusObserver);
        this.handler.removeMessages(MSG_DEVICE_EVENT);
        this.handler.removeMessages(135169);
        C2DeviceEventDispatcher.getInstance().destroy();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                C2NFCAgent.getInstance(port).destroy();
            }
        }
        destroyDeviceCache();
        C2DeviceProxy.getInstance().destroy();
        DeviceServiceProxy.getInstance().destroy();
    }

    private void initDeviceCache(Context context) {
        HardwareStatusCacheProvider.getInstance().init(context, C2DeviceProxy.getInstance().getHardwareStatus());
        SoftwareStatusCacheProvider.getInstance().init(context, C2DeviceProxy.getInstance().getSoftwareStatus());
        C2DeviceProxy.getInstance().initSystemSettingPortsCache();
        C2DeviceProxy.getInstance().initLocalSettingPortsCache();
        C2DeviceProxy.getInstance().initRemoteSettingPortsCache();
        ChargeStatusCacheProvider.getInstance().init(context, C2DeviceProxy.getInstance().initChargeStatus());
    }

    private void destroyDeviceCache() {
        ChargeStatusCacheProvider.getInstance().destroy();
        SoftwareStatusCacheProvider.getInstance().destroy();
        HardwareStatusCacheProvider.getInstance().destroy();
    }

    /* access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("C2DeviceService.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        Intent connIntent = new Intent(Network.ACTION_CONNECTION_CHANGED);
        String lastSegment = uri.getLastPathSegment();
        if ("connected".equals(lastSegment)) {
            connIntent.putExtra("connected", true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(connIntent);
            C2DeviceEventDispatcher.getInstance().handleNetworkStatus(true);
            adjustDataCommunicationThreadPool();
        } else if ("disconnected".equals(lastSegment)) {
            connIntent.putExtra("connected", false);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(connIntent);
            C2DeviceEventDispatcher.getInstance().handleNetworkStatus(false);
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Network.ACTION_NETWORK_CHANGED));
    }

    private void adjustDataCommunicationThreadPool() {
        String active = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
        if (Network.NETWORK_TYPE_MOBILE.equals(active)) {
            String mobile = HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getType();
            if ("2G".equals(mobile)) {
                HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(1);
                FtpUtils.changeTransferCoreThreadNum(1);
            } else if ("3G".equals(mobile)) {
                HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(1);
                FtpUtils.changeTransferCoreThreadNum(1);
            } else if ("4G".equals(mobile)) {
                String basebandVersion = HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getBasebandSV();
                if ("0".equals(basebandVersion) || "rv2".equals(basebandVersion) || "EC122".equals(basebandVersion)) {
                    HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(1);
                    FtpUtils.changeTransferCoreThreadNum(1);
                    return;
                }
                HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(3);
                FtpUtils.changeTransferCoreThreadNum(3);
            }
        } else if (Network.NETWORK_TYPE_ETHERNET.equals(active)) {
            HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(3);
            FtpUtils.changeTransferCoreThreadNum(3);
        } else if (Network.NETWORK_TYPE_WIFI.equals(active)) {
            HttpDownloadManager.getInstance().changeDownloadCoreThreadNum(3);
            FtpUtils.changeTransferCoreThreadNum(3);
        }
    }
}
