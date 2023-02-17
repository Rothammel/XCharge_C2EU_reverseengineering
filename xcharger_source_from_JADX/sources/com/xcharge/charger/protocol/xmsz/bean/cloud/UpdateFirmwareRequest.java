package com.xcharge.charger.protocol.xmsz.bean.cloud;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

public class UpdateFirmwareRequest extends XMSZMessage {
    private String location = "";
    private byte retrieves = 0;
    private String version = "";

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location2) {
        this.location = location2;
    }

    public byte getRetrieves() {
        return this.retrieves;
    }

    public void setRetrieves(byte retrieves2) {
        this.retrieves = retrieves2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public byte[] bodyToBytes() throws Exception {
        return null;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
