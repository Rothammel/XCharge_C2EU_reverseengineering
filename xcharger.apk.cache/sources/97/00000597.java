package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.PARK_LOCK_MODE;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ParkLockSetting extends JsonBean<ParkLockSetting> {
    private PARK_LOCK_MODE mode = PARK_LOCK_MODE.disable;
    private int lockTimeout = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;

    public PARK_LOCK_MODE getMode() {
        return this.mode;
    }

    public void setMode(PARK_LOCK_MODE mode) {
        this.mode = mode;
    }

    public int getLockTimeout() {
        return this.lockTimeout;
    }

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }
}