package com.xcharge.charger.protocol.xmsz.bean.cloud;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

/* loaded from: classes.dex */
public class UpdateFirmwareRequest extends XMSZMessage {
    private String location = "";
    private byte retrieves = 0;
    private String version = "";

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte getRetrieves() {
        return this.retrieves;
    }

    public void setRetrieves(byte retrieves) {
        this.retrieves = retrieves;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        return null;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
