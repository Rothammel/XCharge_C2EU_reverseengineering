package com.xcharge.charger.protocol.ocpp.bean.types;

/* loaded from: classes.dex */
public class AuthorizationData {
    private String idTag;
    private IdTagInfo idTagInfo;

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo) {
        this.idTagInfo = idTagInfo;
    }
}
