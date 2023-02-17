package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.IdTagInfo;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class StartTransactionConf extends JsonBean<StartTransactionConf> {
    private IdTagInfo idTagInfo;
    private int transactionId;

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo) {
        this.idTagInfo = idTagInfo;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
