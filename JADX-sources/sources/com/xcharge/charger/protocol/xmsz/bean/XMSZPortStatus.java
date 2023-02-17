package com.xcharge.charger.protocol.xmsz.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XMSZPortStatus extends JsonBean<XMSZPortStatus> {
    private byte portStatus = 0;
    private byte portError = 4;
    private boolean portLocked = false;
    private boolean portPlugin = false;

    public byte getPortStatus() {
        return this.portStatus;
    }

    public void setPortStatus(byte portStatus) {
        this.portStatus = portStatus;
    }

    public byte getPortError() {
        return this.portError;
    }

    public void setPortError(byte portError) {
        this.portError = portError;
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
