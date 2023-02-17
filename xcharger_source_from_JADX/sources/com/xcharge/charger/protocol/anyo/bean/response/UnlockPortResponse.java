package com.xcharge.charger.protocol.anyo.bean.response;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

public class UnlockPortResponse extends AnyoMessage {
    private byte portNo = 0;

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.portNo};
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
