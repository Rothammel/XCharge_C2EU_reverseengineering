package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

public class ResetResponse extends XMSZMessage {
    public static final byte RC_CRC_ERROR = 2;
    public static final byte RC_EFFECTIVE_NEXT = 3;
    public static final byte RC_EFFECTIVE_NOW = 1;
    public static final byte RC_FAIL = 0;
    private byte returnCode = 1;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode2) {
        this.returnCode = returnCode2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.returnCode};
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
