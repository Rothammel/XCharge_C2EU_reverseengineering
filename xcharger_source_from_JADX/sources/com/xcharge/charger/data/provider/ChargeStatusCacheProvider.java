package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.setting.ChargeFullRequirement;
import com.xcharge.charger.data.bean.setting.TimerSetting;
import com.xcharge.charger.data.bean.status.ChargeStatus;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.AMP_DISTR_POLICY;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class ChargeStatusCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.status";
    public static final Uri CONTENT_URI = Uri.parse("content://com.xcharge.charger.data.provider.status/charge");
    public static final String PATH = "charge";
    private static ChargeStatusCacheProvider instance = null;
    private AtomicReference<ChargeStatus> cache = new AtomicReference<>(new ChargeStatus());
    private Context context = null;
    private ContentResolver resolver = null;

    public static ChargeStatusCacheProvider getInstance() {
        if (instance == null) {
            instance = new ChargeStatusCacheProvider();
        }
        return instance;
    }

    public void init(Context context2, ChargeStatus status) {
        this.context = context2;
        this.resolver = this.context.getContentResolver();
        if (status != null) {
            this.cache.set(status);
        }
    }

    public void destroy() {
    }

    public Uri getUriFor(String subPath) {
        String path = "charge";
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.status/" + path);
    }

    private void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, (ContentObserver) null);
    }

    public synchronized ChargeStatus getChargeStatus() {
        return this.cache.get();
    }

    public synchronized HashMap<String, PortStatus> getPortsStatus() {
        return this.cache.get().getPortsStatus();
    }

    public synchronized PortStatus getPortStatus(String port) {
        PortStatus portStatus;
        PortStatus portStatus2;
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null || (portStatus2 = portsStatus.get(port)) == null) {
            portStatus = null;
        } else {
            portStatus = portStatus2.clone();
        }
        return portStatus;
    }

    public synchronized boolean updatePortsStatus(HashMap<String, PortStatus> status) {
        this.cache.get().setPortsStatus(status);
        notifyChange(getUriFor("ports"));
        return true;
    }

    public synchronized boolean updatePortStatus(String port, PortStatus status) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        portsStatus.put(port, status);
        this.cache.get().setPortsStatus(portsStatus);
        notifyChange(getUriFor("ports/" + port));
        return true;
    }

    public synchronized boolean updatePortQrcodeContent(String port, String qrcodeContent) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        PortStatus status = portsStatus.get(port);
        if (status == null) {
            status = new PortStatus();
        }
        status.setQrcodeContent(qrcodeContent);
        portsStatus.put(port, status);
        this.cache.get().setPortsStatus(portsStatus);
        notifyChange(getUriFor("ports/qrcode/" + port));
        return true;
    }

    public synchronized HashMap<String, FeeRate> getPortsFeeRate() {
        return this.cache.get().getPortsFeeRate();
    }

    public synchronized FeeRate getPortFeeRate(String port) {
        FeeRate feeRate;
        HashMap<String, FeeRate> portsFeeRate = this.cache.get().getPortsFeeRate();
        if (portsFeeRate != null) {
            feeRate = portsFeeRate.get(port);
        } else {
            feeRate = null;
        }
        return feeRate;
    }

    public synchronized boolean updatePortsFeeRate(HashMap<String, FeeRate> feeRate) {
        this.cache.get().setPortsFeeRate(feeRate);
        notifyChange(getUriFor("feerate/ports"));
        return true;
    }

    public synchronized boolean updatePortFeeRate(String port, FeeRate feeRate) {
        HashMap<String, FeeRate> portsFeeRate = this.cache.get().getPortsFeeRate();
        if (portsFeeRate == null) {
            portsFeeRate = new HashMap<>();
        }
        portsFeeRate.put(port, feeRate);
        this.cache.get().setPortsFeeRate(portsFeeRate);
        notifyChange(getUriFor("feerate/ports/" + port));
        return true;
    }

    public synchronized WORK_MODE getWorkMode() {
        return this.cache.get().getWorkMode();
    }

    public synchronized boolean updateWorkMode(WORK_MODE mode) {
        this.cache.get().setWorkMode(mode);
        notifyChange(getUriFor("workmode"));
        return true;
    }

    public synchronized int getCPRange() {
        return this.cache.get().getCpRange();
    }

    public synchronized boolean updateCPRange(int percent) {
        this.cache.get().setCpRange(percent);
        notifyChange(getUriFor("cp/range"));
        return true;
    }

    public synchronized int getVoltageRange() {
        return this.cache.get().getVoltageRange();
    }

    public synchronized boolean updateVoltageRange(int percent) {
        this.cache.get().setVoltageRange(percent);
        notifyChange(getUriFor("voltage/range"));
        return true;
    }

    public synchronized Integer getLeakageTolerance() {
        return this.cache.get().getLeakageTolerance();
    }

    public synchronized boolean updateLeakageTolerance(Integer mamp) {
        this.cache.get().setLeakageTolerance(mamp);
        notifyChange(getUriFor("leakage/tolerance"));
        return true;
    }

    public synchronized Boolean isEarthDisable() {
        return this.cache.get().isEarthDisable();
    }

    public synchronized boolean updateEarthDisable(Boolean isDisable) {
        this.cache.get().setEarthDisable(isDisable);
        notifyChange(getUriFor("earth/switch"));
        return true;
    }

    public synchronized int getAmpCapacity() {
        return this.cache.get().getAmpCapacity();
    }

    public synchronized boolean updateAmpCapacity(int capacity) {
        this.cache.get().setAmpCapacity(capacity);
        notifyChange(getUriFor("amp/capacity"));
        return true;
    }

    public synchronized int getAdjustAmp() {
        return this.cache.get().getAdjustAmp();
    }

    public synchronized boolean updateAdjustAmp(int adjust) {
        this.cache.get().setAdjustAmp(adjust);
        notifyChange(getUriFor("amp/adjust"));
        return true;
    }

    public synchronized AMP_DISTR_POLICY getAmpDistrPolicy() {
        return this.cache.get().getAmpDistrPolicy();
    }

    public synchronized boolean updateAmpDistrPolicy(AMP_DISTR_POLICY policy) {
        this.cache.get().setAmpDistrPolicy(policy);
        notifyChange(getUriFor("amp/distr"));
        return true;
    }

    public synchronized double getPowerFactor() {
        return this.cache.get().getPowerFactor();
    }

    public synchronized boolean updatePowerFactor(int factor) {
        this.cache.get().setPowerFactor((double) factor);
        notifyChange(getUriFor("power/factor"));
        return true;
    }

    public synchronized ChargeFullRequirement getChargeFullRequirement() {
        return this.cache.get().getChargeFullRequirement();
    }

    public synchronized boolean updateChargeFullRequirement(ChargeFullRequirement require) {
        this.cache.get().setChargeFullRequirement(require);
        notifyChange(getUriFor("full/require"));
        return true;
    }

    public synchronized TimerSetting getChargeTimerSetting() {
        return this.cache.get().getTimerSetting();
    }

    public synchronized boolean updateChargeTimerSetting(TimerSetting timer) {
        this.cache.get().setTimerSetting(timer);
        notifyChange(getUriFor("timer"));
        return true;
    }

    public synchronized DEVICE_STATUS getPortRuntimeStatus(String port) {
        DEVICE_STATUS device_status;
        PortStatus portStatus = getPortStatus(port);
        if (portStatus != null) {
            device_status = portStatus.getPortRuntimeStatus();
        } else {
            device_status = null;
        }
        return device_status;
    }

    public synchronized boolean updatePortRuntimeStatus(String port, DEVICE_STATUS status) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        PortStatus portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new PortStatus();
        }
        portStatus.setPortRuntimeStatus(status);
        portsStatus.put(port, portStatus);
        this.cache.get().setPortsStatus(portsStatus);
        notifyChange(getUriFor("ports/runtime/" + port));
        return true;
    }

    public synchronized GUN_LOCK_MODE getPortLockMode(String port) {
        GUN_LOCK_MODE gun_lock_mode;
        PortStatus portStatus = getPortStatus(port);
        if (portStatus != null) {
            gun_lock_mode = portStatus.getGunLockMode();
        } else {
            gun_lock_mode = GUN_LOCK_MODE.auto;
        }
        return gun_lock_mode;
    }

    public synchronized boolean updatePortLockMode(String port, GUN_LOCK_MODE mode) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        PortStatus portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new PortStatus();
        }
        GUN_LOCK_MODE nowMode = portStatus.getGunLockMode();
        portStatus.setGunLockMode(mode);
        portsStatus.put(port, portStatus);
        this.cache.get().setPortsStatus(portsStatus);
        if (!nowMode.equals(mode)) {
            notifyChange(getUriFor("ports/lock/mode/" + port));
        }
        return true;
    }

    public synchronized boolean isCloudConnected() {
        return this.cache.get().isCloudConnected();
    }

    public synchronized boolean updateCloudConnected(boolean isConnected) {
        boolean old = this.cache.get().isCloudConnected();
        this.cache.get().setCloudConnected(isConnected);
        if (old != isConnected) {
            notifyChange(getUriFor("cloud/connection"));
        }
        return true;
    }

    public synchronized boolean isCloudTimeSynch() {
        return this.cache.get().isCloudTimeSynch();
    }

    public synchronized boolean updateCloudTimeSynch(boolean isSynch) {
        boolean old = this.cache.get().isCloudTimeSynch();
        this.cache.get().setCloudTimeSynch(isSynch);
        if (old != isSynch) {
            notifyChange(getUriFor("cloud/timeSynch"));
        }
        return true;
    }

    public synchronized boolean getPortSwitch(String port) {
        boolean z;
        PortStatus portStatus = getPortStatus(port);
        if (portStatus != null) {
            z = portStatus.isEnable();
        } else {
            z = true;
        }
        return z;
    }

    public synchronized boolean updatePortSwitch(String port, boolean isEnable) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        PortStatus portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new PortStatus();
        }
        boolean nowPortStatus = portStatus.isEnable();
        portStatus.setEnable(isEnable);
        portsStatus.put(port, portStatus);
        this.cache.get().setPortsStatus(portsStatus);
        if (nowPortStatus != isEnable) {
            notifyChange(getUriFor("ports/enable/" + port));
        }
        return true;
    }

    public synchronized LOCK_STATUS getPortLockStatus(String port) {
        LOCK_STATUS lock_status;
        PortStatus portStatus = getPortStatus(port);
        if (portStatus != null) {
            lock_status = portStatus.getGunLockStatus();
        } else {
            lock_status = LOCK_STATUS.disable;
        }
        return lock_status;
    }

    public synchronized boolean updatePortLockStatus(String port, LOCK_STATUS status) {
        HashMap<String, PortStatus> portsStatus = this.cache.get().getPortsStatus();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        PortStatus portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new PortStatus();
        }
        LOCK_STATUS nowPortGunlockStatus = portStatus.getGunLockStatus();
        portStatus.setGunLockStatus(status);
        portsStatus.put(port, portStatus);
        this.cache.get().setPortsStatus(portsStatus);
        if (!nowPortGunlockStatus.equals(status)) {
            notifyChange(getUriFor("ports/lock/status/" + port));
        }
        return true;
    }

    public synchronized boolean isAdvertEnabled() {
        return this.cache.get().isAdvertEnable();
    }

    public synchronized boolean updateAdvertEnabled(boolean isEnabled) {
        boolean old = this.cache.get().isAdvertEnable();
        this.cache.get().setAdvertEnable(isEnabled);
        if (old != isEnabled) {
            notifyChange(getUriFor("advert/enable"));
        }
        return true;
    }
}
