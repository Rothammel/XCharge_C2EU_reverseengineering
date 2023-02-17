package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.PARK_LOCK_MODE;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.bean.JsonBean;

public class ParkLockSetting extends JsonBean<ParkLockSetting> {
    private int lockTimeout = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    private PARK_LOCK_MODE mode = PARK_LOCK_MODE.disable;

    public PARK_LOCK_MODE getMode() {
        return this.mode;
    }

    public void setMode(PARK_LOCK_MODE mode2) {
        this.mode = mode2;
    }

    public int getLockTimeout() {
        return this.lockTimeout;
    }

    public void setLockTimeout(int lockTimeout2) {
        this.lockTimeout = lockTimeout2;
    }
}
