package com.alibaba.sdk.android.oss.model;

import java.util.Map;

/* loaded from: classes.dex */
public class OSSResult {
    private Long clientCRC;
    private String requestId;
    private Map<String, String> responseHeader;
    private Long serverCRC;
    private int statusCode;

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getResponseHeader() {
        return this.responseHeader;
    }

    public void setResponseHeader(Map<String, String> responseHeader) {
        this.responseHeader = responseHeader;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getClientCRC() {
        return this.clientCRC;
    }

    public void setClientCRC(Long clientCRC) {
        if (clientCRC != null && clientCRC.longValue() != 0) {
            this.clientCRC = clientCRC;
        }
    }

    public Long getServerCRC() {
        return this.serverCRC;
    }

    public void setServerCRC(Long serverCRC) {
        if (serverCRC != null && serverCRC.longValue() != 0) {
            this.serverCRC = serverCRC;
        }
    }
}