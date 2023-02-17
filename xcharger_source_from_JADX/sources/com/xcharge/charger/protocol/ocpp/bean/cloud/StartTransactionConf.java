package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.IdTagInfo;
import com.xcharge.common.bean.JsonBean;

public class StartTransactionConf extends JsonBean<StartTransactionConf> {
    private IdTagInfo idTagInfo;
    private int transactionId;

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo2) {
        this.idTagInfo = idTagInfo2;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId2) {
        this.transactionId = transactionId2;
    }
}
