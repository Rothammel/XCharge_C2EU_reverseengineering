package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class LoginResponse extends AnyoMessage {
    private byte rand = 0;
    private long timestamp = 0;

    public byte getRand() {
        return this.rand;
    }

    public void setRand(byte rand) {
        this.rand = rand;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[5];
        bytes[0] = this.rand;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.timestamp & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 5) {
            Log.e("LoginResponse.bodyFromBytes", "body length must be 5 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.rand = bytes[0];
        this.timestamp = EndianUtils.littleBytesToInt(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]}) & XMSZHead.ID_BROADCAST;
        return this;
    }
}