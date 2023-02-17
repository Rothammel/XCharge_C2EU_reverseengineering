package com.xcharge.charger.device.c2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.device.api.DeviceServiceProxy;
import com.xcharge.charger.device.c2.nfc.C2NFCAgent;
import com.xcharge.charger.device.network.NetworkController;
import com.xcharge.common.utils.FtpUtils;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import java.util.HashMap;

/* loaded from: classes.dex */
public class C2DeviceService extends Service {
    public static final String ACTION_DEVICE_EVENT = "android.intent.chargerhd.EVENT";
    public static final int MSG_CANCEL_U1_BIND = 8194;
    public static final int MSG_DEVICE_EVENT = 8193;
    private DeviceEventHandler handler = null;
    private DeviceMessageReceiver deviceMessageReceiver = null;
    private LocalDeviceMessageReceiver localDeviceMessageReceiver = null;
    private NetworkStatusObserver networkStatusObserver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DeviceMessageReceiver extends BroadcastReceiver {
        private DeviceMessageReceiver() {
        }

        /* synthetic */ DeviceMessageReceiver(C2DeviceService c2DeviceService, DeviceMessageReceiver deviceMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(C2DeviceService.ACTION_DEVICE_EVENT)) {
                String event = intent.getStringExtra("event");
                C2DeviceService.this.handler.sendMessage(C2DeviceService.this.handler.obtainMessage(C2DeviceService.MSG_DEVICE_EVENT, event));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class LocalDeviceMessageReceiver extends BroadcastReceiver {
        private LocalDeviceMessageReceiver() {
        }

        /* synthetic */ LocalDeviceMessageReceiver(C2DeviceService c2DeviceService, LocalDeviceMessageReceiver localDeviceMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPProxy.ACTION_CANCEL_U1_BIND_EVENT)) {
                String port = intent.getStringExtra(ContentDB.ChargeTable.PORT);
                C2DeviceService.this.handler.sendMessage(C2DeviceService.this.handler.obtainMessage(8194, port));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DeviceEventHandler extends Handler {
        private DeviceEventHandler() {
        }

        /* synthetic */ DeviceEventHandler(C2DeviceService c2DeviceService, DeviceEventHandler deviceEventHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case C2DeviceService.MSG_DEVICE_EVENT /* 8193 */:
                        C2DeviceEventDispatcher.getInstance().dispatchEvent(this, (String) msg.obj);
                        break;
                    case 8194:
                        String port = (String) msg.obj;
                        C2NFCAgent.getInstance(port).sendMessage(C2NFCAgent.getInstance(port).obtainMessage(C2NFCAgent.MSG_NFC_CANCEL_U1_BIND));
                        break;
                    case 135169:
                        Uri uri = (Uri) msg.obj;
                        C2DeviceService.this.handleNetworkStatusChanged(uri);
                        break;
                }
            } catch (Exception e) {
                Log.e("C2DeviceService.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("C2DeviceService handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        init();
        DeviceServiceProxy.getInstance().sendDeviceServiceEvent("created");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        destroy();
        super.onDestroy();
        DeviceServiceProxy.getInstance().sendDeviceServiceEvent("destroyed");
    }

    private void init() {
        Context context = getApplicationContext();
        System.loadLibrary("chargerhd_jni");
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.cddz.equals(platform)) {
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
        this.handler = new DeviceEventHandler(this, null);
        this.networkStatusObserver = new NetworkStatusObserver(context, this.handler);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        NetworkController.getInstance().init(context);
        this.deviceMessageReceiver = new DeviceMessageReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEVICE_EVENT);
        registerReceiver(this.deviceMessageReceiver, filter);
        this.localDeviceMessageReceiver = new LocalDeviceMessageReceiver(this, null);
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

    /* JADX INFO: Access modifiers changed from: private */
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