package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.IdTagInfo;
import com.xcharge.common.bean.JsonBean;

public class StopTransactionConf extends JsonBean<StopTransactionConf> {
    private IdTagInfo idTagInfo;

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo2) {
        this.idTagInfo = idTagInfo2;
    }
}
