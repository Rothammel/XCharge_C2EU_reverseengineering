package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class StopChargeRequest extends AnyoMessage {
    private byte portNo = 0;
    private int userId = 0;

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId2) {
        this.userId = userId2;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[5];
        System.arraycopy(EndianUtils.intToLittleBytes(this.userId), 0, bytes, 0, 4);
        bytes[4] = this.portNo;
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 5) {
            Log.e("StopChargeRequest.bodyFromBytes", "body length must be 5 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.userId = EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
        this.portNo = bytes[4];
        return this;
    }
}
