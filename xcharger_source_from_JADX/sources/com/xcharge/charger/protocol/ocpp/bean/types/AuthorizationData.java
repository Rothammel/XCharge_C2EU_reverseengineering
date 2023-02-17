package com.xcharge.charger.protocol.ocpp.bean.types;

public class AuthorizationData {
    private String idTag;
    private IdTagInfo idTagInfo;

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo2) {
        this.idTagInfo = idTagInfo2;
    }
}
