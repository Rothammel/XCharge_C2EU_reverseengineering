package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class AuthorizeReq extends JsonBean<AuthorizeReq> {
    private String idTag;

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }
}
