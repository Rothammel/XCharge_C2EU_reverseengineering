package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GetLocalListVersionConf extends JsonBean<GetLocalListVersionConf> {
    private int listVersion;

    public int getListVersion() {
        return this.listVersion;
    }

    public void setListVersion(int listVersion) {
        this.listVersion = listVersion;
    }
}
