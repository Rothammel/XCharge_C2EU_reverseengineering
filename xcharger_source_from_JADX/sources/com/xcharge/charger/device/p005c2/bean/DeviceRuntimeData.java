package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.device.c2.bean.DeviceRuntimeData */
public class DeviceRuntimeData extends JsonBean<DeviceRuntimeData> {
    private HashMap<String, PortRuntimeData> portsInfo = null;

    public HashMap<String, PortRuntimeData> getPortsInfo() {
        return this.portsInfo;
    }

    public void setPortsInfo(HashMap<String, PortRuntimeData> portsInfo2) {
        this.portsInfo = portsInfo2;
    }
}
