package com.xcharge.charger.boot.handler;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import com.xcharge.charger.data.bean.setting.RadarSetting;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.CloudTimeSynchObserver;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.common.utils.LogUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class BootHandler {
    public static final int MSG_BOOT_APP_INIT = 20481;
    public static final int MSG_CLEAR_TIMEOUT_BILL = 20483;
    public static final int MSG_NETWORK_CONNECTION_CHANGED = 20482;
    public static final long TIMEOUT_SAVED_BILL = 2592000000L;
    /* access modifiers changed from: private */
    public CloudTimeSynchObserver cloudTimeSynchObserver = null;
    /* access modifiers changed from: private */
    public Context context = null;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public Handler mainHandler = null;
    private HandlerThread thread = null;

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r12) {
            /*
                r11 = this;
                r8 = 200(0xc8, float:2.8E-43)
                int r6 = r12.what     // Catch:{ Exception -> 0x006c }
                switch(r6) {
                    case 20481: goto L_0x000b;
                    case 20482: goto L_0x0121;
                    case 20483: goto L_0x0152;
                    case 143361: goto L_0x0144;
                    default: goto L_0x0007;
                }
            L_0x0007:
                super.handleMessage(r12)
                return
            L_0x000b:
                java.lang.String r6 = "BootHandler.handleMessage"
                java.lang.String r7 = "app init begin !!!"
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.bean.type.CHARGE_PLATFORM r6 = com.xcharge.charger.data.bean.type.CHARGE_PLATFORM.xcharge     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.provider.SystemSettingCacheProvider r7 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.bean.type.CHARGE_PLATFORM r7 = r7.getChargePlatform()     // Catch:{ Exception -> 0x006c }
                boolean r6 = r6.equals(r7)     // Catch:{ Exception -> 0x006c }
                if (r6 == 0) goto L_0x0027
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.migrate()     // Catch:{ Exception -> 0x006c }
            L_0x0027:
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.bean.ErrorCode r3 = r6.appCheck()     // Catch:{ Exception -> 0x006c }
                int r6 = r3.getCode()     // Catch:{ Exception -> 0x006c }
                if (r6 == r8) goto L_0x0043
                int r6 = r3.getCode()     // Catch:{ Exception -> 0x006c }
                r7 = 30010(0x753a, float:4.2053E-41)
                if (r6 < r7) goto L_0x0050
                int r6 = r3.getCode()     // Catch:{ Exception -> 0x006c }
                r7 = 30018(0x7542, float:4.2064E-41)
                if (r6 > r7) goto L_0x0050
            L_0x0043:
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.bean.ErrorCode r1 = r6.appConfig()     // Catch:{ Exception -> 0x006c }
                int r6 = r1.getCode()     // Catch:{ Exception -> 0x006c }
                if (r6 == r8) goto L_0x009d
                r3 = r1
            L_0x0050:
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                android.os.Handler r6 = r6.mainHandler     // Catch:{ Exception -> 0x006c }
                r7 = 12293(0x3005, float:1.7226E-41)
                android.os.Message r0 = r6.obtainMessage(r7)     // Catch:{ Exception -> 0x006c }
                java.lang.String r6 = r3.toJson()     // Catch:{ Exception -> 0x006c }
                r0.obj = r6     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                android.os.Handler r6 = r6.mainHandler     // Catch:{ Exception -> 0x006c }
                r6.sendMessage(r0)     // Catch:{ Exception -> 0x006c }
                goto L_0x0007
            L_0x006c:
                r2 = move-exception
                java.lang.String r6 = "BootHandler.handleMessage"
                java.lang.StringBuilder r7 = new java.lang.StringBuilder
                java.lang.String r8 = "except: "
                r7.<init>(r8)
                java.lang.String r8 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r7 = r7.append(r8)
                java.lang.String r7 = r7.toString()
                android.util.Log.e(r6, r7)
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                java.lang.String r7 = "BootHandler handleMessage exception: "
                r6.<init>(r7)
                java.lang.String r7 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r6 = r6.append(r7)
                java.lang.String r6 = r6.toString()
                com.xcharge.common.utils.LogUtils.syslog(r6)
                goto L_0x0007
            L_0x009d:
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.proxy.CloudTimeSynchObserver r7 = new com.xcharge.charger.data.proxy.CloudTimeSynchObserver     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r8 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                android.content.Context r8 = r8.context     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r9 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler$MsgHandler r9 = r9.handler     // Catch:{ Exception -> 0x006c }
                r7.<init>(r8, r9)     // Catch:{ Exception -> 0x006c }
                r6.cloudTimeSynchObserver = r7     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                android.content.Context r6 = r6.context     // Catch:{ Exception -> 0x006c }
                android.content.ContentResolver r6 = r6.getContentResolver()     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r7 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x006c }
                java.lang.String r8 = "cloud/timeSynch"
                android.net.Uri r7 = r7.getUriFor(r8)     // Catch:{ Exception -> 0x006c }
                r8 = 1
                com.xcharge.charger.boot.handler.BootHandler r9 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.proxy.CloudTimeSynchObserver r9 = r9.cloudTimeSynchObserver     // Catch:{ Exception -> 0x006c }
                r6.registerContentObserver(r7, r8, r9)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r6 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x006c }
                java.util.HashMap r6 = r6.getPorts()     // Catch:{ Exception -> 0x006c }
                java.util.Set r6 = r6.keySet()     // Catch:{ Exception -> 0x006c }
                java.util.Iterator r6 = r6.iterator()     // Catch:{ Exception -> 0x006c }
            L_0x00e1:
                boolean r7 = r6.hasNext()     // Catch:{ Exception -> 0x006c }
                if (r7 != 0) goto L_0x010f
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.endExceptionBills()     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.clearTimeoutBills()     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler$MsgHandler r6 = r6.handler     // Catch:{ Exception -> 0x006c }
                r7 = 20483(0x5003, float:2.8703E-41)
                r8 = 2
                r9 = 0
                r10 = 0
                long r8 = com.xcharge.common.utils.TimeUtils.getTomorrowAt(r8, r9, r10)     // Catch:{ Exception -> 0x006c }
                r6.sendEmptyMessageAtTime(r7, r8)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.initAdvertPath()     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.initDownloadPath()     // Catch:{ Exception -> 0x006c }
                goto L_0x0050
            L_0x010f:
                java.lang.Object r5 = r6.next()     // Catch:{ Exception -> 0x006c }
                java.lang.String r5 = (java.lang.String) r5     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.device.c2.service.C2DeviceProxy r7 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x006c }
                java.lang.String r8 = ""
                java.lang.String r9 = ""
                r7.authInValid(r5, r8, r9)     // Catch:{ Exception -> 0x006c }
                goto L_0x00e1
            L_0x0121:
                java.lang.Object r6 = r12.obj     // Catch:{ Exception -> 0x006c }
                java.lang.Boolean r6 = (java.lang.Boolean) r6     // Catch:{ Exception -> 0x006c }
                boolean r4 = r6.booleanValue()     // Catch:{ Exception -> 0x006c }
                java.lang.String r6 = "BootHandler.handleMessage"
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x006c }
                java.lang.String r8 = "network connection status changed !!! is connected: "
                r7.<init>(r8)     // Catch:{ Exception -> 0x006c }
                java.lang.StringBuilder r7 = r7.append(r4)     // Catch:{ Exception -> 0x006c }
                java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x006c }
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.handleNetworkConnectionStatusChanged(r4)     // Catch:{ Exception -> 0x006c }
                goto L_0x0007
            L_0x0144:
                java.lang.String r6 = "BootHandler.handleMessage"
                java.lang.String r7 = "cloud time synch changed !!!"
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                r6.handleCloudTimeSynch()     // Catch:{ Exception -> 0x006c }
                goto L_0x0007
            L_0x0152:
                java.lang.String r6 = "BootHandler.handleMessage"
                java.lang.String r7 = "clear timeout bills periodically !!!"
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x0172 }
                r6.clearTimeoutBills()     // Catch:{ Exception -> 0x0172 }
            L_0x015e:
                com.xcharge.charger.boot.handler.BootHandler r6 = com.xcharge.charger.boot.handler.BootHandler.this     // Catch:{ Exception -> 0x006c }
                com.xcharge.charger.boot.handler.BootHandler$MsgHandler r6 = r6.handler     // Catch:{ Exception -> 0x006c }
                r7 = 20483(0x5003, float:2.8703E-41)
                r8 = 2
                r9 = 0
                r10 = 0
                long r8 = com.xcharge.common.utils.TimeUtils.getTomorrowAt(r8, r9, r10)     // Catch:{ Exception -> 0x006c }
                r6.sendEmptyMessageAtTime(r7, r8)     // Catch:{ Exception -> 0x006c }
                goto L_0x0007
            L_0x0172:
                r6 = move-exception
                goto L_0x015e
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.boot.handler.BootHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2, Handler mainHandler2) {
        this.context = context2;
        this.mainHandler = mainHandler2;
        this.thread = new HandlerThread("BootSubHandler", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
    }

    public void destroy() {
        if (this.cloudTimeSynchObserver != null) {
            this.context.getContentResolver().unregisterContentObserver(this.cloudTimeSynchObserver);
            this.handler.removeMessages(CloudTimeSynchObserver.MSG_CLOUD_TIME_SYNCHED);
        }
        this.handler.removeMessages(MSG_BOOT_APP_INIT);
        this.handler.removeMessages(MSG_NETWORK_CONNECTION_CHANGED);
        this.handler.removeMessages(MSG_CLEAR_TIMEOUT_BILL);
        this.thread.quit();
    }

    public Message obtainMessage(int what) {
        return this.handler.obtainMessage(what);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    public boolean sendEmptyMessage(int what) {
        return this.handler.sendEmptyMessage(what);
    }

    /* access modifiers changed from: private */
    public ErrorCode appCheck() {
        try {
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            if (TextUtils.isEmpty(sn)) {
                ErrorCode error = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                updateDeviceFaultStatus(error);
                DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                return error;
            }
            try {
                if (Long.parseLong(sn) == 0) {
                    ErrorCode error2 = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                    updateDeviceFaultStatus(error2);
                    DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                    return error2;
                }
            } catch (Exception e) {
            }
            if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                ErrorCode error3 = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
                if (platformData == null) {
                    Log.w("BootHandler.appCheck", "anyo pile, but no pile type and id !!! ");
                    updateDeviceFaultStatus(error3);
                    DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                    return error3;
                } else if (platformData.size() != 2) {
                    Log.w("BootHandler.appCheck", "anyo pile, but not two params: " + platformData.toString());
                    updateDeviceFaultStatus(error3);
                    DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                    return error3;
                } else {
                    for (Map.Entry<String, String> entry : platformData.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (!"type".equals(key) && !"id".equals(key)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but illegal param key: " + key + " !!!");
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        } else if (TextUtils.isEmpty(value)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but no value for param key: " + key + " !!!");
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        } else if ("type".equals(key) && !TextUtils.isDigitsOnly(value)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but illegal value for param type, type value: " + value);
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        }
                    }
                }
            }
            HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
            if (ports == null) {
                ErrorCode error4 = new ErrorCode(ErrorCode.EC_DEVICE_PORT_UNAVAILABLE);
                updateDeviceFaultStatus(error4);
                DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus());
                return error4;
            }
            for (String port : ports.keySet()) {
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(port);
                if (portStatus == null) {
                    ErrorCode error5 = new ErrorCode(ErrorCode.EC_DEVICE_PORT_UNAVAILABLE);
                    updatePortFaultStatus(port, error5);
                    DeviceProxy.getInstance().setSystemStatusBLN(port, DEVICE_STATUS.notInited.getStatus());
                    return error5;
                }
                DEVICE_STATUS portRuntimeStatus = portStatus.getPortRuntimeStatus();
                if (portRuntimeStatus.getStatus() >= DEVICE_STATUS.notInited.getStatus()) {
                    ErrorCode error6 = new ErrorCode(portRuntimeStatus.getStatus() + 30000);
                    updatePortFaultStatus(port, error6);
                    DeviceProxy.getInstance().setSystemStatusBLN(port, portRuntimeStatus.getStatus());
                    return error6;
                }
            }
            File logPath = new File("/data/data/com.xcharge.charger/logs");
            if (logPath.exists() && logPath.isDirectory() && LocalSettingCacheProvider.getInstance().hasLocalSetting() && LocalSettingCacheProvider.getInstance().getChargeSetting().getCpRange() == 0) {
                LocalSettingCacheProvider.getInstance().getChargeSetting().setCpRange(5);
                ChargeStatusCacheProvider.getInstance().updateCPRange(5);
                LocalSettingCacheProvider.getInstance().persist();
            }
            return new ErrorCode(200);
        } catch (Exception e2) {
            Log.e("BootHandler.appCheck", "except: " + Log.getStackTraceString(e2));
            return new ErrorCode(ErrorCode.EC_INTERNAL_ERROR);
        }
    }

    /* access modifiers changed from: private */
    public ErrorCode appConfig() {
        try {
            Boolean isWWlanPolling = SystemSettingCacheProvider.getInstance().isWWlanPolling();
            if (isWWlanPolling != null) {
                C2DeviceProxy.getInstance().switchWWlanPoll(isWWlanPolling.booleanValue());
            }
            Boolean isCPWait = SystemSettingCacheProvider.getInstance().isCPWait();
            if (isCPWait != null) {
                C2DeviceProxy.getInstance().switchCPWait(isCPWait.booleanValue());
            }
            ConsoleSetting consoleSetting = LocalSettingCacheProvider.getInstance().getConsoleSetting();
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (consoleSetting == null || !CHARGE_PLATFORM.xconsole.equals(platform)) {
                int ampCapacity = ChargeStatusCacheProvider.getInstance().getAmpCapacity();
                int cpRange = ChargeStatusCacheProvider.getInstance().getCPRange();
                int voltageRange = ChargeStatusCacheProvider.getInstance().getVoltageRange();
                Integer leakageTolerance = ChargeStatusCacheProvider.getInstance().getLeakageTolerance();
                Boolean earthDisable = ChargeStatusCacheProvider.getInstance().isEarthDisable();
                int adjustCapacity = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
                C2DeviceProxy.getInstance().setAmpCapacity(ampCapacity);
                C2DeviceProxy.getInstance().setCPRange(cpRange);
                C2DeviceProxy.getInstance().setVoltageRange(voltageRange);
                if (leakageTolerance != null) {
                    C2DeviceProxy.getInstance().setLeakageTolerance(leakageTolerance.intValue());
                }
                if (earthDisable != null) {
                    C2DeviceProxy.getInstance().setEarthDisable(earthDisable.booleanValue());
                }
                for (String port : HardwareStatusCacheProvider.getInstance().getPorts().keySet()) {
                    int portAdjustAmp = new BigDecimal((double) (((float) (ChargeStatusCacheProvider.getInstance().getPortStatus(port).getAmpPercent() * adjustCapacity)) / 10000.0f)).setScale(0, 4).intValue();
                    if (portAdjustAmp >= 6) {
                        C2DeviceProxy.getInstance().ajustChargeAmp(port, portAdjustAmp);
                    } else {
                        Log.w("BootHandler.appConfig", "port adjust amp must be more than 6 !!! but init value is " + portAdjustAmp + ", port: " + port);
                    }
                    if (LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(port))) {
                        C2DeviceProxy.getInstance().enableGunLock(port);
                        C2DeviceProxy.getInstance().unlockGun(port);
                        C2DeviceProxy.getInstance().disableGunLock(port);
                    } else {
                        C2DeviceProxy.getInstance().enableGunLock(port);
                        C2DeviceProxy.getInstance().unlockGun(port);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.unlock);
                    }
                }
            } else {
                ChargeSetting chargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
                ChargeStatusCacheProvider.getInstance().updateWorkMode(chargeSetting.getWorkMode());
                int ampCapacity2 = chargeSetting.getAmpCapacity();
                int cpRange2 = chargeSetting.getCpRange();
                int voltageRange2 = chargeSetting.getVoltageRange();
                int adjustCapacity2 = chargeSetting.getAdjustAmp();
                C2DeviceProxy.getInstance().setAmpCapacity(ampCapacity2);
                C2DeviceProxy.getInstance().setCPRange(cpRange2);
                C2DeviceProxy.getInstance().setVoltageRange(voltageRange2);
                ChargeStatusCacheProvider.getInstance().updateAmpCapacity(ampCapacity2);
                ChargeStatusCacheProvider.getInstance().updateCPRange(cpRange2);
                ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltageRange2);
                for (String port2 : HardwareStatusCacheProvider.getInstance().getPorts().keySet()) {
                    int portLocalAmpPercent = LocalSettingCacheProvider.getInstance().getChargePortSetting(port2).getAmpPercent().intValue();
                    int portAdjustAmp2 = new BigDecimal((double) (((float) (adjustCapacity2 * portLocalAmpPercent)) / 10000.0f)).setScale(0, 4).intValue();
                    if (portAdjustAmp2 >= 6) {
                        C2DeviceProxy.getInstance().ajustChargeAmp(port2, portAdjustAmp2);
                        ChargeStatusCacheProvider.getInstance().getPortStatus(port2).setAmpPercent(portLocalAmpPercent);
                        ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustCapacity2);
                    } else {
                        Log.w("BootHandler.appConfig", "port adjust amp must be more than 6 !!! but local set value is " + portAdjustAmp2 + ", port: " + port2);
                    }
                    if (GUN_LOCK_MODE.disable.equals(LocalSettingCacheProvider.getInstance().getChargePortSetting(port2).getGunLockSetting().getMode())) {
                        C2DeviceProxy.getInstance().enableGunLock(port2);
                        C2DeviceProxy.getInstance().unlockGun(port2);
                        C2DeviceProxy.getInstance().disableGunLock(port2);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port2, LOCK_STATUS.disable);
                    } else {
                        C2DeviceProxy.getInstance().enableGunLock(port2);
                        C2DeviceProxy.getInstance().unlockGun(port2);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port2, LOCK_STATUS.unlock);
                    }
                    RadarSetting radarSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting(port2).getRadarSetting();
                    HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(port2, radarSetting.isEnable());
                }
            }
            return new ErrorCode(200);
        } catch (Exception e) {
            Log.e("BootHandler.appConfig", "config except: " + Log.getStackTraceString(e));
            return new ErrorCode(ErrorCode.EC_INTERNAL_ERROR);
        }
    }

    private void updateDeviceFaultStatus(ErrorCode fault) {
        HardwareStatusCacheProvider.getInstance().updateDeviceFaultStatus(fault);
    }

    private void updatePortFaultStatus(String port, ErrorCode fault) {
        int i = 0;
        double d = 0.0d;
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        if (portStatus != null) {
            portStatus.setDeviceError(fault);
            if (fault.getCode() >= 30010) {
                PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
                Double[] dArr = new Double[3];
                dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
                dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
                dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
                portStatus.setAmps(new ArrayList(Arrays.asList(dArr)));
                Double[] dArr2 = new Double[3];
                dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
                dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
                dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
                portStatus.setVolts(new ArrayList(Arrays.asList(dArr2)));
                portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
                if (fullPortRuntimeInfo.getChipTemp() != null) {
                    d = fullPortRuntimeInfo.getChipTemp().doubleValue();
                }
                portStatus.setChipTemp(Double.valueOf(d));
                if (fullPortRuntimeInfo.getCpVoltage() != null) {
                    i = fullPortRuntimeInfo.getCpVoltage().intValue();
                }
                portStatus.setCpVoltage(Integer.valueOf(i));
                HashMap<String, Object> errData = new HashMap<>();
                errData.put("raw", fullPortRuntimeInfo.toJson());
                errData.put("portStatus", portStatus.toJson());
                fault.setData(errData);
            }
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        }
        if (fault.getCode() >= 30010 && fault.getCode() <= 30018) {
            HardwareStatusCacheProvider.getInstance().putDeviceError(port, fault);
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkConnectionStatusChanged(boolean isConnected) {
    }

    /* access modifiers changed from: private */
    public void handleCloudTimeSynch() {
        ChargeStatusCacheProvider.getInstance().isCloudTimeSynch();
    }

    /* access modifiers changed from: private */
    public void endExceptionBills() {
        int exceptionBillNum = ChargeContentProxy.getInstance().endExceptionChargeBill();
        if (exceptionBillNum > 0) {
            Log.i("BootHandler.endExceptionBills", "forced to end exception charge bill !!! num: " + exceptionBillNum);
            LogUtils.applog("force to finish " + exceptionBillNum + " unended charge bills when booting");
        }
    }

    /* access modifiers changed from: private */
    public void clearTimeoutBills() {
        long nowTime = System.currentTimeMillis();
        long timeBefore = nowTime - TIMEOUT_SAVED_BILL;
        long timeAfter = nowTime - 315360000000L;
        if (timeBefore <= 0 || timeAfter <= 0) {
            Log.w("BootHandler.clearTimeoutBills", "system time maybe not reality time, do not clear bill !!! now time: " + nowTime);
            return;
        }
        int timeoutBillNum = ChargeContentProxy.getInstance().clearChargeBillBefore(timeAfter, timeBefore);
        if (timeoutBillNum > 0) {
            Log.i("BootHandler.clearTimeoutBills", "forced to delete timeout charge bill !!! num: " + timeoutBillNum);
            LogUtils.applog("delete " + timeoutBillNum + " timeout charge bills in local database");
        }
    }

    public void initAdvertPath() {
        String advertRootPath = String.valueOf(this.context.getFilesDir().getParentFile().getPath()) + "/advert/";
        String pullAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.pullAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String wakeUpAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.wakeUpAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String idleAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.idleAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String chargingAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.chargingAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        File scanAdvsite = new File(String.valueOf(advertRootPath) + ADVERT_POLICY.scanAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR);
        if (!scanAdvsite.exists()) {
            scanAdvsite.mkdirs();
        }
        File pullAdvsite = new File(pullAdvsitePath);
        if (!pullAdvsite.exists()) {
            pullAdvsite.mkdirs();
        }
        File wakeUpAdvsite = new File(wakeUpAdvsitePath);
        if (!wakeUpAdvsite.exists()) {
            wakeUpAdvsite.mkdirs();
        }
        File idleAdvsite = new File(idleAdvsitePath);
        if (!idleAdvsite.exists()) {
            idleAdvsite.mkdirs();
        }
        File chargingAdvsite = new File(chargingAdvsitePath);
        if (!chargingAdvsite.exists()) {
            chargingAdvsite.mkdirs();
        }
    }

    /* access modifiers changed from: private */
    public void initDownloadPath() {
        String downloadRootPath = String.valueOf(this.context.getFilesDir().getParentFile().getPath()) + "/download/";
        String resourcePath = String.valueOf(downloadRootPath) + "resource/";
        String scanAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.scanAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String pullAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.pullAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String wakeUpAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.wakeUpAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String idleAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.idleAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String chargingAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.chargingAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        File upgrade = new File(String.valueOf(downloadRootPath) + "upgrade/");
        if (!upgrade.exists()) {
            upgrade.mkdirs();
        }
        File resource = new File(resourcePath);
        if (!resource.exists()) {
            resource.mkdirs();
        }
        File scanAdvsite = new File(scanAdvsitePath);
        if (!scanAdvsite.exists()) {
            scanAdvsite.mkdirs();
        }
        File pullAdvsite = new File(pullAdvsitePath);
        if (!pullAdvsite.exists()) {
            pullAdvsite.mkdirs();
        }
        File wakeUpAdvsite = new File(wakeUpAdvsitePath);
        if (!wakeUpAdvsite.exists()) {
            wakeUpAdvsite.mkdirs();
        }
        File idleAdvsite = new File(idleAdvsitePath);
        if (!idleAdvsite.exists()) {
            idleAdvsite.mkdirs();
        }
        File chargingAdvsite = new File(chargingAdvsitePath);
        if (!chargingAdvsite.exists()) {
            chargingAdvsite.mkdirs();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0042, code lost:
        r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r12.context, com.xcharge.charger.C0221R.raw.migrate_in, "migrate_in.sql");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0035, code lost:
        r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r12.context, com.xcharge.charger.C0221R.raw.migrate_out, "migrate_out.sql");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void migrate() {
        /*
            r12 = this;
            java.io.File r6 = new java.io.File
            java.lang.String r9 = "/data/tmp/com.atsmart.xcharger/databases"
            r6.<init>(r9)
            boolean r9 = r6.exists()
            if (r9 == 0) goto L_0x0013
            boolean r9 = r6.isDirectory()
            if (r9 != 0) goto L_0x0014
        L_0x0013:
            return
        L_0x0014:
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.String r10 = "migrate v1.0 database data to v2.0 !!!"
            android.util.Log.i(r9, r10)
            java.lang.String r9 = "try to migrate database data from V1 APP to V2 APP !!!"
            com.xcharge.common.utils.LogUtils.applog(r9)
            java.lang.String r5 = "migrate.sql"
            java.lang.String r4 = "migrate_out.sql"
            java.lang.String r3 = "migrate_in.sql"
            java.lang.String r0 = "droptable.sql"
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate.sql"
            android.content.Context r9 = r12.context     // Catch:{ Exception -> 0x0064 }
            r10 = 2131099653(0x7f060005, float:1.7811665E38)
            boolean r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r9, r10, r5)     // Catch:{ Exception -> 0x0064 }
            if (r2 == 0) goto L_0x005a
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate_out.sql"
            android.content.Context r9 = r12.context     // Catch:{ Exception -> 0x0064 }
            r10 = 2131099655(0x7f060007, float:1.781167E38)
            boolean r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r9, r10, r4)     // Catch:{ Exception -> 0x0064 }
            if (r2 == 0) goto L_0x005a
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate_in.sql"
            android.content.Context r9 = r12.context     // Catch:{ Exception -> 0x0064 }
            r10 = 2131099654(0x7f060006, float:1.7811667E38)
            boolean r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r9, r10, r3)     // Catch:{ Exception -> 0x0064 }
            if (r2 == 0) goto L_0x005a
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/droptable.sql"
            android.content.Context r9 = r12.context     // Catch:{ Exception -> 0x0064 }
            r10 = 2131099651(0x7f060003, float:1.7811661E38)
            boolean r2 = com.xcharge.common.utils.ContextUtils.getRawFileToContextPath(r9, r10, r0)     // Catch:{ Exception -> 0x0064 }
        L_0x005a:
            if (r2 != 0) goto L_0x006f
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.String r10 = "failed load migrate scripts !!!"
            android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x0064 }
            goto L_0x0013
        L_0x0064:
            r1 = move-exception
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.String r10 = android.util.Log.getStackTraceString(r1)
            android.util.Log.w(r9, r10)
            goto L_0x0013
        L_0x006f:
            r7 = -1
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate_out.sql"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = "sqlite3 /data/tmp/com.atsmart.xcharger/databases/key.db < "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r9 = r9.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0064 }
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "exec shell cmd, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = ", script: "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate_in.sql"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = "sqlite3 /data/data/com.xcharge.charger/databases/content.db < "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r9 = r9.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0064 }
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "exec shell cmd, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = ", script: "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate.sql"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = "sqlite3 /data/data/com.xcharge.charger/databases/content.db < "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r9 = r9.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0064 }
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "exec shell cmd, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = ", script: "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            if (r7 != 0) goto L_0x011c
            com.xcharge.charger.data.proxy.NFCKeyContentProxy r9 = com.xcharge.charger.data.proxy.NFCKeyContentProxy.getInstance()     // Catch:{ Exception -> 0x0064 }
            boolean r9 = r9.updateKeyseedFromOldVerDb()     // Catch:{ Exception -> 0x0064 }
            if (r9 != 0) goto L_0x011c
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.String r10 = "failed to update nfc key table !!!"
            android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x0064 }
        L_0x011c:
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/droptable.sql"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = "sqlite3 /data/data/com.xcharge.charger/databases/content.db < "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r9 = r9.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0064 }
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "exec shell cmd, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = ", script: "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r8 = "/data/data/com.xcharge.charger/files/migrate* /data/data/com.xcharge.charger/files/droptable.sql /data/data/com.xcharge.charger/files/key.sql"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = "rm -rf "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r9 = r9.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0064 }
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "exec shell cmd, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = ", script: "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r8)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "mkdir -p /data/data/com.xcharge.charger/files/oldver;cp -rf /data/tmp/com.atsmart.xcharger/databases /data/data/com.xcharge.charger/files/oldver"
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "backup old version dbs, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "rm -rf /data/data/com.atsmart.xcharger /data/tmp/com.atsmart.xcharger"
            int r7 = com.xcharge.common.utils.FileUtils.execShell(r9)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r9 = "BootHandler.migrate"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064 }
            java.lang.String r11 = "delete old version /data/data/com.atsmart.xcharger /data/tmp/com.atsmart.xcharger, ret: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x0064 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x0064 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0064 }
            android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0064 }
            goto L_0x0013
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.boot.handler.BootHandler.migrate():void");
    }
}
