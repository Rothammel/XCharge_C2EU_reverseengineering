package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.common.bean.JsonBean;

public class AnyoStatus extends JsonBean<AnyoStatus> {
    private byte deviceStatus = 0;
    private ErrorCode error = new ErrorCode(200);
    private boolean portLocked = false;
    private boolean portPlugin = false;

    public byte getDeviceStatus() {
        return this.deviceStatus;
    }

    public void setDeviceStatus(byte deviceStatus2) {
        this.deviceStatus = deviceStatus2;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error2) {
        this.error = error2;
    }

    public boolean isPortLocked() {
        return this.portLocked;
    }

    public void setPortLocked(boolean portLocked2) {
        this.portLocked = portLocked2;
    }

    public boolean isPortPlugin() {
        return this.portPlugin;
    }

    public void setPortPlugin(boolean portPlugin2) {
        this.portPlugin = portPlugin2;
    }
}
