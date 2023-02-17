package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.AuthorizationData;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class SendLocalListReq extends JsonBean<SendLocalListReq> {
    private int listVersion;
    private ArrayList<AuthorizationData> localAuthorisationList;
    private String updateType;

    public int getListVersion() {
        return this.listVersion;
    }

    public void setListVersion(int listVersion2) {
        this.listVersion = listVersion2;
    }

    public ArrayList<AuthorizationData> getLocalAuthorisationList() {
        return this.localAuthorisationList;
    }

    public void setLocalAuthorisationList(ArrayList<AuthorizationData> localAuthorisationList2) {
        this.localAuthorisationList = localAuthorisationList2;
    }

    public String getUpdateType() {
        return this.updateType;
    }

    public void setUpdateType(String updateType2) {
        this.updateType = updateType2;
    }
}
