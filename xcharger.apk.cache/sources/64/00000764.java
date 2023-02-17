package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.AuthorizationData;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SendLocalListReq extends JsonBean<SendLocalListReq> {
    private int listVersion;
    private ArrayList<AuthorizationData> localAuthorisationList;
    private String updateType;

    public int getListVersion() {
        return this.listVersion;
    }

    public void setListVersion(int listVersion) {
        this.listVersion = listVersion;
    }

    public ArrayList<AuthorizationData> getLocalAuthorisationList() {
        return this.localAuthorisationList;
    }

    public void setLocalAuthorisationList(ArrayList<AuthorizationData> localAuthorisationList) {
        this.localAuthorisationList = localAuthorisationList;
    }

    public String getUpdateType() {
        return this.updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }
}