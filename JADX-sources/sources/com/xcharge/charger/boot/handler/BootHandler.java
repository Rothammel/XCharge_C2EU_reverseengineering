package com.xcharge.charger.boot.handler;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.R;
import com.xcharge.charger.boot.service.BootService;
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
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class BootHandler {
    public static final int MSG_BOOT_APP_INIT = 20481;
    public static final int MSG_CLEAR_TIMEOUT_BILL = 20483;
    public static final int MSG_NETWORK_CONNECTION_CHANGED = 20482;
    public static final long TIMEOUT_SAVED_BILL = 2592000000L;
    private Context context = null;
    private Handler mainHandler = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private CloudTimeSynchObserver cloudTimeSynchObserver = null;

    /* loaded from: classes.dex */
    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case BootHandler.MSG_BOOT_APP_INIT /* 20481 */:
                        Log.i("BootHandler.handleMessage", "app init begin !!!");
                        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                            BootHandler.this.migrate();
                        }
                        ErrorCode error = BootHandler.this.appCheck();
                        if (error.getCode() == 200 || (error.getCode() >= 30010 && error.getCode() <= 30018)) {
                            ErrorCode configError = BootHandler.this.appConfig();
                            if (configError.getCode() == 200) {
                                BootHandler.this.cloudTimeSynchObserver = new CloudTimeSynchObserver(BootHandler.this.context, BootHandler.this.handler);
                                BootHandler.this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("cloud/timeSynch"), true, BootHandler.this.cloudTimeSynchObserver);
                                for (String port : HardwareStatusCacheProvider.getInstance().getPorts().keySet()) {
                                    C2DeviceProxy.getInstance().authInValid(port, "", "");
                                }
                                BootHandler.this.endExceptionBills();
                                BootHandler.this.clearTimeoutBills();
                                BootHandler.this.handler.sendEmptyMessageAtTime(BootHandler.MSG_CLEAR_TIMEOUT_BILL, TimeUtils.getTomorrowAt(2, 0, 0));
                                BootHandler.this.initAdvertPath();
                                BootHandler.this.initDownloadPath();
                            } else {
                                error = configError;
                            }
                        }
                        Message appInitEndMsg = BootHandler.this.mainHandler.obtainMessage(BootService.MSG_BOOT_APP_INIT_END);
                        appInitEndMsg.obj = error.toJson();
                        BootHandler.this.mainHandler.sendMessage(appInitEndMsg);
                        break;
                    case BootHandler.MSG_NETWORK_CONNECTION_CHANGED /* 20482 */:
                        boolean isConnected = ((Boolean) msg.obj).booleanValue();
                        Log.i("BootHandler.handleMessage", "network connection status changed !!! is connected: " + isConnected);
                        BootHandler.this.handleNetworkConnectionStatusChanged(isConnected);
                        break;
                    case BootHandler.MSG_CLEAR_TIMEOUT_BILL /* 20483 */:
                        Log.i("BootHandler.handleMessage", "clear timeout bills periodically !!!");
                        try {
                            BootHandler.this.clearTimeoutBills();
                        } catch (Exception e) {
                        }
                        BootHandler.this.handler.sendEmptyMessageAtTime(BootHandler.MSG_CLEAR_TIMEOUT_BILL, TimeUtils.getTomorrowAt(2, 0, 0));
                        break;
                    case CloudTimeSynchObserver.MSG_CLOUD_TIME_SYNCHED /* 143361 */:
                        Log.i("BootHandler.handleMessage", "cloud time synch changed !!!");
                        BootHandler.this.handleCloudTimeSynch();
                        break;
                }
            } catch (Exception e2) {
                Log.e("BootHandler.handleMessage", "except: " + Log.getStackTraceString(e2));
                LogUtils.syslog("BootHandler handleMessage exception: " + Log.getStackTraceString(e2));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context, Handler mainHandler) {
        this.context = context;
        this.mainHandler = mainHandler;
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

    /* JADX INFO: Access modifiers changed from: private */
    public ErrorCode appCheck() {
        try {
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            if (TextUtils.isEmpty(sn)) {
                ErrorCode error = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                updateDeviceFaultStatus(error);
                DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                return error;
            }
            try {
                if (Long.parseLong(sn) == 0) {
                    ErrorCode error2 = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                    updateDeviceFaultStatus(error2);
                    DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                    return error2;
                }
            } catch (Exception e) {
            }
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (CHARGE_PLATFORM.anyo.equals(platform)) {
                ErrorCode error3 = new ErrorCode(ErrorCode.EC_DEVICE_SN_NOT_EXIST);
                HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
                if (platformData == null) {
                    Log.w("BootHandler.appCheck", "anyo pile, but no pile type and id !!! ");
                    updateDeviceFaultStatus(error3);
                    DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                    return error3;
                } else if (platformData.size() != 2) {
                    Log.w("BootHandler.appCheck", "anyo pile, but not two params: " + platformData.toString());
                    updateDeviceFaultStatus(error3);
                    DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                    return error3;
                } else {
                    for (Map.Entry<String, String> entry : platformData.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (!"type".equals(key) && !"id".equals(key)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but illegal param key: " + key + " !!!");
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        } else if (TextUtils.isEmpty(value)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but no value for param key: " + key + " !!!");
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        } else if ("type".equals(key) && !TextUtils.isDigitsOnly(value)) {
                            Log.w("BootHandler.appCheck", "anyo pile, but illegal value for param type, type value: " + value);
                            updateDeviceFaultStatus(error3);
                            DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
                            return error3;
                        }
                    }
                }
            }
            HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
            if (ports == null) {
                ErrorCode error4 = new ErrorCode(ErrorCode.EC_DEVICE_PORT_UNAVAILABLE);
                updateDeviceFaultStatus(error4);
                DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus());
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
            if (logPath.exists() && logPath.isDirectory() && LocalSettingCacheProvider.getInstance().hasLocalSetting()) {
                int cpRange = LocalSettingCacheProvider.getInstance().getChargeSetting().getCpRange();
                if (cpRange == 0) {
                    LocalSettingCacheProvider.getInstance().getChargeSetting().setCpRange(5);
                    ChargeStatusCacheProvider.getInstance().updateCPRange(5);
                    LocalSettingCacheProvider.getInstance().persist();
                }
            }
            ErrorCode error7 = new ErrorCode(200);
            return error7;
        } catch (Exception e2) {
            Log.e("BootHandler.appCheck", "except: " + Log.getStackTraceString(e2));
            ErrorCode error8 = new ErrorCode(ErrorCode.EC_INTERNAL_ERROR);
            return error8;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
            if (consoleSetting != null && CHARGE_PLATFORM.xconsole.equals(platform)) {
                ChargeSetting chargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
                ChargeStatusCacheProvider.getInstance().updateWorkMode(chargeSetting.getWorkMode());
                int ampCapacity = chargeSetting.getAmpCapacity();
                int cpRange = chargeSetting.getCpRange();
                int voltageRange = chargeSetting.getVoltageRange();
                int adjustCapacity = chargeSetting.getAdjustAmp();
                C2DeviceProxy.getInstance().setAmpCapacity(ampCapacity);
                C2DeviceProxy.getInstance().setCPRange(cpRange);
                C2DeviceProxy.getInstance().setVoltageRange(voltageRange);
                ChargeStatusCacheProvider.getInstance().updateAmpCapacity(ampCapacity);
                ChargeStatusCacheProvider.getInstance().updateCPRange(cpRange);
                ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltageRange);
                HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
                for (String port : ports.keySet()) {
                    int portLocalAmpPercent = LocalSettingCacheProvider.getInstance().getChargePortSetting(port).getAmpPercent().intValue();
                    int portAdjustAmp = new BigDecimal((adjustCapacity * portLocalAmpPercent) / 10000.0f).setScale(0, 4).intValue();
                    if (portAdjustAmp >= 6) {
                        C2DeviceProxy.getInstance().ajustChargeAmp(port, portAdjustAmp);
                        ChargeStatusCacheProvider.getInstance().getPortStatus(port).setAmpPercent(portLocalAmpPercent);
                        ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustCapacity);
                    } else {
                        Log.w("BootHandler.appConfig", "port adjust amp must be more than 6 !!! but local set value is " + portAdjustAmp + ", port: " + port);
                    }
                    GUN_LOCK_MODE lockMode = LocalSettingCacheProvider.getInstance().getChargePortSetting(port).getGunLockSetting().getMode();
                    if (GUN_LOCK_MODE.disable.equals(lockMode)) {
                        C2DeviceProxy.getInstance().enableGunLock(port);
                        C2DeviceProxy.getInstance().unlockGun(port);
                        C2DeviceProxy.getInstance().disableGunLock(port);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.disable);
                    } else {
                        C2DeviceProxy.getInstance().enableGunLock(port);
                        C2DeviceProxy.getInstance().unlockGun(port);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.unlock);
                    }
                    RadarSetting radarSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting(port).getRadarSetting();
                    HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(port, radarSetting.isEnable());
                }
            } else {
                int ampCapacity2 = ChargeStatusCacheProvider.getInstance().getAmpCapacity();
                int cpRange2 = ChargeStatusCacheProvider.getInstance().getCPRange();
                int voltageRange2 = ChargeStatusCacheProvider.getInstance().getVoltageRange();
                Integer leakageTolerance = ChargeStatusCacheProvider.getInstance().getLeakageTolerance();
                Boolean earthDisable = ChargeStatusCacheProvider.getInstance().isEarthDisable();
                int adjustCapacity2 = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
                C2DeviceProxy.getInstance().setAmpCapacity(ampCapacity2);
                C2DeviceProxy.getInstance().setCPRange(cpRange2);
                C2DeviceProxy.getInstance().setVoltageRange(voltageRange2);
                if (leakageTolerance != null) {
                    C2DeviceProxy.getInstance().setLeakageTolerance(leakageTolerance.intValue());
                }
                if (earthDisable != null) {
                    C2DeviceProxy.getInstance().setEarthDisable(earthDisable.booleanValue());
                }
                HashMap<String, Port> ports2 = HardwareStatusCacheProvider.getInstance().getPorts();
                for (String port2 : ports2.keySet()) {
                    int portAdjustAmp2 = new BigDecimal((ChargeStatusCacheProvider.getInstance().getPortStatus(port2).getAmpPercent() * adjustCapacity2) / 10000.0f).setScale(0, 4).intValue();
                    if (portAdjustAmp2 >= 6) {
                        C2DeviceProxy.getInstance().ajustChargeAmp(port2, portAdjustAmp2);
                    } else {
                        Log.w("BootHandler.appConfig", "port adjust amp must be more than 6 !!! but init value is " + portAdjustAmp2 + ", port: " + port2);
                    }
                    LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(port2);
                    if (LOCK_STATUS.disable.equals(lockStatus)) {
                        C2DeviceProxy.getInstance().enableGunLock(port2);
                        C2DeviceProxy.getInstance().unlockGun(port2);
                        C2DeviceProxy.getInstance().disableGunLock(port2);
                    } else {
                        C2DeviceProxy.getInstance().enableGunLock(port2);
                        C2DeviceProxy.getInstance().unlockGun(port2);
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port2, LOCK_STATUS.unlock);
                    }
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
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        if (portStatus != null) {
            portStatus.setDeviceError(fault);
            if (fault.getCode() >= 30010) {
                PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
                Double[] dArr = new Double[3];
                dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
                dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
                dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
                portStatus.setAmps(new ArrayList<>(Arrays.asList(dArr)));
                Double[] dArr2 = new Double[3];
                dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
                dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
                dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
                portStatus.setVolts(new ArrayList<>(Arrays.asList(dArr2)));
                portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
                portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() != null ? fullPortRuntimeInfo.getChipTemp().doubleValue() : 0.0d));
                portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() != null ? fullPortRuntimeInfo.getCpVoltage().intValue() : 0));
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkConnectionStatusChanged(boolean isConnected) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCloudTimeSynch() {
        ChargeStatusCacheProvider.getInstance().isCloudTimeSynch();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void endExceptionBills() {
        int exceptionBillNum = ChargeContentProxy.getInstance().endExceptionChargeBill();
        if (exceptionBillNum > 0) {
            Log.i("BootHandler.endExceptionBills", "forced to end exception charge bill !!! num: " + exceptionBillNum);
            LogUtils.applog("force to finish " + exceptionBillNum + " unended charge bills when booting");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
        String scanAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.scanAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String pullAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.pullAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String wakeUpAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.wakeUpAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String idleAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.idleAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String chargingAdvsitePath = String.valueOf(advertRootPath) + ADVERT_POLICY.chargingAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
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

    /* JADX INFO: Access modifiers changed from: private */
    public void initDownloadPath() {
        String downloadRootPath = String.valueOf(this.context.getFilesDir().getParentFile().getPath()) + "/download/";
        String upgradePath = String.valueOf(downloadRootPath) + "upgrade/";
        String resourcePath = String.valueOf(downloadRootPath) + "resource/";
        String scanAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.scanAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String pullAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.pullAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String wakeUpAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.wakeUpAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String idleAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.idleAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        String chargingAdvsitePath = String.valueOf(downloadRootPath) + "advert/" + ADVERT_POLICY.chargingAdvsite + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        File upgrade = new File(upgradePath);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void migrate() {
        File oldVerContextPath = new File("/data/tmp/com.atsmart.xcharger/databases");
        if (oldVerContextPath.exists() && oldVerContextPath.isDirectory()) {
            Log.i("BootHandler.migrate", "migrate v1.0 database data to v2.0 !!!");
            LogUtils.applog("try to migrate database data from V1 APP to V2 APP !!!");
            try {
                boolean isOk = ContextUtils.getRawFileToContextPath(this.context, R.raw.migrate, "migrate.sql");
                if (isOk && (isOk = ContextUtils.getRawFileToContextPath(this.context, R.raw.migrate_out, "migrate_out.sql")) && (isOk = ContextUtils.getRawFileToContextPath(this.context, R.raw.migrate_in, "migrate_in.sql"))) {
                    isOk = ContextUtils.getRawFileToContextPath(this.context, R.raw.droptable, "droptable.sql");
                }
                if (!isOk) {
                    Log.w("BootHandler.migrate", "failed load migrate scripts !!!");
                    return;
                }
                int ret = FileUtils.execShell("sqlite3 /data/tmp/com.atsmart.xcharger/databases/key.db < /data/data/com.xcharge.charger/files/migrate_out.sql");
                Log.i("BootHandler.migrate", "exec shell cmd, ret: " + ret + ", script: /data/data/com.xcharge.charger/files/migrate_out.sql");
                int ret2 = FileUtils.execShell("sqlite3 /data/data/com.xcharge.charger/databases/content.db < /data/data/com.xcharge.charger/files/migrate_in.sql");
                Log.i("BootHandler.migrate", "exec shell cmd, ret: " + ret2 + ", script: /data/data/com.xcharge.charger/files/migrate_in.sql");
                int ret3 = FileUtils.execShell("sqlite3 /data/data/com.xcharge.charger/databases/content.db < /data/data/com.xcharge.charger/files/migrate.sql");
                Log.i("BootHandler.migrate", "exec shell cmd, ret: " + ret3 + ", script: /data/data/com.xcharge.charger/files/migrate.sql");
                if (ret3 == 0 && !NFCKeyContentProxy.getInstance().updateKeyseedFromOldVerDb()) {
                    Log.w("BootHandler.migrate", "failed to update nfc key table !!!");
                }
                int ret4 = FileUtils.execShell("sqlite3 /data/data/com.xcharge.charger/databases/content.db < /data/data/com.xcharge.charger/files/droptable.sql");
                Log.i("BootHandler.migrate", "exec shell cmd, ret: " + ret4 + ", script: /data/data/com.xcharge.charger/files/droptable.sql");
                int ret5 = FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/files/migrate* /data/data/com.xcharge.charger/files/droptable.sql /data/data/com.xcharge.charger/files/key.sql");
                Log.i("BootHandler.migrate", "exec shell cmd, ret: " + ret5 + ", script: /data/data/com.xcharge.charger/files/migrate* /data/data/com.xcharge.charger/files/droptable.sql /data/data/com.xcharge.charger/files/key.sql");
                int ret6 = FileUtils.execShell("mkdir -p /data/data/com.xcharge.charger/files/oldver;cp -rf /data/tmp/com.atsmart.xcharger/databases /data/data/com.xcharge.charger/files/oldver");
                Log.i("BootHandler.migrate", "backup old version dbs, ret: " + ret6);
                int ret7 = FileUtils.execShell("rm -rf /data/data/com.atsmart.xcharger /data/tmp/com.atsmart.xcharger");
                Log.i("BootHandler.migrate", "delete old version /data/data/com.atsmart.xcharger /data/tmp/com.atsmart.xcharger, ret: " + ret7);
            } catch (Exception e) {
                Log.w("BootHandler.migrate", Log.getStackTraceString(e));
            }
        }
    }
}
