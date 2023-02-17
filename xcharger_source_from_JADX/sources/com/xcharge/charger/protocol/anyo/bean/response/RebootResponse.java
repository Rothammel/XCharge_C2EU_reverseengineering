package com.xcharge.charger.protocol.anyo.bean.response;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;

public class RebootResponse extends AnyoMessage {

    /* renamed from: ts */
    private long f77ts = 0;

    public long getTs() {
        return this.f77ts;
    }

    public void setTs(long ts) {
        this.f77ts = ts;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[4];
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.f77ts & -1)), 0, bytes, 0, 4);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
