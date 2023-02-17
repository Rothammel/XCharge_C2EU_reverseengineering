package com.xcharge.charger.protocol.anyo.bean.response;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

/* loaded from: classes.dex */
public class UnlockPortResponse extends AnyoMessage {
    private byte portNo = 0;

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo) {
        this.portNo = portNo;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = {this.portNo};
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}