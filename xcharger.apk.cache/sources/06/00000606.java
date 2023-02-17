package com.xcharge.charger.device.c2.service;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.android.chargerhd.chargerhdNative;
import com.xcharge.charger.data.bean.device.Hardware;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.device.Software;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.RadarSetting;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.status.ChargeStatus;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.device.api.PortStatusListener;
import com.xcharge.charger.device.c2.bean.AuthValue;
import com.xcharge.charger.device.c2.bean.BLNValue;
import com.xcharge.charger.device.c2.bean.BooleanValue;
import com.xcharge.charger.device.c2.bean.DeviceBasicInfoData;
import com.xcharge.charger.device.c2.bean.DeviceControlData;
import com.xcharge.charger.device.c2.bean.DeviceRuntimeData;
import com.xcharge.charger.device.c2.bean.IntValue;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.network.NetworkProxy;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.SystemPropertiesProxy;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class C2DeviceProxy extends NetworkProxy {
    private static C2DeviceProxy instance = null;

    public static C2DeviceProxy getInstance() {
        if (instance == null) {
            instance = new C2DeviceProxy();
        }
        return instance;
    }

    @Override // com.xcharge.charger.device.network.NetworkProxy
    public void init(Context context) {
        super.init(context);
    }

    @Override // com.xcharge.charger.device.network.NetworkProxy
    public void destroy() {
        super.destroy();
    }

    public void attachPortStatusListener(PortStatusListener listener) {
        C2DeviceEventDispatcher.getInstance().attachPortStatusListener(listener);
    }

    public void dettachPortStatusListener(PortStatusListener listener) {
        C2DeviceEventDispatcher.getInstance().dettachPortStatusListener(listener);
    }

    public void authValid(String port, String type, String uid) {
        try {
            Log.i("C2DeviceProxy.authValid", "port: " + port + ", type: " + type + ", uid: " + uid);
            DeviceControlData data = new DeviceControlData();
            AuthValue param = new AuthValue();
            param.setValue(true);
            param.setCause(String.valueOf(type) + "_" + uid);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_AUTH);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.authValid", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.authValid", Log.getStackTraceString(e));
        }
    }

    public void authInValid(String port, String type, String uid) {
        try {
            Log.i("C2DeviceProxy.authInValid", "port: " + port + ", type: " + type + ", uid: " + uid);
            DeviceControlData data = new DeviceControlData();
            AuthValue param = new AuthValue();
            param.setValue(false);
            param.setCause(String.valueOf(type) + "_" + uid);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_AUTH);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.authInValid", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.authInValid", Log.getStackTraceString(e));
        }
    }

    public void startCharge(String port) {
        try {
            Log.i("C2DeviceProxy.startCharge", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(true);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_CHARGE_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.startCharge", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.startCharge", Log.getStackTraceString(e));
        }
    }

    public void stopCharge(String port) {
        try {
            Log.i("C2DeviceProxy.stopCharge", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(false);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_CHARGE_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
            Log.d("C2DeviceProxy.stopCharge", "cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.stopCharge", Log.getStackTraceString(e));
        }
    }

    public void ajustChargeAmp(String port, int amp) {
        try {
            Log.i("C2DeviceProxy.ajustChargeAmp", "port: " + port + ", amp: " + amp);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(amp);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_PWM_AMP);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.ajustChargeAmp", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.ajustChargeAmp", Log.getStackTraceString(e));
        }
    }

    public void setAmpCapacity(int amp) {
        try {
            Log.i("C2DeviceProxy.setAmpCapacity", "amp: " + amp);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(amp);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_CAPACITY_AMP);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            HardwareStatusCacheProvider.getInstance().updateAmpCapacity(amp);
            Log.d("C2DeviceProxy.setAmpCapacity", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setAmpCapacity", Log.getStackTraceString(e));
        }
    }

    public void setCPRange(int percent) {
        try {
            Log.i("C2DeviceProxy.setCPRange", "percent: " + percent);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(percent);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_CP_RANGE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setCPRange", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setCPRange", Log.getStackTraceString(e));
        }
    }

    public void setVoltageRange(int percent) {
        try {
            Log.i("C2DeviceProxy.setVoltageRange", "percent: " + percent);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(percent);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_VOLTAGE_RANGE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setVoltageRange", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setVoltageRange", Log.getStackTraceString(e));
        }
    }

    public void setLeakageTolerance(int mamp) {
        try {
            Log.i("C2DeviceProxy.setLeakageTolerance", "mamp: " + mamp);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(mamp);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_LEAKAGE_TOLERANCE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setLeakageTolerance", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setLeakageTolerance", Log.getStackTraceString(e));
        }
    }

    public void setEarthDisable(boolean isDisable) {
        try {
            Log.i("C2DeviceProxy.setEarthDisable", "isDisable: " + isDisable);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(isDisable);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_EARTH_DISABLE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setEarthDisable", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setEarthDisable", Log.getStackTraceString(e));
        }
    }

    public void openGunLed(String port) {
        try {
            Log.i("C2DeviceProxy.openGunLed", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(true);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_GUN_LED_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.openGunLed", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.openGunLed", Log.getStackTraceString(e));
        }
    }

    public void closeGunLed(String port) {
        try {
            Log.i("C2DeviceProxy.closeGunLed", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(false);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_GUN_LED_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.closeGunLed", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.closeGunLed", Log.getStackTraceString(e));
        }
    }

    public void enableGunLock(String port) {
        try {
            Log.i("C2DeviceProxy.enableGunLock", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(true);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_ELECLOCK_ENABLE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.enableGunLock", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.enableGunLock", Log.getStackTraceString(e));
        }
    }

    public void disableGunLock(String port) {
        try {
            Log.i("C2DeviceProxy.disableGunLock", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(false);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_ELECLOCK_ENABLE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.disableGunLock", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.disableGunLock", Log.getStackTraceString(e));
        }
    }

    public void setGunLockMode(String port, int mode) {
        try {
            Log.i("C2DeviceProxy.setGunLockMode", "port: " + port + ", mode: " + mode);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(mode);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_SET_ELECLOCK);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setGunLockMode", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setGunLockMode", Log.getStackTraceString(e));
        }
    }

    public void lockGun(String port) {
        try {
            Log.i("C2DeviceProxy.lockGun", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(true);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_ELECLOCK_ON_OFF);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.lockGun", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.lockGun", Log.getStackTraceString(e));
        }
    }

    public void unlockGun(String port) {
        try {
            Log.i("C2DeviceProxy.unlockGun", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(false);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_ELECLOCK_ON_OFF);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.unlockGun", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.unlockGun", Log.getStackTraceString(e));
        }
    }

    public void switchWWlanPoll(boolean enabled) {
        try {
            Log.i("C2DeviceProxy.switchWWlanPoll", "enable: " + enabled);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(enabled);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_WWLAN_POLL_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.switchWWlanPoll", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.switchWWlanPoll", Log.getStackTraceString(e));
        }
    }

    public void switchCPWait(boolean enabled) {
        try {
            Log.i("C2DeviceProxy.switchCPWait", "enable: " + enabled);
            DeviceControlData data = new DeviceControlData();
            BooleanValue param = new BooleanValue();
            param.setValue(enabled);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_CP_WAIT_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.switchCPWait", "cmd: " + cmd + ", reply: " + reply);
            LogUtils.syslog("cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.switchCPWait", Log.getStackTraceString(e));
        }
    }

    public void beep(int num) {
        try {
            Log.i("C2DeviceProxy.beep", "num: " + num);
            DeviceControlData data = new DeviceControlData();
            IntValue param = new IntValue();
            param.setValue(num);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_BEEP_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.beep", "cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.beep", Log.getStackTraceString(e));
        }
    }

    public void setBLN(int color, int time, int mode) {
        int delayOn;
        int delayOff;
        try {
            Log.i("C2DeviceProxy.setBLN", "color: " + color + ", time: " + time + ", mode: " + mode);
            if (mode == 0) {
                delayOn = time & MotionEventCompat.ACTION_MASK;
                delayOff = time & MotionEventCompat.ACTION_MASK;
            } else {
                delayOn = time | 32768;
                delayOff = time | 32768;
            }
            DeviceControlData data = new DeviceControlData();
            BLNValue param = new BLNValue();
            param.setColor(color);
            param.setDelayOn(delayOn);
            param.setDelayOff(delayOff);
            data.setData(param);
            data.setCmd(DeviceControlData.CMD_LED_CTRL);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.setBLN", "cmd: " + cmd + ", reply: " + reply);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.setBLN", Log.getStackTraceString(e));
        }
    }

    public void setDeviceStatusBLN(int status, int defaultColor) {
        switch (status) {
            case -1:
            case 0:
            case 1:
                setBLN(defaultColor, 0, 0);
                return;
            case 2:
                setBLN(defaultColor, XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED, 0);
                return;
            case 3:
                setBLN(16776960, XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED, 0);
                return;
            case 4:
                setBLN(defaultColor, 1000, 1);
                return;
            case 5:
            case 6:
                setBLN(16776960, XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED, 0);
                return;
            case 7:
            case 8:
            case 9:
            case 10:
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
            default:
                setBLN(16711680, 0, 0);
                return;
            case 13:
                setBLN(16711680, FTPCodes.NOT_LOGGED_IN, 0);
                return;
        }
    }

    public void setParkBusyBLN() {
        setBLN(16711680, 100, 0);
    }

    public void setDeviceUnavailableBLN() {
        setBLN(16711680, 0, 0);
    }

    public void setSystemStatusBLN(String port, int status, int defaultColor) {
        if (status >= 10 && status <= 18) {
            setDeviceStatusBLN(status, defaultColor);
        } else if (!HardwareStatusCacheProvider.getInstance().isNetworkConnected() && (WORK_MODE.Public.equals(ChargeStatusCacheProvider.getInstance().getWorkMode()) || (!CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform()) && !CHARGE_PLATFORM.xconsole.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())))) {
            if (!ChargeStatusCacheProvider.getInstance().getPortSwitch("1") && status == 0) {
                setDeviceUnavailableBLN();
            } else {
                setBLN(16776960, 0, 0);
            }
        } else if (!ChargeStatusCacheProvider.getInstance().getPortSwitch("1") && status == 0) {
            setDeviceUnavailableBLN();
        } else {
            setDeviceStatusBLN(status, defaultColor);
        }
    }

    public DeviceBasicInfoData getDeviceBasicInfo() {
        DeviceBasicInfoData deviceInfo = null;
        try {
            Log.i("C2DeviceProxy.getDeviceBasicInfo", "query device info");
            DeviceControlData data = new DeviceControlData();
            data.setData(new Object());
            data.setCmd(DeviceControlData.CMD_GET_INFO);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.getDeviceBasicInfo", "cmd: " + cmd + ", reply: " + reply);
            DeviceControlData replyChargeControlData = new DeviceControlData().fromJson(reply);
            deviceInfo = new DeviceBasicInfoData().fromJson(JsonBean.ObjectToJson(replyChargeControlData.getData()));
            deviceInfo.setPorts(new ArrayList<>(Arrays.asList("1")));
            return deviceInfo;
        } catch (Exception e) {
            Log.e("C2DeviceProxy.getDeviceBasicInfo", Log.getStackTraceString(e));
            return deviceInfo;
        }
    }

    public DeviceRuntimeData getDeviceRuntimeInfo() {
        DeviceRuntimeData deviceRuntimeInfo = new DeviceRuntimeData();
        HashMap<String, PortRuntimeData> portsRuntimeInfo = new HashMap<>();
        Set<String> ports = HardwareStatusCacheProvider.getInstance().getPorts().keySet();
        for (String port : ports) {
            PortRuntimeData portInfo = getPortRuntimeInfo(port);
            portsRuntimeInfo.put(port, portInfo);
        }
        deviceRuntimeInfo.setPortsInfo(portsRuntimeInfo);
        return deviceRuntimeInfo;
    }

    public PortRuntimeData getPortRuntimeInfo(String port) {
        try {
            Log.i("C2DeviceProxy.getPortRuntimeInfo", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            data.setData(new Object());
            data.setCmd(DeviceControlData.CMD_GET_UPDATE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.getPortRuntimeInfo", "cmd: " + cmd + ", reply: " + reply);
            DeviceControlData replyChargeControlData = new DeviceControlData().fromJson(reply);
            PortRuntimeData portInfo = new PortRuntimeData().fromJson(JsonBean.ObjectToJson(replyChargeControlData.getData()));
            return portInfo;
        } catch (Exception e) {
            Log.e("C2DeviceProxy.getPortRuntimeInfo", Log.getStackTraceString(e));
            return null;
        }
    }

    public String getRawPortRuntimeInfo(String port) {
        try {
            Log.i("C2DeviceProxy.getRawPortRuntimeInfo", "port: " + port);
            DeviceControlData data = new DeviceControlData();
            data.setData(new Object());
            data.setCmd(DeviceControlData.CMD_GET_UPDATE);
            String cmd = data.toJson();
            String reply = chargerhdNative.chargerhdControl(cmd);
            Log.d("C2DeviceProxy.getRawPortRuntimeInfo", "cmd: " + cmd + ", reply: " + reply);
            return reply;
        } catch (Exception e) {
            Log.e("C2DeviceProxy.getRawPortRuntimeInfo", Log.getStackTraceString(e));
            return null;
        }
    }

    public PortStatus getPortChargeStatus(String port) {
        PortRuntimeData portRuntimeData = getPortRuntimeInfo(port);
        if (portRuntimeData == null) {
            return null;
        }
        return createPortChargeStatusFrom(portRuntimeData);
    }

    public Port getPortRuntimeStatus(String port) {
        PortRuntimeData portRuntimeData = getPortRuntimeInfo(port);
        if (portRuntimeData == null) {
            return null;
        }
        return createPortRuntimeStatusFrom(portRuntimeData);
    }

    public Hardware getHardwareStatus() {
        DeviceBasicInfoData deviceBasicInfoData = getDeviceBasicInfo();
        if (deviceBasicInfoData != null) {
            Hardware hardware = new Hardware();
            hardware.setPid(deviceBasicInfoData.getPID());
            hardware.setSn(SystemPropertiesProxy.get(this.context, "ro.boot.serialno"));
            if ("C2011601CNWHBJCU".equals(hardware.getSn())) {
                hardware.setSn("C2011601CNGZQTCU");
            }
            hardware.setPhase(PHASE.valueBy(Integer.parseInt(deviceBasicInfoData.getPileType())));
            hardware.setAmpCapacity(Integer.parseInt(deviceBasicInfoData.getCurrentMax()));
            hardware.setNetwork(getNetworkStatus());
            HashMap<String, Port> ports = hardware.getPorts();
            if (ports == null) {
                ports = new HashMap<>();
            }
            List<String> portList = deviceBasicInfoData.getPorts();
            for (String portNo : portList) {
                if (ports.get(portNo) == null) {
                    Port port = new Port();
                    port.setPort(portNo);
                    ports.put(portNo, port);
                }
            }
            hardware.setPorts(ports);
            return hardware;
        }
        return null;
    }

    public Software getSoftwareStatus() {
        Software software = new Software();
        software.setOsVer(Build.VERSION.RELEASE);
        software.setFirewareVer(Build.FIRMWARE);
        try {
            String appVer = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName;
            software.setAppVer(appVer);
        } catch (Exception e) {
            Log.e("C2DeviceProxy.getSoftwareStatus", Log.getStackTraceString(e));
        }
        return software;
    }

    public ChargeStatus initChargeStatus() {
        int capacityAmp;
        HashMap<String, PortRuntimeData> portsRuntimeData;
        ChargeStatus chargeStatus = new ChargeStatus();
        boolean hasLocalSetting = false;
        if (LocalSettingCacheProvider.getInstance().hasLocalSetting()) {
            hasLocalSetting = true;
            capacityAmp = LocalSettingCacheProvider.getInstance().getChargeSetting().getAmpCapacity();
            int manufactoryAmpCapacity = LocalSettingCacheProvider.getInstance().getChargeSetting().getManufactoryAmpCapacity();
            if (capacityAmp != manufactoryAmpCapacity) {
                Log.w("C2DeviceProxy.initChargeStatus", "illegal local amp capacity setting !!! manufactoryAmpCapacity: " + manufactoryAmpCapacity + ", work capacityAmp: " + capacityAmp);
                capacityAmp = manufactoryAmpCapacity;
                LocalSettingCacheProvider.getInstance().getChargeSetting().setAmpCapacity(manufactoryAmpCapacity);
                LocalSettingCacheProvider.getInstance().persist();
            }
            int adjustAmp = LocalSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp();
            if (adjustAmp > capacityAmp) {
                Log.w("C2DeviceProxy.initChargeStatus", "illegal local amp adjust setting !!! adjustAmp: " + adjustAmp + ", capacityAmp: " + capacityAmp);
                LocalSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(capacityAmp);
                LocalSettingCacheProvider.getInstance().persist();
            }
        } else {
            int deivceCapacityAmp = new BigDecimal(HardwareStatusCacheProvider.getInstance().getAmpCapacity()).setScale(0, 4).intValue();
            int manufactoryAmpCapacity2 = LocalSettingCacheProvider.getInstance().getChargeSetting().getManufactoryAmpCapacity();
            if (deivceCapacityAmp != manufactoryAmpCapacity2) {
                manufactoryAmpCapacity2 = deivceCapacityAmp;
                LocalSettingCacheProvider.getInstance().getChargeSetting().setManufactoryAmpCapacity(manufactoryAmpCapacity2);
            }
            capacityAmp = LocalSettingCacheProvider.getInstance().getChargeSetting().getAmpCapacity();
            if (capacityAmp != manufactoryAmpCapacity2) {
                Log.w("C2DeviceProxy.initChargeStatus", "illegal local amp capacity default setting !!! manufactoryAmpCapacity: " + manufactoryAmpCapacity2 + ", work capacityAmp: " + capacityAmp);
                capacityAmp = manufactoryAmpCapacity2;
                LocalSettingCacheProvider.getInstance().getChargeSetting().setAmpCapacity(manufactoryAmpCapacity2);
            }
            int adjustAmp2 = LocalSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp();
            if (adjustAmp2 > capacityAmp) {
                Log.w("C2DeviceProxy.initChargeStatus", "illegal local amp adjust default setting !!! adjustAmp: " + adjustAmp2 + ", capacityAmp: " + capacityAmp);
                LocalSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(capacityAmp);
            }
        }
        boolean hasRemoteSetting = false;
        if (RemoteSettingCacheProvider.getInstance().hasRemoteSetting()) {
            hasRemoteSetting = true;
        }
        ChargeSetting remoteChargeSetting = RemoteSettingCacheProvider.getInstance().getChargeSetting();
        ChargeSetting localChargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
        boolean needPersist = false;
        int adjustAmp3 = remoteChargeSetting.getAdjustAmp();
        if (adjustAmp3 > capacityAmp) {
            Log.w("C2DeviceProxy.initChargeStatus", "illegal remote amp adjust setting !!! adjustAmp: " + adjustAmp3 + ", capacityAmp: " + capacityAmp);
            remoteChargeSetting.setAdjustAmp(capacityAmp);
            if (hasRemoteSetting) {
                needPersist = true;
            }
        }
        if (remoteChargeSetting.getManufactoryAmpCapacity() != localChargeSetting.getManufactoryAmpCapacity()) {
            remoteChargeSetting.setManufactoryAmpCapacity(localChargeSetting.getManufactoryAmpCapacity());
            if (hasRemoteSetting) {
                needPersist = true;
            }
        }
        if (remoteChargeSetting.getAmpCapacity() != localChargeSetting.getAmpCapacity()) {
            remoteChargeSetting.setAmpCapacity(localChargeSetting.getAmpCapacity());
            if (hasRemoteSetting) {
                needPersist = true;
            }
        }
        RemoteSettingCacheProvider.getInstance().updateChargeSetting(remoteChargeSetting);
        if (needPersist) {
            RemoteSettingCacheProvider.getInstance().persist();
        }
        int adjustAmp4 = 0;
        DeviceRuntimeData deviceRuntimeData = getDeviceRuntimeInfo();
        if (deviceRuntimeData != null && (portsRuntimeData = deviceRuntimeData.getPortsInfo()) != null) {
            HashMap<String, PortStatus> portsStatus = new HashMap<>();
            if (hasRemoteSetting) {
                adjustAmp4 = RemoteSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp();
            } else if (hasLocalSetting) {
                adjustAmp4 = LocalSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp();
            }
            for (Map.Entry<String, PortRuntimeData> entry : portsRuntimeData.entrySet()) {
                String port = entry.getKey();
                PortRuntimeData portRuntimeData = entry.getValue();
                int standardCp = portRuntimeData.getCpVoltage() == null ? 12000 : portRuntimeData.getCpVoltage().intValue();
                if (isPlugoutCp(standardCp)) {
                    HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(port, false);
                } else {
                    HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(port, true);
                }
                PortStatus portStatus = new PortStatus();
                portStatus.setPortRuntimeStatus(DEVICE_STATUS.valueBy(portRuntimeData.getStatus().intValue()));
                portStatus.setAmmeter(portRuntimeData.getEnergy());
                Double[] dArr = new Double[4];
                dArr[0] = Double.valueOf(portRuntimeData.getCurrent() == null ? 0.0d : portRuntimeData.getCurrent().doubleValue());
                dArr[1] = Double.valueOf(portRuntimeData.getCurrentA() == null ? 0.0d : portRuntimeData.getCurrentA().doubleValue());
                dArr[2] = Double.valueOf(portRuntimeData.getCurrentB() == null ? 0.0d : portRuntimeData.getCurrentB().doubleValue());
                dArr[3] = Double.valueOf(portRuntimeData.getCurrentC() == null ? 0.0d : portRuntimeData.getCurrentC().doubleValue());
                portStatus.setAmps(new ArrayList<>(Arrays.asList(dArr)));
                Double[] dArr2 = new Double[3];
                dArr2[0] = Double.valueOf(portRuntimeData.getVoltageA() == null ? 0.0d : portRuntimeData.getVoltageA().doubleValue());
                dArr2[1] = Double.valueOf(portRuntimeData.getVoltageB() == null ? 0.0d : portRuntimeData.getVoltageB().doubleValue());
                dArr2[2] = Double.valueOf(portRuntimeData.getVoltageC() == null ? 0.0d : portRuntimeData.getVoltageC().doubleValue());
                portStatus.setVolts(new ArrayList<>(Arrays.asList(dArr2)));
                PortSetting portSetting = null;
                if (hasRemoteSetting) {
                    portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(port);
                } else if (hasLocalSetting) {
                    portSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting(port);
                }
                if (portSetting != null) {
                    if (adjustAmp4 == 0) {
                        adjustAmp4 = new BigDecimal((portRuntimeData.getCurrentUsed() == null ? 6 : portRuntimeData.getCurrentUsed().intValue()) / (portSetting.getAmpPercent().intValue() / 10000.0f)).setScale(0, 4).intValue();
                    }
                    portStatus.setAmpPercent(portSetting.getAmpPercent().intValue());
                    GUN_LOCK_MODE portLockMode = portSetting.getGunLockSetting().getMode();
                    if (portLockMode != null) {
                        portStatus.setGunLockMode(portLockMode);
                        if (GUN_LOCK_MODE.disable.equals(portLockMode)) {
                            portStatus.setGunLockStatus(LOCK_STATUS.disable);
                        } else {
                            portStatus.setGunLockStatus(LOCK_STATUS.unlock);
                        }
                    }
                    portStatus.setEnable(portSetting.isEnable());
                    RadarSetting radarSetting = portSetting.getRadarSetting();
                    HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(port, radarSetting.isEnable());
                } else {
                    int portsNum = HardwareStatusCacheProvider.getInstance().getPorts().size();
                    int ampPercent = 10000 / portsNum;
                    if (adjustAmp4 == 0) {
                        adjustAmp4 = (portRuntimeData.getCurrentUsed() == null ? 6 : portRuntimeData.getCurrentUsed().intValue()) * portsNum;
                    }
                    portStatus.setAmpPercent(ampPercent);
                }
                portsStatus.put(port, portStatus);
            }
            chargeStatus.setAdjustAmp(adjustAmp4);
            chargeStatus.setPortsStatus(portsStatus);
        }
        WORK_MODE workMode = chargeStatus.getWorkMode();
        int cpRange = chargeStatus.getCpRange();
        int voltageRange = chargeStatus.getVoltageRange();
        Integer leakageTolerance = chargeStatus.getLeakageTolerance();
        Boolean earthDisable = chargeStatus.isEarthDisable();
        if (hasLocalSetting) {
            workMode = LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode();
        }
        if (hasRemoteSetting) {
            cpRange = RemoteSettingCacheProvider.getInstance().getChargeSetting().getCpRange();
            voltageRange = RemoteSettingCacheProvider.getInstance().getChargeSetting().getVoltageRange();
        } else if (hasLocalSetting) {
            cpRange = LocalSettingCacheProvider.getInstance().getChargeSetting().getCpRange();
            voltageRange = LocalSettingCacheProvider.getInstance().getChargeSetting().getVoltageRange();
        }
        if (hasRemoteSetting) {
            leakageTolerance = RemoteSettingCacheProvider.getInstance().getChargeSetting().getLeakageTolerance();
            earthDisable = RemoteSettingCacheProvider.getInstance().getChargeSetting().isEarthDisable();
        }
        if (capacityAmp == 0) {
            capacityAmp = new BigDecimal(HardwareStatusCacheProvider.getInstance().getAmpCapacity()).setScale(0, 4).intValue();
        }
        chargeStatus.setWorkMode(workMode);
        chargeStatus.setAmpCapacity(capacityAmp);
        chargeStatus.setCpRange(cpRange);
        chargeStatus.setVoltageRange(voltageRange);
        chargeStatus.setLeakageTolerance(leakageTolerance);
        chargeStatus.setEarthDisable(earthDisable);
        return chargeStatus;
    }

    public void initSystemSettingPortsCache() {
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        HashMap<String, SwipeCardPermission> portsSwipeCardPermission = SystemSettingCacheProvider.getInstance().getPortsSwipeCardPermission();
        if (portsSwipeCardPermission == null) {
            portsSwipeCardPermission = new HashMap<>();
        }
        for (String port : ports.keySet()) {
            if (!portsSwipeCardPermission.containsKey(port)) {
                portsSwipeCardPermission.put(port, new SwipeCardPermission());
            }
        }
        SystemSettingCacheProvider.getInstance().updatePortsSwipeCardPermission(portsSwipeCardPermission);
    }

    public void initLocalSettingPortsCache() {
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        HashMap<String, PortSetting> portsSetting = LocalSettingCacheProvider.getInstance().getChargePortsSetting();
        if (portsSetting == null) {
            portsSetting = new HashMap<>();
        }
        int portsNum = ports.size();
        int ampPercent = 10000 / portsNum;
        for (String port : ports.keySet()) {
            PortSetting portSetting = portsSetting.get(port);
            if (portSetting == null) {
                portSetting = new PortSetting();
            }
            if (portSetting.getAmpPercent() == null) {
                portSetting.setAmpPercent(Integer.valueOf(ampPercent));
            } else if (portSetting.getAmpPercent().intValue() == 0) {
                portSetting.setAmpPercent(Integer.valueOf(ampPercent));
            }
            portsSetting.put(port, portSetting);
        }
        LocalSettingCacheProvider.getInstance().updateChargePortsSetting(portsSetting);
    }

    public void initRemoteSettingPortsCache() {
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        HashMap<String, PortSetting> portsSetting = RemoteSettingCacheProvider.getInstance().getChargePortsSetting();
        if (portsSetting == null) {
            portsSetting = new HashMap<>();
        }
        int portsNum = ports.size();
        int ampPercent = 10000 / portsNum;
        for (String port : ports.keySet()) {
            PortSetting portSetting = portsSetting.get(port);
            if (portSetting == null) {
                portSetting = new PortSetting();
            }
            if (portSetting.getAmpPercent() == null) {
                portSetting.setAmpPercent(Integer.valueOf(ampPercent));
            } else if (portSetting.getAmpPercent().intValue() == 0) {
                portSetting.setAmpPercent(Integer.valueOf(ampPercent));
            }
            portsSetting.put(port, portSetting);
        }
        RemoteSettingCacheProvider.getInstance().updateChargePortsSetting(portsSetting);
    }

    public PortStatus createPortChargeStatusFrom(PortRuntimeData data) {
        return data.toPortStatus();
    }

    private Port createPortRuntimeStatusFrom(PortRuntimeData data) {
        return data.toPort();
    }

    private boolean isPlugoutCp(int cp) {
        return cp == 12000;
    }

    public void notifyPortStatusUpdatedByCmd(Port portStatus) {
        C2DeviceEventDispatcher.getInstance().notifyPortStatusUpdatedByCmd(portStatus);
    }
}