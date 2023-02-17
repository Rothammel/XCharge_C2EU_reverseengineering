package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

/* loaded from: classes.dex */
public class ResetResponse extends XMSZMessage {
    public static final byte RC_CRC_ERROR = 2;
    public static final byte RC_EFFECTIVE_NEXT = 3;
    public static final byte RC_EFFECTIVE_NOW = 1;
    public static final byte RC_FAIL = 0;
    private byte returnCode = 1;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.returnCode};
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}