package com.xcharge.charger.protocol.family.xcloud.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

public enum EnumActionStatus {
    received("received"),
    success(YZXProperty.UPGRADE_SUCCESS),
    failed(YZXProperty.UPGRADE_FAILED);
    
    private String status;

    private EnumActionStatus(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
