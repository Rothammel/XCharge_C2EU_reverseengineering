package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPDeviceSystem;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPEthernetAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPIOTAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPMobileAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPSubnode;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPWifiAccess;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

public class HelloRequest extends JsonBean<HelloRequest> {
    private DDAPEthernetAccess ethernet = null;
    private DDAPIOTAccess iot_access = null;
    private DDAPMobileAccess mobile = null;
    private List<DDAPSubnode> subset = null;
    private DDAPDeviceSystem system = null;
    private DDAPWifiAccess wifi = null;

    public DDAPDeviceSystem getSystem() {
        return this.system;
    }

    public void setSystem(DDAPDeviceSystem system2) {
        this.system = system2;
    }

    public DDAPMobileAccess getMobile() {
        return this.mobile;
    }

    public void setMobile(DDAPMobileAccess mobile2) {
        this.mobile = mobile2;
    }

    public DDAPEthernetAccess getEthernet() {
        return this.ethernet;
    }

    public void setEthernet(DDAPEthernetAccess ethernet2) {
        this.ethernet = ethernet2;
    }

    public DDAPWifiAccess getWifi() {
        return this.wifi;
    }

    public void setWifi(DDAPWifiAccess wifi2) {
        this.wifi = wifi2;
    }

    public DDAPIOTAccess getIot_access() {
        return this.iot_access;
    }

    public void setIot_access(DDAPIOTAccess iot_access2) {
        this.iot_access = iot_access2;
    }

    public List<DDAPSubnode> getSubset() {
        return this.subset;
    }

    public void setSubset(List<DDAPSubnode> subset2) {
        this.subset = subset2;
    }
}
