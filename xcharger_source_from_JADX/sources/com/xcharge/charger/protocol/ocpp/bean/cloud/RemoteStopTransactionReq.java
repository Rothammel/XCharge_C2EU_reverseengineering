package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class RemoteStopTransactionReq extends JsonBean<RemoteStopTransactionReq> {
    private int transactionId;

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId2) {
        this.transactionId = transactionId2;
    }
}
