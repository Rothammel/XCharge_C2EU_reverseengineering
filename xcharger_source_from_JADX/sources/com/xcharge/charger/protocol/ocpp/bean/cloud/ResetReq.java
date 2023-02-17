package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class ResetReq extends JsonBean<ResetReq> {
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }
}
