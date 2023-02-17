package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AnyoStatus extends JsonBean<AnyoStatus> {
    private byte deviceStatus = 0;
    private ErrorCode error = new ErrorCode(200);
    private boolean portLocked = false;
    private boolean portPlugin = false;

    public byte getDeviceStatus() {
        return this.deviceStatus;
    }

    public void setDeviceStatus(byte deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public boolean isPortLocked() {
        return this.portLocked;
    }

    public void setPortLocked(boolean portLocked) {
        this.portLocked = portLocked;
    }

    public boolean isPortPlugin() {
        return this.portPlugin;
    }

    public void setPortPlugin(boolean portPlugin) {
        this.portPlugin = portPlugin;
    }
}