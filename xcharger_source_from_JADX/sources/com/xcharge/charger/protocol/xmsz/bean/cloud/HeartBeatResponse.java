package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class HeartBeatResponse extends XMSZMessage {
    public static final byte RC_FAIL = 2;
    public static final byte RC_OK = 1;
    private byte returnCode = 1;
    private long time = 0;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode2) {
        this.returnCode = returnCode2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[5];
        bytes[0] = this.returnCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.time & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 5) {
            Log.e("HeartBeatResponse.bodyFromBytes", "body length must be 5 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.returnCode = bytes[0];
        this.time = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]})) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
