package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class GetLocalListVersionConf extends JsonBean<GetLocalListVersionConf> {
    private int listVersion;

    public int getListVersion() {
        return this.listVersion;
    }

    public void setListVersion(int listVersion2) {
        this.listVersion = listVersion2;
    }
}
