package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ZigBee extends JsonBean<ZigBee> {
    private String type = Network.NETWORK_TYPE_ZIGBEE;
    private String ifName = null;
    private boolean fault = false;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault) {
        this.fault = fault;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName) {
        this.ifName = ifName;
    }
}
