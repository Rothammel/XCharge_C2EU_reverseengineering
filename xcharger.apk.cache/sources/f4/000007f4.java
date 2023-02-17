package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

/* loaded from: classes.dex */
public class FirmwareUpdateInformRequest extends XMSZMessage {
    public static final byte STATUS_DOWNLOAD_FAIL = 1;
    public static final byte STATUS_DOWNLOAD_OK = 0;
    public static final byte STATUS_INSTALL_FAIL = 2;
    public static final byte STATUS_INSTALL_OK = 3;
    private byte updateStatus = 3;

    public byte getUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(byte updateStatus) {
        this.updateStatus = updateStatus;
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