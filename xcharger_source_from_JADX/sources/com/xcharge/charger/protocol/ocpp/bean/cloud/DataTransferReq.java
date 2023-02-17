package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class DataTransferReq extends JsonBean<DataTransferReq> {
    private String data;
    private String messageId;
    private String vendorId;

    public String getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(String vendorId2) {
        this.vendorId = vendorId2;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String messageId2) {
        this.messageId = messageId2;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data2) {
        this.data = data2;
    }
}
