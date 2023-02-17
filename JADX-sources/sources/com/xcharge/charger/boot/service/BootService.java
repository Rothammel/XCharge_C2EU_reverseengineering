package com.xcharge.charger.boot.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.R;
import com.xcharge.charger.application.MultiProtocolCharger;
import com.xcharge.charger.boot.handler.BootHandler;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.service.DCAPService;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.LocalSetting;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.WORK_MODE;
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
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.test.service.TestService;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.type.HOME_UI_STAGE;
import com.xcharge.charger.ui.adapter.type.UI_MODE;
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
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
    private BootMainHandler mainHandler = null;
    private BootMessageReceiver bootMessageReceiver = null;
    private BootHandler subHandler = null;
    private String deviceServiceClass = null;
    private String uiServiceClass = null;
    private CHARGE_PLATFORM chargePlatform = null;
    private String protocolServiceClass = null;
    private String protocolMonitorServiceClass = "com.xcharge.charger.protocol.monitor.service.MonitorCloudService";
    private LinkedList<String> bootedServices = null;
    ErrorCode bootError = new ErrorCode(200);

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM;
        if (iArr == null) {
            iArr = new int[CHARGE_PLATFORM.valuesCustom().length];
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class OldVerNFCFeePolicy extends JsonBean<OldVerNFCFeePolicy> {
        public static final int TIME_PRICE_SIZE = 6;
        private long createTime;
        private String currencyType;
        private long id;
        private ArrayList<ArrayList<Integer>> timedPrice;
        private long updateTime;

        private OldVerNFCFeePolicy() {
            this.id = 0L;
            this.currencyType = null;
            this.timedPrice = null;
            this.createTime = 0L;
            this.updateTime = 0L;
        }

        /* synthetic */ OldVerNFCFeePolicy(BootService bootService, OldVerNFCFeePolicy oldVerNFCFeePolicy) {
            this();
        }

        public long getId() {
            return this.id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getCurrencyType() {
            return this.currencyType;
        }

        public void setCurrencyType(String currencyType) {
            this.currencyType = currencyType;
        }

        public ArrayList<ArrayList<Integer>> getTimedPrice() {
            return this.timedPrice;
        }

        public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice) {
            this.timedPrice = timedPrice;
        }

        public long getCreateTime() {
            return this.createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getUpdateTime() {
            return this.updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class BootMessageReceiver extends BroadcastReceiver {
        private BootMessageReceiver() {
        }

        /* synthetic */ BootMessageReceiver(BootService bootService, BootMessageReceiver bootMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
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
                boolean isConnected = intent.getBooleanExtra("connected", false);
                BootService.this.subHandler.sendMessage(BootService.this.subHandler.obtainMessage(BootHandler.MSG_NETWORK_CONNECTION_CHANGED, Boolean.valueOf(isConnected)));
            } else if (action.equals(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT)) {
                BootService.this.mainHandler.sendMessage(BootService.this.mainHandler.obtainMessage(BootService.MSG_BOOT_REBOOT));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class BootMainHandler extends Handler {
        private BootMainHandler() {
        }

        /* synthetic */ BootMainHandler(BootService bootService, BootMainHandler bootMainHandler) {
            this();
        }

        /* JADX WARN: Not initialized variable reg: 2, insn: 0x015c: MOVE  (r1 I:??[OBJECT, ARRAY]) = (r2 I:??[OBJECT, ARRAY] A[D('data' android.os.Bundle)]), block:B:22:0x015c */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
            } catch (Exception e) {
                e = e;
            }
            try {
                switch (msg.what) {
                    case BootService.MSG_BOOT_BEGIN /* 12290 */:
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
                    case BootService.MSG_BOOT_UI_MODULE /* 12292 */:
                        Log.i("BootService.handleMessage", "ui module boot OK !!!");
                        LogUtils.applog("service " + BootService.this.uiServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.uiServiceClass);
                        Bundle data = new Bundle();
                        data.putString("stage", HOME_UI_STAGE.booting.getStage());
                        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.home, data);
                        BootService.this.subHandler.sendEmptyMessage(BootHandler.MSG_BOOT_APP_INIT);
                        ChargeStatusCacheProvider.getInstance().updateAdvertEnabled(true);
                        break;
                    case BootService.MSG_BOOT_APP_INIT_END /* 12293 */:
                        String error = (String) msg.obj;
                        Log.i("BootService.handleMessage", "app init end: " + error);
                        BootService.this.bootError = new ErrorCode().fromJson(error);
                        if (BootService.this.bootError.getCode() != 200) {
                            Log.w("BootService.handleMessage", "failed to boot !!!");
                            BootService.this.handleBootError(BootService.this.bootError);
                            if (BootService.this.bootError.getCode() >= 30010 && BootService.this.bootError.getCode() <= 30018) {
                                Log.w("BootService.handleMessage", "can resume boot error, boot continue !!!");
                                BootService.this.startDCAPService();
                                break;
                            } else if (!BootService.this.isUIModuleActive()) {
                                Log.e("BootService.handleMessage", "can not resume boot error, and no ui, exit app !!!");
                                String serviceCls = (String) BootService.this.bootedServices.pollFirst();
                                BootService.this.stopService(serviceCls);
                                break;
                            }
                        } else {
                            BootService.this.startDCAPService();
                            break;
                        }
                        break;
                    case BootService.MSG_BOOT_CORE_MODULE /* 12294 */:
                        Log.i("BootService.handleMessage", "core module boot OK !!!");
                        LogUtils.applog("service " + DCAPService.class.getName() + " started");
                        BootService.this.bootedServices.addFirst(DCAPService.class.getName());
                        BootService.this.startProtocolModule();
                        if (TextUtils.isEmpty(BootService.this.protocolServiceClass)) {
                            BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                            break;
                        }
                        break;
                    case BootService.MSG_BOOT_PROTOCOL_MODULE /* 12295 */:
                        Log.i("BootService.handleMessage", "protocol module boot OK !!! service class: " + BootService.this.protocolServiceClass);
                        LogUtils.applog("service " + BootService.this.protocolServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.protocolServiceClass);
                        boolean isYZXMonitor = SystemSettingCacheProvider.getInstance().getSystemSetting().isYZXMonitor();
                        if (BootService.this.chargePlatform == null || BootService.this.chargePlatform == CHARGE_PLATFORM.yzx || !isYZXMonitor) {
                            BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                            break;
                        } else {
                            BootService.this.startService(BootService.this.protocolMonitorServiceClass);
                            break;
                        }
                    case BootService.MSG_BOOT_END /* 12296 */:
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
                            DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.idle.getStatus());
                            if (BootService.this.isUIModuleActive()) {
                                Bundle data2 = new Bundle();
                                data2.putString("stage", HOME_UI_STAGE.normal.getStage());
                                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.home, data2);
                            }
                        }
                        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
                        for (String port : ports.keySet()) {
                            C2DeviceProxy.getInstance().notifyPortStatusUpdatedByCmd(C2DeviceProxy.getInstance().getPortRuntimeStatus(port));
                        }
                        break;
                    case BootService.MSG_BOOT_DEVICE_MODULE_EXIT /* 12297 */:
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
                    case BootService.MSG_BOOT_UI_MODULE_EXIT /* 12304 */:
                        Log.i("BootService.handleMessage", "ui module exit !!!");
                        LogUtils.applog("service " + BootService.this.uiServiceClass + " exited");
                        String serviceCls2 = (String) BootService.this.bootedServices.pollFirst();
                        BootService.this.stopService(serviceCls2);
                        break;
                    case BootService.MSG_BOOT_CORE_MODULE_EXIT /* 12305 */:
                        Log.i("BootService.handleMessage", "core module exit !!!");
                        LogUtils.applog("service " + DCAPService.class.getName() + " exited");
                        String serviceCls3 = (String) BootService.this.bootedServices.pollFirst();
                        BootService.this.stopService(serviceCls3);
                        break;
                    case BootService.MSG_BOOT_PROTOCOL_MODULE_EXIT /* 12306 */:
                        Log.i("BootService.handleMessage", "protocol module exit !!!");
                        LogUtils.applog("service " + BootService.this.protocolServiceClass + " exited");
                        String serviceCls4 = (String) BootService.this.bootedServices.pollFirst();
                        BootService.this.stopService(serviceCls4);
                        break;
                    case BootService.MSG_BOOT_REBOOT /* 12307 */:
                        Log.i("BootService.handleMessage", "graceful reboot !!!");
                        String serviceCls5 = (String) BootService.this.bootedServices.pollFirst();
                        BootService.this.stopService(serviceCls5);
                        break;
                    case BootService.MSG_MONITOR_PROTOCOL_MODULE /* 12308 */:
                        Log.i("BootService.handleMessage", "protocol monitor module boot OK !!!");
                        LogUtils.applog("service " + BootService.this.protocolMonitorServiceClass + " started");
                        BootService.this.bootedServices.addFirst(BootService.this.protocolMonitorServiceClass);
                        BootService.this.mainHandler.sendEmptyMessage(BootService.MSG_BOOT_END);
                        break;
                    case BootService.MSG_MONITOR_PROTOCOL_MODULE_EXIT /* 12309 */:
                        Log.i("BootService.handleMessage", "protocol monitor module exit !!!");
                        LogUtils.applog("service " + BootService.this.protocolMonitorServiceClass + " exited");
                        String serviceCls6 = (String) BootService.this.bootedServices.pollFirst();
                        BootService.this.stopService(serviceCls6);
                        break;
                    case BootService.MSG_UI_UPDATE_QRCODE_REQUEST /* 12320 */:
                        ProtocolServiceProxy.getInstance().sentUpdateQrcodeRequestEvent("1");
                        break;
                }
            } catch (Exception e2) {
                e = e2;
                Log.e("BootService.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("BootService handleMessage exception: " + Log.getStackTraceString(e));
                super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return 1;
    }

    @Override // android.app.Service
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
            if (!TextUtils.isEmpty(serviceCls)) {
                stopService(serviceCls);
            } else {
                destroyBootMsgHandle();
                destroyContentProxy();
                destroyAppSettingCache();
                destroyCountySetting();
                destroySystemSetting();
                return;
            }
        }
    }

    private void initBootMsgHandle(Context context) {
        this.mainHandler = new BootMainHandler(this, null);
        this.subHandler = new BootHandler();
        this.subHandler.init(context, this.mainHandler);
        this.bootMessageReceiver = new BootMessageReceiver(this, null);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void startService(String cls) {
        try {
            Log.i("BootService.startService", "start serivce: " + cls);
            Intent intent = new Intent(this, Class.forName(cls));
            startService(intent);
        } catch (Exception e) {
            Log.e("BootService.startService", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopService(String cls) {
        try {
            Log.i("BootService.stopService", "stop serivce: " + cls);
            Intent intent = new Intent(this, Class.forName(cls));
            stopService(intent);
        } catch (Exception e) {
            Log.e("BootService.stopService", Log.getStackTraceString(e));
        }
    }

    private void startTestService() {
        Intent intent = new Intent(this, TestService.class);
        startService(intent);
    }

    private void stopTestService() {
        Intent intent = new Intent(this, TestService.class);
        stopService(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startDCAPService() {
        Intent intent = new Intent(this, DCAPService.class);
        startService(intent);
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
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            if (RemoteSettingCacheProvider.getInstance().getCountrySetting() == null) {
                CountrySetting defaultCountrySetting = new CountrySetting();
                defaultCountrySetting.setZone("+08:00");
                defaultCountrySetting.setUseDaylightTime(false);
                defaultCountrySetting.setLang("zh");
                defaultCountrySetting.setMoney("CNY");
                defaultCountrySetting.setMoneyDisp("元");
                RemoteSettingCacheProvider.getInstance().updateCountrySetting(defaultCountrySetting);
            }
            CountrySetting countrySetting = RemoteSettingCacheProvider.getInstance().getCountrySetting().m10clone();
            CountrySettingCacheProvider.getInstance().updateCountrySetting(countrySetting);
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
        String country = SystemSettingCacheProvider.getInstance().getCountry();
        TimeUtils.setSystemCountry(country.toUpperCase());
        String zone = CountrySettingCacheProvider.getInstance().getZone();
        boolean useDST = CountrySettingCacheProvider.getInstance().isUseDaylightTime();
        String zoneId = TimeUtils.getTimezoneId(zone, useDST);
        if (TextUtils.isEmpty(zoneId)) {
            Log.w("BootService.initCountySetting", "unavailable id for timezone: " + zone);
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
        alarmManager.setTimeZone(zoneId);
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
        String exceptAnyoPiles = ContextUtils.getRawFileToString(getApplicationContext(), R.raw.except_anyo_piles);
        if (!TextUtils.isEmpty(sn) && !TextUtils.isEmpty(exceptAnyoPiles)) {
            Log.d("BootService.initChargePlatformType", "sn: " + sn + ", exceptAnyoPiles: " + exceptAnyoPiles);
            Map exceptAnyoPileMap = (Map) JsonBean.getGsonBuilder().create().fromJson(exceptAnyoPiles, new TypeToken<Map>() { // from class: com.xcharge.charger.boot.service.BootService.1
            }.getType());
            String anyoPileId = (String) exceptAnyoPileMap.get(sn);
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
        Settings.Global.putInt(context.getContentResolver(), "auto_time", SystemSettingCacheProvider.getInstance().getEnableAutoTime() ? 1 : 0);
        Settings.Global.putInt(context.getContentResolver(), "auto_time_zone", SystemSettingCacheProvider.getInstance().getEnableAutoZone() ? 1 : 0);
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

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isUIModuleActive() {
        return !TextUtils.isEmpty(this.uiServiceClass) && this.bootedServices.contains(this.uiServiceClass);
    }

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
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
            case 6:
            case 7:
            case 8:
            default:
                ErrorCode errorCode = new ErrorCode();
                errorCode.setCode(ErrorCode.EC_UNAVAILABLE_PLATFORM_SETTING);
                handleBootError(errorCode);
                break;
            case 9:
                this.protocolServiceClass = "com.xcharge.charger.protocol.ocpp.service.OcppCloudService";
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
        String[] kvPairs = data.split(";");
        for (String kvPair : kvPairs) {
            String[] kv = kvPair.split("=");
            if (kv.length != 2) {
                return null;
            }
            kvMap.put(kv[0], kv[1]);
        }
        return kvMap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void migrateLocalSettingFromOldVer() {
        JSONArray welcome_content;
        try {
            File oldVerContextPath = new File("/data/tmp/com.atsmart.xcharger/shared_prefs");
            if (oldVerContextPath.exists() && oldVerContextPath.isDirectory()) {
                LogUtils.applog("try to migrate local setting from V1 APP to V2 APP !!!");
                File newVerContextPath = new File("/data/data/com.xcharge.charger/shared_prefs");
                if (!newVerContextPath.exists()) {
                    FileUtils.execShell("mkdir -p /data/data/com.xcharge.charger/shared_prefs");
                }
                FileUtils.execShell("cp -rf /data/tmp/com.atsmart.xcharger/shared_prefs/work.xml" + StringUtils.SPACE + "/data/data/com.xcharge.charger/shared_prefs");
                FileUtils.execShell("cp -rf /data/tmp/com.atsmart.xcharger/shared_prefs/nfc_setting.xml" + StringUtils.SPACE + "/data/data/com.xcharge.charger/shared_prefs");
                FileUtils.execShell("cp -rf /data/tmp/com.atsmart.xcharger/shared_prefs/property.xml" + StringUtils.SPACE + "/data/data/com.xcharge.charger/shared_prefs");
                Log.i("BootService.migrateLocalSettingFromOldVer", "migrate v1.0 local setting data to v2.0 !!!");
                LocalSetting localSetting = LocalSettingCacheProvider.getInstance().getLocalSetting();
                boolean needPersist = false;
                SharedPreferences work_preferences = getSharedPreferences("work", 0);
                String work_mode = work_preferences.getString("work_mode", null);
                if (!TextUtils.isEmpty(work_mode)) {
                    localSetting.getChargeSetting().setWorkMode(WORK_MODE.valueBy(work_mode));
                    needPersist = true;
                    Log.i("BootService.migrateLocalSettingFromOldVer", "migrate local work mode: " + work_mode);
                }
                SharedPreferences nfc_setting_preferences = getSharedPreferences("nfc_setting", 0);
                String nfc_setting = nfc_setting_preferences.getString("setting", null);
                if (!TextUtils.isEmpty(nfc_setting)) {
                    JSONObject nfc_setting_json = new JSONObject(nfc_setting);
                    JSONObject content = nfc_setting_json.optJSONObject("content");
                    if (content != null && (welcome_content = content.optJSONArray("welcome")) != null && welcome_content.length() > 0) {
                        int i = 0;
                        while (true) {
                            if (i >= welcome_content.length()) {
                                break;
                            }
                            JSONObject item = welcome_content.getJSONObject(i);
                            if (!item.has("text")) {
                                i++;
                            } else {
                                String welcome = item.getString("text");
                                UserDefineUISetting userDefineUISetting = localSetting.getUserDefineUISetting();
                                if (userDefineUISetting == null) {
                                    UserDefineUISetting userDefineUISetting2 = new UserDefineUISetting();
                                    localSetting.setUserDefineUISetting(userDefineUISetting2);
                                }
                                localSetting.getUserDefineUISetting().setWelcome(welcome);
                                needPersist = true;
                                Log.i("BootService.migrateLocalSettingFromOldVer", "migrate local welcome: " + welcome);
                            }
                        }
                    }
                }
                SharedPreferences property_preferences = getSharedPreferences("property", 0);
                String nfc_stratege = property_preferences.getString("nfc_stratege", null);
                if (!TextUtils.isEmpty(nfc_stratege)) {
                    OldVerNFCFeePolicy oldVerNFCFeePolicy = new OldVerNFCFeePolicy(this, null).fromJson(nfc_stratege);
                    ArrayList<ArrayList<Integer>> priceSections = oldVerNFCFeePolicy.getTimedPrice();
                    FeeRate feeRate = DCAPProxy.getInstance().formatFeeRate(priceSections);
                    if (feeRate != null) {
                        FeeRateSetting feeRateSetting = localSetting.getFeeRateSetting();
                        if (feeRateSetting == null) {
                            feeRateSetting = new FeeRateSetting();
                        }
                        HashMap<String, PortFeeRate> portsFeeRate = feeRateSetting.getPortsFeeRate();
                        if (portsFeeRate == null) {
                            portsFeeRate = new HashMap<>();
                            portsFeeRate.put("1", new PortFeeRate());
                        }
                        for (Map.Entry<String, PortFeeRate> entry : portsFeeRate.entrySet()) {
                            entry.getKey();
                            PortFeeRate portFeeRate = entry.getValue();
                            HashMap<String, FeeRate> feeRates = portFeeRate.getFeeRates();
                            if (feeRates == null) {
                                feeRates = new HashMap<>();
                            }
                            String feeRateId = String.valueOf(oldVerNFCFeePolicy.getId());
                            feeRate.setFeeRateId(feeRateId);
                            feeRates.put(feeRateId, feeRate);
                            portFeeRate.setFeeRates(feeRates);
                            portFeeRate.setActiveFeeRateId(feeRateId);
                        }
                        feeRateSetting.setPortsFeeRate(portsFeeRate);
                        localSetting.setFeeRateSetting(feeRateSetting);
                        needPersist = true;
                        Log.i("BootService.migrateLocalSettingFromOldVer", "migrate local fee rate: " + feeRateSetting.toJson());
                    }
                }
                if (needPersist) {
                    LocalSettingCacheProvider.getInstance().PersistSetting(localSetting);
                    LocalSettingCacheProvider.getInstance().loadSetting();
                }
                FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/shared_prefs/work.xml");
                FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/shared_prefs/nfc_setting.xml");
                FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/shared_prefs/property.xml");
            }
        } catch (Exception e) {
            Log.w("BootService.migrateLocalSettingFromOldVer", Log.getStackTraceString(e));
        }
    }

    private void applyAnyoDefaultConfig() {
        if (CHARGE_PLATFORM.anyo.equals(getChargePlatformFromSysProp())) {
            File cfg_file = new File("/data/data/com.xcharge.charger/shared_prefs/com.xcharge.charger.data.provider.setting.xml");
            if (cfg_file.exists()) {
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
}
