package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class RebootRequest extends AnyoMessage {

    /* renamed from: ts */
    private long f75ts = 0;

    public long getTs() {
        return this.f75ts;
    }

    public void setTs(long ts) {
        this.f75ts = ts;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[4];
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.f75ts & -1)), 0, bytes, 0, 4);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 4) {
            Log.e("RebootRequest.bodyFromBytes", "body length must be 4 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.f75ts = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]})) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
