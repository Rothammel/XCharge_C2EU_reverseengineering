package com.xcharge.charger.device.adpter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.device.api.PortStatusListener;
import com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;

/* loaded from: classes.dex */
public class DeviceProxy {
    private static DeviceProxy instance = null;
    private Context context = null;

    public static DeviceProxy getInstance() {
        if (instance == null) {
            instance = new DeviceProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void destroy() {
    }

    public void attachPortStatusListener(PortStatusListener listener) {
        C2DeviceEventDispatcher.getInstance().attachPortStatusListener(listener);
    }

    public void dettachPortStatusListener(PortStatusListener listener) {
        C2DeviceEventDispatcher.getInstance().dettachPortStatusListener(listener);
    }

    public void authValid(String port, String type, String uid) {
        C2DeviceProxy.getInstance().authValid(port, type, uid);
    }

    public void authInValid(String port, String type, String uid) {
        C2DeviceProxy.getInstance().authInValid(port, type, uid);
    }

    public void startCharge(String port) {
        C2DeviceProxy.getInstance().startCharge(port);
    }

    public void stopCharge(String port) {
        C2DeviceProxy.getInstance().stopCharge(port);
    }

    public PortStatus getPortChargeStatus(String port) {
        return C2DeviceProxy.getInstance().getPortChargeStatus(port);
    }

    public Port getPortRuntimeStatus(String port) {
        return C2DeviceProxy.getInstance().getPortRuntimeStatus(port);
    }

    public void ajustChargeAmp(String port, int amp) {
        C2DeviceProxy.getInstance().ajustChargeAmp(port, amp);
    }

    public void ajustChargeAmp(int amp) {
        C2DeviceProxy.getInstance().ajustChargeAmp("1", amp);
    }

    public void setAmpCapacity(int amp) {
        C2DeviceProxy.getInstance().setAmpCapacity(amp);
    }

    public void setCPRange(int percent) {
        C2DeviceProxy.getInstance().setCPRange(percent);
    }

    public void setVoltageRange(int percent) {
        C2DeviceProxy.getInstance().setVoltageRange(percent);
    }

    public void setLeakageTolerance(int mamp) {
        C2DeviceProxy.getInstance().setLeakageTolerance(mamp);
    }

    public void setEarthDisable(boolean isDisable) {
        C2DeviceProxy.getInstance().setEarthDisable(isDisable);
    }

    public void openGunLed(String port) {
        C2DeviceProxy.getInstance().openGunLed(port);
    }

    public void closeGunLed(String port) {
        C2DeviceProxy.getInstance().closeGunLed(port);
    }

    public void enableGunLock(String port) {
        C2DeviceProxy.getInstance().enableGunLock(port);
    }

    public void disableGunLock(String port) {
        C2DeviceProxy.getInstance().disableGunLock(port);
    }

    public void setGunLockMode(String port, int mode) {
        C2DeviceProxy.getInstance().setGunLockMode(port, mode);
    }

    public void lockGun(String port) {
        C2DeviceProxy.getInstance().lockGun(port);
    }

    public void unlockGun(String port) {
        C2DeviceProxy.getInstance().unlockGun(port);
    }

    public void beep(int num) {
        C2DeviceProxy.getInstance().beep(num);
    }

    private void setBLN(int color, int time, int mode) {
        C2DeviceProxy.getInstance().setBLN(color, time, mode);
    }

    public void setSystemStatusBLN(String port, int status) {
        int defaultColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        C2DeviceProxy.getInstance().setSystemStatusBLN(port, status, defaultColor);
    }

    public void notifyPortStatusUpdatedByCmd(Port portStatus) {
        C2DeviceProxy.getInstance().notifyPortStatusUpdatedByCmd(portStatus);
    }
}
