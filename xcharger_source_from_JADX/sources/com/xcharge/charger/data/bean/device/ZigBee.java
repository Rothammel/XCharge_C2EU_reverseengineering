package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ZigBee extends JsonBean<ZigBee> {
    private boolean fault = false;
    private String ifName = null;
    private String type = Network.NETWORK_TYPE_ZIGBEE;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault2) {
        this.fault = fault2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName2) {
        this.ifName = ifName2;
    }
}
