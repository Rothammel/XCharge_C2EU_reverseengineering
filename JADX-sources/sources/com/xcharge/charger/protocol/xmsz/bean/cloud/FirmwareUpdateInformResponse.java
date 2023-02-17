package com.xcharge.charger.protocol.xmsz.bean.cloud;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

/* loaded from: classes.dex */
public class FirmwareUpdateInformResponse extends XMSZMessage {
    public static final byte RC_BUSY = 3;
    public static final byte RC_CRC_ERROR = 2;
    public static final byte RC_FAIL = 0;
    public static final byte RC_OK = 1;
    private byte returnCode = 1;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
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
