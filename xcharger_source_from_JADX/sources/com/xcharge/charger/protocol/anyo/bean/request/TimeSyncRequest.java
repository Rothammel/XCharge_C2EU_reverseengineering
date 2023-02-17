package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class TimeSyncRequest extends AnyoMessage {
    private long timestamp = 0;

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp2) {
        this.timestamp = timestamp2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[4];
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.timestamp & XMSZHead.ID_BROADCAST)), 0, bytes, 0, 4);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 4) {
            Log.e("TimeSyncRequest.bodyFromBytes", "body length must be 4 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.timestamp = ((long) EndianUtils.littleBytesToInt(bytes)) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
