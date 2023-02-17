package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;

public class HeartBeatRequest extends XMSZMessage {
    public byte[] bodyToBytes() throws Exception {
        return new byte[0];
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
