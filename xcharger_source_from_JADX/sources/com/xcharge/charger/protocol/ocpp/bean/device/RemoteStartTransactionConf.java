package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class RemoteStartTransactionConf extends JsonBean<RemoteStartTransactionConf> {
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }
}
