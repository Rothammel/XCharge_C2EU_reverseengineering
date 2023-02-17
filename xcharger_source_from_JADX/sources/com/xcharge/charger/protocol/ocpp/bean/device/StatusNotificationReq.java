package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class StatusNotificationReq extends JsonBean<StatusNotificationReq> {
    private int connectorId;
    private String errorCode;
    private String info;
    private String status;
    private String timestamp;
    private String vendorErrorCode;
    private String vendorId;

    public int getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(int connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode2) {
        this.errorCode = errorCode2;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info2) {
        this.info = info2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }

    public String getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(String vendorId2) {
        this.vendorId = vendorId2;
    }

    public String getVendorErrorCode() {
        return this.vendorErrorCode;
    }

    public void setVendorErrorCode(String vendorErrorCode2) {
        this.vendorErrorCode = vendorErrorCode2;
    }
}
