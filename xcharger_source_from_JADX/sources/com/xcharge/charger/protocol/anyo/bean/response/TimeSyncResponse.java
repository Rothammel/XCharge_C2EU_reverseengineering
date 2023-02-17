package com.xcharge.charger.protocol.anyo.bean.response;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;

public class TimeSyncResponse extends AnyoMessage {
    private long timestamp = 0;

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp2) {
        this.timestamp = timestamp2;
    }

    public byte[] bodyToBytes() throws Exception {
        return EndianUtils.intToLittleBytes((int) (this.timestamp & XMSZHead.ID_BROADCAST));
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
