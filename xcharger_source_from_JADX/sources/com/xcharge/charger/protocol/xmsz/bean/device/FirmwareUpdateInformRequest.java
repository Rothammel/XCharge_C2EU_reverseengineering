package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

public class FirmwareUpdateInformRequest extends XMSZMessage {
    public static final byte STATUS_DOWNLOAD_FAIL = 1;
    public static final byte STATUS_DOWNLOAD_OK = 0;
    public static final byte STATUS_INSTALL_FAIL = 2;
    public static final byte STATUS_INSTALL_OK = 3;
    private byte updateStatus = 3;

    public byte getUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(byte updateStatus2) {
        this.updateStatus = updateStatus2;
    }

    public byte[] bodyToBytes() throws Exception {
        return null;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
