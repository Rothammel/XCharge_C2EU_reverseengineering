package com.xcharge.charger.device.c2.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class DeviceRuntimeData extends JsonBean<DeviceRuntimeData> {
    private HashMap<String, PortRuntimeData> portsInfo = null;

    public HashMap<String, PortRuntimeData> getPortsInfo() {
        return this.portsInfo;
    }

    public void setPortsInfo(HashMap<String, PortRuntimeData> portsInfo) {
        this.portsInfo = portsInfo;
    }
}
