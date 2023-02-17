package com.xcharge.charger.boot.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.application.MultiProtocolCharger;
import com.xcharge.charger.boot.handler.BootHandler;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.service.DCAPService;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.AuthInfoProxy;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.IDGeneratorContentProxy;
import com.xcharge.charger.data.proxy.NFCConsumeFailCacheContentProxy;
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.device.api.DeviceServiceProxy;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.charger.p006ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.p006ui.adapter.type.HOME_UI_STAGE;
import com.xcharge.charger.p006ui.adapter.type.UI_MODE;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.test.service.TestService;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.SystemPropertiesProxy;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class BootService extends Service {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM = null;
    public static final int MSG_BOOT_APP_INIT_END = 12293;
    public static final int MSG_BOOT_BEGIN = 12290;
    public static final int MSG_BOOT_CORE_MODULE = 12294;
    public static final int MSG_BOOT_CORE_MODULE_EXIT = 12305;
    public static final int MSG_BOOT_DEVICE_MODULE = 12291;
    public static final int MSG_BOOT_DEVICE_MODULE_EXIT = 12297;
    public static final int MSG_BOOT_END = 12296;
    public static final int MSG_BOOT_PROTOCOL_MODULE = 12295;
    public static final int MSG_BOOT_PROTOCOL_MODULE_EXIT = 12306;
    public static final int MSG_BOOT_REBOOT = 12307;
    public static final int MSG_BOOT_UI_MODULE = 12292;
    public static final int MSG_BOOT_UI_MODULE_EXIT = 12304;
    public static final int MSG_MONITOR_PROTOCOL_MODULE = 12308;
    public static final int MSG_MONITOR_PROTOCOL_MODULE_EXIT = 12309;
    public static final int MSG_UI_UPDATE_QRCODE_REQUEST = 12320;
    ErrorCode bootError = new ErrorCode(200);
    private BootMessageReceiver bootMessageReceiver = null;
    /* access modifiers changed from: private */
    public LinkedList<String> bootedServices = null;
    /* access modifiers changed from: private */
    public CHARGE_PLATFORM chargePlatform = null;
    /* access modifiers changed from: private */
    public String deviceServiceClass = null;
    /* access modifiers changed from: private */
    public BootMainHandler mainHandler = null;
    /* access modifiers changed from: private */
    public String protocolMonitorServiceClass = "com.xcharge.charger.protocol.monitor.service.MonitorCloudService";
    /* access modifiers changed from: private */
    public String protocolServiceClass = null;
    /* access modifiers changed from: private */
    public BootHandler subHandler = null;
    /* access modifiers changed from: private */
    public String uiServiceClass = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM;
        if (iArr == null) {
            iArr = new int[CHARGE_PLATFORM.values().length];
            try {
                iArr[CHARGE_PLATFORM.anyo.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_PLATFORM.cddz.ordinal()] = 8;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_PLATFORM.ecw.ordinal()] = 6;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_PLATFORM.ocpp.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_PLATFORM.ptne.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_PLATFORM.xcharge.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_PLATFORM.xconsole.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_PLATFORM.xmsz.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_PLATFORM.yzx.ordinal()] = 7;
            } catch (NoSuchFieldError e9) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM = iArr;
        }
        return iArr;
    }

    private class OldVerNFCFeePolicy extends JsonBean<OldVerNFCFeePolicy> {
        public static final int TIME_PRICE_SIZE = 6;
        private long createTime;
        private String currencyType;

        /* renamed from: id */
        private long f43id;
        private ArrayList<ArrayList<Integer>> timedPrice;
        private long updateTime;

        private OldVerNFCFeePolicy() {
            this.f43id = 0;
            this.currencyType = null;
            this.timedPrice = null;
            this.createTime = 0;
            this.updateTime = 0;
        }

        /* synthetic */ OldVerNFCFeePolicy(BootService bootService, OldVerNFCFeePolicy oldVerNFCFeePolicy) {
            this();
        }

        public long getId() {
            return this.f43id;
        }

        public void setId(long id) {
            this.f43id = id;
        }

        public String getCurrencyType() {
            return this.currencyType;
        }

        public void setCurrencyType(String currencyType2) {
            this.currencyType = currencyType2;
        }

        public ArrayList<ArrayList<Integer>> getTimedPrice() {
            return this.timedPrice;
        }

        public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice2) {
            this.timedPrice = timedPrice2;
        }

        public long getCreateTime() {
            return this.createTime;
        }

        public void setCreateTime(long createTime2) {
            this.createTime = createTime2;
        }

        public long getUpdateTime() {
            return this.updateTime;
        }

        public void setUpdateTime(long updateTime2) {
            this.updateTime = updateTime2;
        }
    }

    private class BootMessageReceiver extends BroadcastReceiver {
        private BootMessageReceiver() {
        }

        /* synthetic */ BootMessageReceiver(BootService bootService, BootMessageReceiver bootMessageReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DeviceServiceProxy.ACTION_DEVICE_SERIVCE_EVENT)) {
                String event = intent.getStringExtra("event");
                if ("created".equals(event)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(12291));
                } else if ("destroyed".equals(event)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_DEVICE_MODULE_EXIT));
                }
            } else if (action.equals(UIServiceProxy.ACTION_UI_SERIVCE_EVENT)) {
                String event2 = intent.getStringExtra("event");
                if (UIServiceProxy.UI_SERIVCE_EVENT_CREATED.equals(event2)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_UI_MODULE));
                } else if (UIServiceProxy.UI_SERIVCE_EVENT_DESTROYED.equals(event2)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_UI_MODULE_EXIT));
                } else if (UIServiceProxy.UI_SERIVCE_EVENT_UPDATE_QRCODE.equals(event2)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_UI_UPDATE_QRCODE_REQUEST));
                }
            } else if (action.equals(DCAPProxy.ACTION_DCAP_SERIVCE_EVENT)) {
                String event3 = intent.getStringExtra("event");
                if ("created".equals(event3)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_CORE_MODULE));
                } else if ("destroyed".equals(event3)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_CORE_MODULE_EXIT));
                }
            } else if (action.equals(ProtocolServiceProxy.ACTION_PROTOCOL_SERIVCE_EVENT)) {
                String event4 = intent.getStringExtra("event");
                if ("created".equals(event4)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_PROTOCOL_MODULE));
                } else if ("destroyed".equals(event4)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_PROTOCOL_MODULE_EXIT));
                } else if (ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_CREATED.equals(event4)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_MONITOR_PROTOCOL_MODULE));
                } else if (ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_DESTROYED.equals(event4)) {
                    BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_MONITOR_PROTOCOL_MODULE_EXIT));
                }
            } else if (action.equals(Network.ACTION_CONNECTION_CHANGED)) {
                BootService.this.subHandler.sendMessage(BootService.this.subHandler.obtainMessage(BootHandler.MSG_NETWORK_CONNECTION_CHANGED, Boolean.valueOf(intent.getBooleanExtra("connected", false))));
            } else if (action.equals(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT)) {
                BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_REBOOT));
            }
        }
    }

    private class BootMainHandler extends Handler {
        private BootMainHandler() {
        }

        /* synthetic */ BootMainHandler(BootService bootService, BootMainHandler bootMainHandler) {
            this();
        }

        public void handleMessage(Message msg) {
            Bundle data;
            try {
                switch (msg.what) {
                    case BootService.MSG_BOOT_BEGIN /*12290*/:
                        Log.i("BootService.handleMessage", "boot handle begin !!!");
                        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                            BootService.this.migrateLocalSettingFromOldVer();
                        }
                        BootService.this.bootedServices = new LinkedList();
                        BootService.this.deviceServiceClass = SystemSettingCacheProvider.getInstance().getDeviceServiceClass();
                        BootService.this.startService(BootService.this.deviceServiceClass);
                        break;
                    case 12291:
                        Log.i("BootService.handleMessage", "device module boot OK !!!");
                        LogUtils.applog("service " + BootService.this.deviceServiceClass + " started");
                        HardwareStatusCacheProvider.getInstance().getHardwareStatus().setBootTimestamp(System.currentTimeMillis());
                        BootService.this.bootedServices.addFirst(BootService.this.deviceServiceClass);
                        BootService.this.uiServiceClass = SystemSettingCacheProvider.getInstance().getUiServiceClass();
                        if (TextUtils.isEmpty(BootService.this.uiServiceClass)) {
                            BootService.this.subHandler.sendEmptyMessage(BootHandler.MSG_BOOT_APP_INIT);
                            ChargeStatusCacheProvider.getInstance().updateAdvertEnabled(false);
                            break;
                        } else {
                            BootService.this.startService(BootService.this.uiServiceClass);
                            break;
                        }
                    case BootService.MSG_BOOT_UI_MODULE /*12292*/:
                        Log.i("BootService.handleMessage", "ui module boot OK !!!");
                        LogUtils.applog("service " + BootService.this.uiServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.uiServiceClass);
                        data = new Bundle();
                        try {
                            data.putString("stage", HOME_UI_STAGE.booting.getStage());
                            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.home, data);
                            BootService.this.subHandler.sendEmptyMessage(BootHandler.MSG_BOOT_APP_INIT);
                            ChargeStatusCacheProvider.getInstance().updateAdvertEnabled(true);
                            break;
                        } catch (Exception e) {
                            e = e;
                            Bundle bundle = data;
                            break;
                        }
                    case BootService.MSG_BOOT_APP_INIT_END /*12293*/:
                        String error = (String) msg.obj;
                        Log.i("BootService.handleMessage", "app init end: " + error);
                        BootService.this.bootError = (ErrorCode) new ErrorCode().fromJson(error);
                        if (BootService.this.bootError.getCode() != 200) {
                            Log.w("BootService.handleMessage", "failed to boot !!!");
                            BootService.this.handleBootError(BootService.this.bootError);
                            if (BootService.this.bootError.getCode() < 30010 || BootService.this.bootError.getCode() > 30018) {
                                if (!BootService.this.isUIModuleActive()) {
                                    Log.e("BootService.handleMessage", "can not resume boot error, and no ui, exit app !!!");
                                    BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                                    break;
                                }
                            } else {
                                Log.w("BootService.handleMessage", "can resume boot error, boot continue !!!");
                                BootService.this.startDCAPService();
                                break;
                            }
                        } else {
                            BootService.this.startDCAPService();
                            break;
                        }
                        break;
                    case BootService.MSG_BOOT_CORE_MODULE /*12294*/:
                        Log.i("BootService.handleMessage", "core module boot OK !!!");
                        LogUtils.applog("service " + DCAPService.class.getName() + " started");
                        BootService.this.bootedServices.addFirst(DCAPService.class.getName());
                        BootService.this.startProtocolModule();
                        if (TextUtils.isEmpty(BootService.this.protocolServiceClass)) {
                            BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                            break;
                        }
                        break;
                    case BootService.MSG_BOOT_PROTOCOL_MODULE /*12295*/:
                        Log.i("BootService.handleMessage", "protocol module boot OK !!! service class: " + BootService.this.protocolServiceClass);
                        LogUtils.applog("service " + BootService.this.protocolServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.protocolServiceClass);
                        boolean isYZXMonitor = SystemSettingCacheProvider.getInstance().getSystemSetting().isYZXMonitor();
                        if (BootService.this.chargePlatform != null && BootService.this.chargePlatform != CHARGE_PLATFORM.yzx && isYZXMonitor) {
                            BootService.this.startService(BootService.this.protocolMonitorServiceClass);
                            break;
                        } else {
                            BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                            break;
                        }
                    case BootService.MSG_BOOT_END /*12296*/:
                        Log.i("BootService.handleMessage", "app booted !!!");
                        Log.d("BootService.handleMessage", "system setting: " + SystemSettingCacheProvider.getInstance().getSystemSetting().toJson());
                        Log.d("BootService.handleMessage", "country setting: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
                        Log.d("BootService.handleMessage", String.valueOf(!LocalSettingCacheProvider.getInstance().hasLocalSetting() ? "no local setting in config file, using default " : "") + "local setting: " + LocalSettingCacheProvider.getInstance().getLocalSetting().toJson());
                        Log.d("BootService.handleMessage", String.valueOf(!RemoteSettingCacheProvider.getInstance().hasRemoteSetting() ? "no remote setting in config file, using default " : "") + "remote setting: " + RemoteSettingCacheProvider.getInstance().getRemoteSetting().toJson());
                        Log.d("BootService.handleMessage", "hardware status: " + HardwareStatusCacheProvider.getInstance().getHardwareStatus().toJson());
                        Log.d("BootService.handleMessage", "software status: " + SoftwareStatusCacheProvider.getInstance().getSoftwareStatus().toJson());
                        Log.d("BootService.handleMessage", "charge status: " + ChargeStatusCacheProvider.getInstance().getChargeStatus().toJson());
                        LogUtils.applog("system booted !!!");
                        LogUtils.applog("system setting: " + SystemSettingCacheProvider.getInstance().getSystemSetting().toJson());
                        LogUtils.applog("country setting: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
                        LogUtils.applog(String.valueOf(!LocalSettingCacheProvider.getInstance().hasLocalSetting() ? "no local setting in config file, using default " : "") + "local setting: " + LocalSettingCacheProvider.getInstance().getLocalSetting().toJson());
                        LogUtils.applog(String.valueOf(!RemoteSettingCacheProvider.getInstance().hasRemoteSetting() ? "no remote setting in config file, using default " : "") + "remote setting: " + RemoteSettingCacheProvider.getInstance().getRemoteSetting().toJson());
                        LogUtils.applog("hardware status: " + HardwareStatusCacheProvider.getInstance().getHardwareStatus().toJson());
                        LogUtils.applog("software status: " + SoftwareStatusCacheProvider.getInstance().getSoftwareStatus().toJson());
                        LogUtils.applog("charge status: " + ChargeStatusCacheProvider.getInstance().getChargeStatus().toJson());
                        List<XKeyseed> allSettedNFCKeyseed = NFCKeyContentProxy.getInstance().getAllKeyseed();
                        if (allSettedNFCKeyseed != null) {
                            Log.d("BootService.handleMessage", "all setted nfc keyseed: " + JsonBean.listToJson(allSettedNFCKeyseed));
                            LogUtils.applog("all setted nfc keyseed: " + JsonBean.listToJson(allSettedNFCKeyseed));
                        }
                        if (BootService.this.bootError.getCode() == 200) {
                            DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.idle.getStatus());
                            if (BootService.this.isUIModuleActive()) {
                                data = new Bundle();
                                data.putString("stage", HOME_UI_STAGE.normal.getStage());
                                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.home, data);
                                Bundle bundle2 = data;
                            }
                        }
                        for (String port : HardwareStatusCacheProvider.getInstance().getPorts().keySet()) {
                            C2DeviceProxy.getInstance().notifyPortStatusUpdatedByCmd(C2DeviceProxy.getInstance().getPortRuntimeStatus(port));
                        }
                        break;
                    case BootService.MSG_BOOT_DEVICE_MODULE_EXIT /*12297*/:
                        Log.i("BootService.handleMessage", "device module exit, and stop boot service self !!!");
                        LogUtils.applog("service " + BootService.this.deviceServiceClass + " exited");
                        if (BootService.this.bootedServices.size() > 0) {
                            Log.w("BootService.handleMessage", "unnormal exit: " + BootService.this.bootedServices.toString());
                        }
                        BootService.this.stopSelf();
                        LocalBroadcastManager.getInstance(BootService.this.getApplicationContext()).sendBroadcast(new Intent(MultiProtocolCharger.ACTION_TERMINATE_APP));
                        LogUtils.syslog("system will reboot !!!");
                        ((PowerManager) BootService.this.getSystemService(ChargeStopCondition.TYPE_POWER)).reboot("");
                        break;
                    case BootService.MSG_BOOT_UI_MODULE_EXIT /*12304*/:
                        Log.i("BootService.handleMessage", "ui module exit !!!");
                        LogUtils.applog("service " + BootService.this.uiServiceClass + " exited");
                        BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                        break;
                    case BootService.MSG_BOOT_CORE_MODULE_EXIT /*12305*/:
                        Log.i("BootService.handleMessage", "core module exit !!!");
                        LogUtils.applog("service " + DCAPService.class.getName() + " exited");
                        BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                        break;
                    case BootService.MSG_BOOT_PROTOCOL_MODULE_EXIT /*12306*/:
                        Log.i("BootService.handleMessage", "protocol module exit !!!");
                        LogUtils.applog("service " + BootService.this.protocolServiceClass + " exited");
                        BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                        break;
                    case BootService.MSG_BOOT_REBOOT /*12307*/:
                        Log.i("BootService.handleMessage", "graceful reboot !!!");
                        BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                        break;
                    case BootService.MSG_MONITOR_PROTOCOL_MODULE /*12308*/:
                        Log.i("BootService.handleMessage", "protocol monitor module boot OK !!!");
                        LogUtils.applog("service " + BootService.this.protocolMonitorServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.protocolMonitorServiceClass);
                        BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                        break;
                    case BootService.MSG_MONITOR_PROTOCOL_MODULE_EXIT /*12309*/:
                        Log.i("BootService.handleMessage", "protocol monitor module exit !!!");
                        LogUtils.applog("service " + BootService.this.protocolMonitorServiceClass + " exited");
                        BootService.this.stopService((String) BootService.this.bootedServices.pollFirst());
                        break;
                    case BootService.MSG_UI_UPDATE_QRCODE_REQUEST /*12320*/:
                        ProtocolServiceProxy.getInstance().sentUpdateQrcodeRequestEvent("1");
                        break;
                }
            } catch (Exception e2) {
                e = e2;
            }
            Log.e("BootService.handleMessage", "except: " + Log.getStackTraceString(e));
            LogUtils.syslog("BootService handleMessage exception: " + Log.getStackTraceString(e));
            super.handleMessage(msg);
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        init();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return 1;
    }

    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        initBootMsgHandle(context);
        initSystemSetting(context);
        initAppSettingCache(context);
        initCountySetting(context);
        initContentProxy(context);
        this.mainHandler.sendEmptyMessage(MSG_BOOT_BEGIN);
    }

    private void destroy() {
        while (true) {
            String serviceCls = this.bootedServices.pollFirst();
            if (TextUtils.isEmpty(serviceCls)) {
                destroyBootMsgHandle();
                destroyContentProxy();
                destroyAppSettingCache();
                destroyCountySetting();
                destroySystemSetting();
                return;
            }
            stopService(serviceCls);
        }
    }

    private void initBootMsgHandle(Context context) {
        this.mainHandler = new BootMainHandler(this, (BootMainHandler) null);
        this.subHandler = new BootHandler();
        this.subHandler.init(context, this.mainHandler);
        this.bootMessageReceiver = new BootMessageReceiver(this, (BootMessageReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceServiceProxy.ACTION_DEVICE_SERIVCE_EVENT);
        filter.addAction(UIServiceProxy.ACTION_UI_SERIVCE_EVENT);
        filter.addAction(DCAPProxy.ACTION_DCAP_SERIVCE_EVENT);
        filter.addAction(ProtocolServiceProxy.ACTION_PROTOCOL_SERIVCE_EVENT);
        filter.addAction(Network.ACTION_CONNECTION_CHANGED);
        filter.addAction(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.bootMessageReceiver, filter);
    }

    private void destroyBootMsgHandle() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.bootMessageReceiver);
        this.subHandler.destroy();
        this.mainHandler.removeMessages(MSG_BOOT_BEGIN);
        this.mainHandler.removeMessages(12291);
        this.mainHandler.removeMessages(MSG_BOOT_UI_MODULE);
        this.mainHandler.removeMessages(MSG_BOOT_APP_INIT_END);
        this.mainHandler.removeMessages(MSG_BOOT_CORE_MODULE);
        this.mainHandler.removeMessages(MSG_BOOT_PROTOCOL_MODULE);
        this.mainHandler.removeMessages(MSG_BOOT_END);
        this.mainHandler.removeMessages(MSG_BOOT_DEVICE_MODULE_EXIT);
        this.mainHandler.removeMessages(MSG_BOOT_UI_MODULE_EXIT);
        this.mainHandler.removeMessages(MSG_BOOT_CORE_MODULE_EXIT);
        this.mainHandler.removeMessages(MSG_BOOT_PROTOCOL_MODULE_EXIT);
        this.mainHandler.removeMessages(MSG_BOOT_REBOOT);
    }

    /* access modifiers changed from: private */
    public void startService(String cls) {
        try {
            Log.i("BootService.startService", "start serivce: " + cls);
            startService(new Intent(this, Class.forName(cls)));
        } catch (Exception e) {
            Log.e("BootService.startService", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void stopService(String cls) {
        try {
            Log.i("BootService.stopService", "stop serivce: " + cls);
            stopService(new Intent(this, Class.forName(cls)));
        } catch (Exception e) {
            Log.e("BootService.stopService", Log.getStackTraceString(e));
        }
    }

    private void startTestService() {
        startService(new Intent(this, TestService.class));
    }

    private void stopTestService() {
        stopService(new Intent(this, TestService.class));
    }

    /* access modifiers changed from: private */
    public void startDCAPService() {
        startService(new Intent(this, DCAPService.class));
    }

    private void initContentProxy(Context context) {
        NFCConsumeFailCacheContentProxy.getInstance().init(context);
        NFCConsumeFailCacheContentProxy.getInstance().getConsumeFailCache(NFC_CARD_TYPE.M1, "000000", "000000");
        NFCKeyContentProxy.getInstance().init(context);
        NFCKeyContentProxy.getInstance().getKeyseed("000000", "M1");
        ChargeContentProxy.getInstance().init(context);
        ChargeContentProxy.getInstance().getChargeBill("000000");
        IDGeneratorContentProxy.getInstance().init(context);
        AuthInfoProxy.getInstance().init(context);
    }

    private void destroyContentProxy() {
        NFCConsumeFailCacheContentProxy.getInstance().destroy();
        NFCKeyContentProxy.getInstance().destroy();
        ChargeContentProxy.getInstance().destroy();
    }

    private void initAppSettingCache(Context context) {
        LocalSettingCacheProvider.getInstance().init(context);
        RemoteSettingCacheProvider.getInstance().init(context);
        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (RemoteSettingCacheProvider.getInstance().getCountrySetting() == null) {
                CountrySetting defaultCountrySetting = new CountrySetting();
                defaultCountrySetting.setZone("+08:00");
                defaultCountrySetting.setUseDaylightTime(false);
                defaultCountrySetting.setLang("zh");
                defaultCountrySetting.setMoney("CNY");
                defaultCountrySetting.setMoneyDisp("元");
                RemoteSettingCacheProvider.getInstance().updateCountrySetting(defaultCountrySetting);
            }
            CountrySettingCacheProvider.getInstance().updateCountrySetting(RemoteSettingCacheProvider.getInstance().getCountrySetting().clone());
        }
    }

    private void destroyAppSettingCache() {
        LocalSettingCacheProvider.getInstance().destroy();
        RemoteSettingCacheProvider.getInstance().destroy();
    }

    private void initSystemSetting(Context context) {
        SystemSettingCacheProvider.getInstance().init(context);
        CountrySettingCacheProvider.getInstance().init(context);
        initChargePlatformType();
        initChargePlatformCustomizedData();
        initDNSCache();
        initAutoTimeSetting(context);
        initScreenBrightSetting(context);
    }

    private void destroySystemSetting() {
        SystemSettingCacheProvider.getInstance().destroy();
    }

    private void initCountySetting(Context context) {
        TimeUtils.setSystemCountry(SystemSettingCacheProvider.getInstance().getCountry().toUpperCase());
        String zone = CountrySettingCacheProvider.getInstance().getZone();
        boolean useDST = CountrySettingCacheProvider.getInstance().isUseDaylightTime();
        String zoneId = TimeUtils.getTimezoneId(zone, useDST);
        if (TextUtils.isEmpty(zoneId)) {
            Log.w("BootService.initCountySetting", "unavailable id for timezone: " + zone);
            return;
        }
        ((AlarmManager) getSystemService("alarm")).setTimeZone(zoneId);
        boolean realUseDST = TimeZone.getTimeZone(zoneId).useDaylightTime();
        if (realUseDST != useDST) {
            CountrySettingCacheProvider.getInstance().updateUseDaylightTime(realUseDST);
        }
        Log.i("BootService.initCountySetting", "set timezone: " + zone + " using id: " + zoneId + ", useDST: " + realUseDST);
        LogUtils.syslog("set timezone: " + zone + " using id: " + zoneId + ", useDST: " + realUseDST);
    }

    private void destroyCountySetting() {
        CountrySettingCacheProvider.getInstance().destroy();
    }

    private void initChargePlatformCustomizedData() {
        HashMap<String, String> platformData = null;
        try {
            String pdata = SystemPropertiesProxy.get(getApplicationContext(), "ro.boot.pdata");
            if (!TextUtils.isEmpty(pdata)) {
                Log.i("BootService.initChargePlatformCustomizedData", "pdata: " + pdata);
                platformData = parseKVData(pdata);
            }
            if (platformData != null) {
                SystemSettingCacheProvider.getInstance().updatePlatformCustomizedData(platformData);
            }
        } catch (Exception e) {
            Log.w("BootService.initChargePlatformCustomizedData", Log.getStackTraceString(e));
        }
    }

    private CHARGE_PLATFORM getChargePlatformFromSysProp() {
        CHARGE_PLATFORM platform = null;
        try {
            String ptype = SystemPropertiesProxy.get(getApplicationContext(), "ro.boot.ptype");
            if (!TextUtils.isEmpty(ptype)) {
                Log.i("BootService.getChargePlatformFromSysProp", "ptype: " + ptype);
                platform = CHARGE_PLATFORM.valueOf(ptype);
            }
        } catch (Exception e) {
            Log.w("BootService.getChargePlatformFromSysProp", Log.getStackTraceString(e));
        }
        if (platform == null) {
            return CHARGE_PLATFORM.xcharge;
        }
        return platform;
    }

    private void initChargePlatformType() {
        CHARGE_PLATFORM platform = null;
        try {
            platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (platform == null) {
                String ptype = SystemPropertiesProxy.get(getApplicationContext(), "ro.boot.ptype");
                if (!TextUtils.isEmpty(ptype)) {
                    Log.i("BootService.initChargePlatformType", "ptype: " + ptype);
                    platform = CHARGE_PLATFORM.valueOf(ptype);
                }
            }
        } catch (Exception e) {
            Log.w("BootService.initChargePlatformType", Log.getStackTraceString(e));
        }
        String sn = SystemPropertiesProxy.get(getApplicationContext(), "ro.boot.serialno");
        String exceptAnyoPiles = ContextUtils.getRawFileToString(getApplicationContext(), C0221R.raw.except_anyo_piles);
        if (!TextUtils.isEmpty(sn) && !TextUtils.isEmpty(exceptAnyoPiles)) {
            Log.d("BootService.initChargePlatformType", "sn: " + sn + ", exceptAnyoPiles: " + exceptAnyoPiles);
            String anyoPileId = (String) ((Map) JsonBean.getGsonBuilder().create().fromJson(exceptAnyoPiles, new TypeToken<Map>() {
            }.getType())).get(sn);
            if (!TextUtils.isEmpty(anyoPileId)) {
                Log.i("BootService.initChargePlatformType", "this is an except anyo pile, type: 1, id:" + anyoPileId);
                HashMap<String, String> anyoPileData = new HashMap<>();
                anyoPileData.put("type", "1");
                anyoPileData.put("id", anyoPileId);
                SystemSettingCacheProvider.getInstance().updatePlatformCustomizedData(anyoPileData);
                platform = CHARGE_PLATFORM.anyo;
            }
        }
        if (platform == null) {
            platform = CHARGE_PLATFORM.xcharge;
        }
        SystemSettingCacheProvider.getInstance().updatetChargePlatform(platform);
    }

    private void initDNSCache() {
        Security.setProperty("networkaddress.cache.ttl", String.valueOf(SystemSettingCacheProvider.getInstance().getDnsOkCacheTime()));
        Security.setProperty("networkaddress.cache.negative.ttl", String.valueOf(SystemSettingCacheProvider.getInstance().getDnsFailCacheTime()));
    }

    private void initAutoTimeSetting(Context context) {
        int i = 1;
        Settings.Global.putInt(context.getContentResolver(), "auto_time", SystemSettingCacheProvider.getInstance().getEnableAutoTime() ? 1 : 0);
        ContentResolver contentResolver = context.getContentResolver();
        if (!SystemSettingCacheProvider.getInstance().getEnableAutoZone()) {
            i = 0;
        }
        Settings.Global.putInt(contentResolver, "auto_time_zone", i);
    }

    private void initScreenBrightSetting(Context context) {
        Settings.System.putInt(context.getContentResolver(), "screen_brightness", SystemSettingCacheProvider.getInstance().getScreenBrightMode());
    }

    private void initMobileDataRoamingSetting(Context context) {
        try {
            Log.d("BootService.initMobileDataRoamingSetting", "data_roaming:" + Settings.System.getInt(context.getContentResolver(), "data_roaming"));
            Settings.System.putInt(context.getContentResolver(), "data_roaming", SystemSettingCacheProvider.getInstance().isMobileRoaming() ? 1 : 0);
            Log.d("BootService.initMobileDataRoamingSetting", "data_roaming:" + Settings.System.getInt(context.getContentResolver(), "data_roaming"));
        } catch (Exception e) {
            Log.e("BootService.initMobileDataRoamingSetting", Log.getStackTraceString(e));
        }
    }

    private void setConnectionSamplingInterval(Context context, int interval) {
        Settings.Global.putInt(context.getContentResolver(), "connectivity_sampling_interval_in_seconds", interval);
    }

    private void initSystemMemOptimizeSetting(Context context) {
        try {
            boolean is = SystemPropertiesProxy.getBoolean(context, "sys.mem.opt", false).booleanValue();
            Log.d("BootService.initSystemMemOptimizeSetting", "sys.mem.opt: " + is);
            if (is) {
                SystemPropertiesProxy.set(context, "sys.mem.opt", "false");
                boolean isSetted = SystemPropertiesProxy.getBoolean(context, "sys.mem.opt", true).booleanValue();
                Log.d("BootService.initSystemMemOptimizeSetting", "sys.mem.opt: " + isSetted);
                if (!isSetted) {
                    FileUtils.killProcess("zygote");
                }
            }
        } catch (Exception e) {
            Log.e("BootService.initSystemMemOptimizeSetting", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public boolean isUIModuleActive() {
        return !TextUtils.isEmpty(this.uiServiceClass) && this.bootedServices.contains(this.uiServiceClass);
    }

    /* access modifiers changed from: private */
    public void handleBootError(ErrorCode error) {
        if (isUIModuleActive()) {
            Bundle data = new Bundle();
            data.putString("stage", HOME_UI_STAGE.boot_error.getStage());
            data.putString("error", error.toJson());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.home, data);
            return;
        }
        DeviceProxy.getInstance().beep(3);
    }

    /* access modifiers changed from: private */
    public void startProtocolModule() {
        this.chargePlatform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM()[this.chargePlatform.ordinal()]) {
            case 1:
                this.protocolServiceClass = "com.xcharge.charger.protocol.family.xcloud.service.XCloudService";
                break;
            case 2:
                this.protocolServiceClass = "com.xcharge.charger.protocol.xconsole.service.XConsoleService";
                break;
            case 3:
                this.protocolServiceClass = "com.xcharge.charger.protocol.anyo.service.AnyoCloudService";
                break;
            case 4:
                this.protocolServiceClass = "com.xcharge.charger.protocol.xmsz.service.XMSZCloudService";
                break;
            case 5:
                this.protocolServiceClass = "com.xcharge.charger.protocol.ptne.service.PTNECloudService";
                break;
            case 9:
                this.protocolServiceClass = "com.xcharge.charger.protocol.ocpp.service.OcppCloudService";
                break;
            default:
                ErrorCode errorCode = new ErrorCode();
                errorCode.setCode(ErrorCode.EC_UNAVAILABLE_PLATFORM_SETTING);
                handleBootError(errorCode);
                break;
        }
        if (!TextUtils.isEmpty(this.protocolServiceClass)) {
            startService(this.protocolServiceClass);
        }
    }

    private HashMap<String, String> parseKVData(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        HashMap<String, String> kvMap = new HashMap<>();
        for (String kvPair : data.split(";")) {
            String[] kv = kvPair.split("=");
            if (kv.length != 2) {
                return null;
            }
            kvMap.put(kv[0], kv[1]);
        }
        return kvMap;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x016e, code lost:
        r22 = (com.xcharge.charger.boot.service.BootService.OldVerNFCFeePolicy) new com.xcharge.charger.boot.service.BootService.OldVerNFCFeePolicy(r38, (com.xcharge.charger.boot.service.BootService.OldVerNFCFeePolicy) null).fromJson(r20);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void migrateLocalSettingFromOldVer() {
        /*
            r38 = this;
            java.io.File r21 = new java.io.File     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "/data/tmp/com.atsmart.xcharger/shared_prefs"
            r0 = r21
            r1 = r34
            r0.<init>(r1)     // Catch:{ Exception -> 0x0248 }
            boolean r34 = r21.exists()     // Catch:{ Exception -> 0x0248 }
            if (r34 == 0) goto L_0x0017
            boolean r34 = r21.isDirectory()     // Catch:{ Exception -> 0x0248 }
            if (r34 != 0) goto L_0x0018
        L_0x0017:
            return
        L_0x0018:
            java.lang.String r34 = "try to migrate local setting from V1 APP to V2 APP !!!"
            com.xcharge.common.utils.LogUtils.applog(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r15 = "/data/data/com.xcharge.charger/shared_prefs"
            java.io.File r16 = new java.io.File     // Catch:{ Exception -> 0x0248 }
            r0 = r16
            r0.<init>(r15)     // Catch:{ Exception -> 0x0248 }
            boolean r34 = r16.exists()     // Catch:{ Exception -> 0x0248 }
            if (r34 != 0) goto L_0x0040
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "mkdir -p "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            java.lang.StringBuilder r34 = r0.append(r15)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
        L_0x0040:
            java.lang.String r28 = "/data/tmp/com.atsmart.xcharger/shared_prefs/work.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "cp -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = " "
            java.lang.StringBuilder r34 = r34.append(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            java.lang.StringBuilder r34 = r0.append(r15)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r28 = "/data/tmp/com.atsmart.xcharger/shared_prefs/nfc_setting.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "cp -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = " "
            java.lang.StringBuilder r34 = r34.append(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            java.lang.StringBuilder r34 = r0.append(r15)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r28 = "/data/tmp/com.atsmart.xcharger/shared_prefs/property.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "cp -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = " "
            java.lang.StringBuilder r34 = r34.append(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            java.lang.StringBuilder r34 = r0.append(r15)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "BootService.migrateLocalSettingFromOldVer"
            java.lang.String r35 = "migrate v1.0 local setting data to v2.0 !!!"
            android.util.Log.i(r34, r35)     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.provider.LocalSettingCacheProvider r34 = com.xcharge.charger.data.provider.LocalSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.bean.setting.LocalSetting r13 = r34.getLocalSetting()     // Catch:{ Exception -> 0x0248 }
            r14 = 0
            java.lang.String r34 = "work"
            r35 = 0
            r0 = r38
            r1 = r34
            r2 = r35
            android.content.SharedPreferences r33 = r0.getSharedPreferences(r1, r2)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "work_mode"
            r35 = 0
            java.lang.String r32 = r33.getString(r34, r35)     // Catch:{ Exception -> 0x0248 }
            boolean r34 = android.text.TextUtils.isEmpty(r32)     // Catch:{ Exception -> 0x0248 }
            if (r34 != 0) goto L_0x00fc
            com.xcharge.charger.data.bean.setting.ChargeSetting r34 = r13.getChargeSetting()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.bean.type.WORK_MODE r35 = com.xcharge.charger.data.bean.type.WORK_MODE.valueBy(r32)     // Catch:{ Exception -> 0x0248 }
            r34.setWorkMode(r35)     // Catch:{ Exception -> 0x0248 }
            r14 = 1
            java.lang.String r34 = "BootService.migrateLocalSettingFromOldVer"
            java.lang.StringBuilder r35 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r36 = "migrate local work mode: "
            r35.<init>(r36)     // Catch:{ Exception -> 0x0248 }
            r0 = r35
            r1 = r32
            java.lang.StringBuilder r35 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = r35.toString()     // Catch:{ Exception -> 0x0248 }
            android.util.Log.i(r34, r35)     // Catch:{ Exception -> 0x0248 }
        L_0x00fc:
            java.lang.String r34 = "nfc_setting"
            r35 = 0
            r0 = r38
            r1 = r34
            r2 = r35
            android.content.SharedPreferences r19 = r0.getSharedPreferences(r1, r2)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "setting"
            r35 = 0
            r0 = r19
            r1 = r34
            r2 = r35
            java.lang.String r17 = r0.getString(r1, r2)     // Catch:{ Exception -> 0x0248 }
            boolean r34 = android.text.TextUtils.isEmpty(r17)     // Catch:{ Exception -> 0x0248 }
            if (r34 != 0) goto L_0x014c
            org.json.JSONObject r18 = new org.json.JSONObject     // Catch:{ Exception -> 0x0248 }
            r0 = r18
            r1 = r17
            r0.<init>(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "content"
            r0 = r18
            r1 = r34
            org.json.JSONObject r4 = r0.optJSONObject(r1)     // Catch:{ Exception -> 0x0248 }
            if (r4 == 0) goto L_0x014c
            java.lang.String r34 = "welcome"
            r0 = r34
            org.json.JSONArray r31 = r4.optJSONArray(r0)     // Catch:{ Exception -> 0x0248 }
            if (r31 == 0) goto L_0x014c
            int r34 = r31.length()     // Catch:{ Exception -> 0x0248 }
            if (r34 <= 0) goto L_0x014c
            r11 = 0
        L_0x0144:
            int r34 = r31.length()     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            if (r11 < r0) goto L_0x0254
        L_0x014c:
            java.lang.String r34 = "property"
            r35 = 0
            r0 = r38
            r1 = r34
            r2 = r35
            android.content.SharedPreferences r27 = r0.getSharedPreferences(r1, r2)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "nfc_stratege"
            r35 = 0
            r0 = r27
            r1 = r34
            r2 = r35
            java.lang.String r20 = r0.getString(r1, r2)     // Catch:{ Exception -> 0x0248 }
            boolean r34 = android.text.TextUtils.isEmpty(r20)     // Catch:{ Exception -> 0x0248 }
            if (r34 != 0) goto L_0x01ec
            com.xcharge.charger.boot.service.BootService$OldVerNFCFeePolicy r34 = new com.xcharge.charger.boot.service.BootService$OldVerNFCFeePolicy     // Catch:{ Exception -> 0x0248 }
            r35 = 0
            r0 = r34
            r1 = r38
            r2 = r35
            r0.<init>(r1, r2)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r20
            java.lang.Object r22 = r0.fromJson(r1)     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.boot.service.BootService$OldVerNFCFeePolicy r22 = (com.xcharge.charger.boot.service.BootService.OldVerNFCFeePolicy) r22     // Catch:{ Exception -> 0x0248 }
            java.util.ArrayList r26 = r22.getTimedPrice()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.core.api.DCAPProxy r34 = com.xcharge.charger.core.api.DCAPProxy.getInstance()     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r26
            com.xcharge.charger.data.bean.FeeRate r7 = r0.formatFeeRate(r1)     // Catch:{ Exception -> 0x0248 }
            if (r7 == 0) goto L_0x01ec
            com.xcharge.charger.data.bean.setting.FeeRateSetting r9 = r13.getFeeRateSetting()     // Catch:{ Exception -> 0x0248 }
            if (r9 != 0) goto L_0x01a2
            com.xcharge.charger.data.bean.setting.FeeRateSetting r9 = new com.xcharge.charger.data.bean.setting.FeeRateSetting     // Catch:{ Exception -> 0x0248 }
            r9.<init>()     // Catch:{ Exception -> 0x0248 }
        L_0x01a2:
            java.util.HashMap r25 = r9.getPortsFeeRate()     // Catch:{ Exception -> 0x0248 }
            if (r25 != 0) goto L_0x01bd
            java.util.HashMap r25 = new java.util.HashMap     // Catch:{ Exception -> 0x0248 }
            r25.<init>()     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "1"
            com.xcharge.charger.data.bean.PortFeeRate r35 = new com.xcharge.charger.data.bean.PortFeeRate     // Catch:{ Exception -> 0x0248 }
            r35.<init>()     // Catch:{ Exception -> 0x0248 }
            r0 = r25
            r1 = r34
            r2 = r35
            r0.put(r1, r2)     // Catch:{ Exception -> 0x0248 }
        L_0x01bd:
            java.util.Set r34 = r25.entrySet()     // Catch:{ Exception -> 0x0248 }
            java.util.Iterator r34 = r34.iterator()     // Catch:{ Exception -> 0x0248 }
        L_0x01c5:
            boolean r35 = r34.hasNext()     // Catch:{ Exception -> 0x0248 }
            if (r35 != 0) goto L_0x02a6
            r0 = r25
            r9.setPortsFeeRate(r0)     // Catch:{ Exception -> 0x0248 }
            r13.setFeeRateSetting(r9)     // Catch:{ Exception -> 0x0248 }
            r14 = 1
            java.lang.String r34 = "BootService.migrateLocalSettingFromOldVer"
            java.lang.StringBuilder r35 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r36 = "migrate local fee rate: "
            r35.<init>(r36)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r36 = r9.toJson()     // Catch:{ Exception -> 0x0248 }
            java.lang.StringBuilder r35 = r35.append(r36)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = r35.toString()     // Catch:{ Exception -> 0x0248 }
            android.util.Log.i(r34, r35)     // Catch:{ Exception -> 0x0248 }
        L_0x01ec:
            if (r14 == 0) goto L_0x01fe
            com.xcharge.charger.data.provider.LocalSettingCacheProvider r34 = com.xcharge.charger.data.provider.LocalSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r0.PersistSetting(r13)     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.provider.LocalSettingCacheProvider r34 = com.xcharge.charger.data.provider.LocalSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x0248 }
            r34.loadSetting()     // Catch:{ Exception -> 0x0248 }
        L_0x01fe:
            java.lang.String r28 = "/data/data/com.xcharge.charger/shared_prefs/work.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "rm -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r28 = "/data/data/com.xcharge.charger/shared_prefs/nfc_setting.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "rm -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r28 = "/data/data/com.xcharge.charger/shared_prefs/property.xml"
            java.lang.StringBuilder r34 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = "rm -rf "
            r34.<init>(r35)     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r28
            java.lang.StringBuilder r34 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = r34.toString()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.common.utils.FileUtils.execShell(r34)     // Catch:{ Exception -> 0x0248 }
            goto L_0x0017
        L_0x0248:
            r5 = move-exception
            java.lang.String r34 = "BootService.migrateLocalSettingFromOldVer"
            java.lang.String r35 = android.util.Log.getStackTraceString(r5)
            android.util.Log.w(r34, r35)
            goto L_0x0017
        L_0x0254:
            r0 = r31
            org.json.JSONObject r12 = r0.getJSONObject(r11)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r34 = "text"
            r0 = r34
            boolean r34 = r12.has(r0)     // Catch:{ Exception -> 0x0248 }
            if (r34 == 0) goto L_0x02a2
            java.lang.String r34 = "text"
            r0 = r34
            java.lang.String r30 = r12.getString(r0)     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.bean.setting.UserDefineUISetting r29 = r13.getUserDefineUISetting()     // Catch:{ Exception -> 0x0248 }
            if (r29 != 0) goto L_0x027c
            com.xcharge.charger.data.bean.setting.UserDefineUISetting r29 = new com.xcharge.charger.data.bean.setting.UserDefineUISetting     // Catch:{ Exception -> 0x0248 }
            r29.<init>()     // Catch:{ Exception -> 0x0248 }
            r0 = r29
            r13.setUserDefineUISetting(r0)     // Catch:{ Exception -> 0x0248 }
        L_0x027c:
            com.xcharge.charger.data.bean.setting.UserDefineUISetting r34 = r13.getUserDefineUISetting()     // Catch:{ Exception -> 0x0248 }
            r0 = r34
            r1 = r30
            r0.setWelcome(r1)     // Catch:{ Exception -> 0x0248 }
            r14 = 1
            java.lang.String r34 = "BootService.migrateLocalSettingFromOldVer"
            java.lang.StringBuilder r35 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0248 }
            java.lang.String r36 = "migrate local welcome: "
            r35.<init>(r36)     // Catch:{ Exception -> 0x0248 }
            r0 = r35
            r1 = r30
            java.lang.StringBuilder r35 = r0.append(r1)     // Catch:{ Exception -> 0x0248 }
            java.lang.String r35 = r35.toString()     // Catch:{ Exception -> 0x0248 }
            android.util.Log.i(r34, r35)     // Catch:{ Exception -> 0x0248 }
            goto L_0x014c
        L_0x02a2:
            int r11 = r11 + 1
            goto L_0x0144
        L_0x02a6:
            java.lang.Object r6 = r34.next()     // Catch:{ Exception -> 0x0248 }
            java.util.Map$Entry r6 = (java.util.Map.Entry) r6     // Catch:{ Exception -> 0x0248 }
            java.lang.Object r24 = r6.getKey()     // Catch:{ Exception -> 0x0248 }
            java.lang.String r24 = (java.lang.String) r24     // Catch:{ Exception -> 0x0248 }
            java.lang.Object r23 = r6.getValue()     // Catch:{ Exception -> 0x0248 }
            com.xcharge.charger.data.bean.PortFeeRate r23 = (com.xcharge.charger.data.bean.PortFeeRate) r23     // Catch:{ Exception -> 0x0248 }
            java.util.HashMap r10 = r23.getFeeRates()     // Catch:{ Exception -> 0x0248 }
            if (r10 != 0) goto L_0x02c3
            java.util.HashMap r10 = new java.util.HashMap     // Catch:{ Exception -> 0x0248 }
            r10.<init>()     // Catch:{ Exception -> 0x0248 }
        L_0x02c3:
            long r36 = r22.getId()     // Catch:{ Exception -> 0x0248 }
            java.lang.String r8 = java.lang.String.valueOf(r36)     // Catch:{ Exception -> 0x0248 }
            r7.setFeeRateId(r8)     // Catch:{ Exception -> 0x0248 }
            r10.put(r8, r7)     // Catch:{ Exception -> 0x0248 }
            r0 = r23
            r0.setFeeRates(r10)     // Catch:{ Exception -> 0x0248 }
            r0 = r23
            r0.setActiveFeeRateId(r8)     // Catch:{ Exception -> 0x0248 }
            goto L_0x01c5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.boot.service.BootService.migrateLocalSettingFromOldVer():void");
    }

    private void applyAnyoDefaultConfig() {
        if (!CHARGE_PLATFORM.anyo.equals(getChargePlatformFromSysProp())) {
            return;
        }
        if (new File("/data/data/com.xcharge.charger/shared_prefs/com.xcharge.charger.data.provider.setting.xml").exists()) {
            Log.d("BootService.applyAnyoDefaultConfig", "exist config file, not apply default config and resources");
            return;
        }
        Log.i("BootService.applyAnyoDefaultConfig", "not exist config file, apply default config and resources !!!");
        File cfg_file_path = new File("/data/data/com.xcharge.charger/shared_prefs");
        if (!cfg_file_path.exists()) {
            cfg_file_path.mkdirs();
        }
        this.subHandler.initAdvertPath();
        if (ContextUtils.readAssetsFileTo(getApplicationContext(), "anyo/com.xcharge.charger.data.provider.setting.xml", "/data/data/com.xcharge.charger/shared_prefs/com.xcharge.charger.data.provider.setting.xml")) {
            ContextUtils.readAssetsFileTo(getApplicationContext(), "anyo/scan.jpg", "/data/data/com.xcharge.charger/advert/scanAdvsite/scan.jpg");
            ContextUtils.readAssetsFileTo(getApplicationContext(), "anyo/plugout.jpg", "/data/data/com.xcharge.charger/advert/pullAdvsite/plugout.jpg");
            ContextUtils.readAssetsFileTo(getApplicationContext(), "anyo/wakeup.jpg", "/data/data/com.xcharge.charger/advert/wakeUpAdvsite/wakeup.jpg");
        }
    }
}
