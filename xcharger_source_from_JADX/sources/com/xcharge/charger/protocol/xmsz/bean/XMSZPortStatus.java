package com.xcharge.charger.protocol.xmsz.bean;

import com.xcharge.common.bean.JsonBean;

public class XMSZPortStatus extends JsonBean<XMSZPortStatus> {
    private byte portError = 4;
    private boolean portLocked = false;
    private boolean portPlugin = false;
    private byte portStatus = 0;

    public byte getPortStatus() {
        return this.portStatus;
    }

    public void setPortStatus(byte portStatus2) {
        this.portStatus = portStatus2;
    }

    public byte getPortError() {
        return this.portError;
    }

    public void setPortError(byte portError2) {
        this.portError = portError2;
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
