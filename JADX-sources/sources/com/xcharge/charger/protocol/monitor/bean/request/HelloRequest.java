package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPDeviceSystem;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPEthernetAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPIOTAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPMobileAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPSubnode;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPWifiAccess;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class HelloRequest extends JsonBean<HelloRequest> {
    private DDAPDeviceSystem system = null;
    private DDAPMobileAccess mobile = null;
    private DDAPEthernetAccess ethernet = null;
    private DDAPWifiAccess wifi = null;
    private DDAPIOTAccess iot_access = null;
    private List<DDAPSubnode> subset = null;

    public DDAPDeviceSystem getSystem() {
        return this.system;
    }

    public void setSystem(DDAPDeviceSystem system) {
        this.system = system;
    }

    public DDAPMobileAccess getMobile() {
        return this.mobile;
    }

    public void setMobile(DDAPMobileAccess mobile) {
        this.mobile = mobile;
    }

    public DDAPEthernetAccess getEthernet() {
        return this.ethernet;
    }

    public void setEthernet(DDAPEthernetAccess ethernet) {
        this.ethernet = ethernet;
    }

    public DDAPWifiAccess getWifi() {
        return this.wifi;
    }

    public void setWifi(DDAPWifiAccess wifi) {
        this.wifi = wifi;
    }

    public DDAPIOTAccess getIot_access() {
        return this.iot_access;
    }

    public void setIot_access(DDAPIOTAccess iot_access) {
        this.iot_access = iot_access;
    }

    public List<DDAPSubnode> getSubset() {
        return this.subset;
    }

    public void setSubset(List<DDAPSubnode> subset) {
        this.subset = subset;
    }
}
