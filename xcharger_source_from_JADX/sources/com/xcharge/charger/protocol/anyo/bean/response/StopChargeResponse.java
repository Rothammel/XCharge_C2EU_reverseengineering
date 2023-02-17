package com.xcharge.charger.protocol.anyo.bean.response;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

public class StopChargeResponse extends AnyoMessage {
    public byte[] bodyToBytes() throws Exception {
        return new byte[0];
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
