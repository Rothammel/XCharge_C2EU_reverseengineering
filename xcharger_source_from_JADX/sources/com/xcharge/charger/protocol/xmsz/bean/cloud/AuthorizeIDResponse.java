package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class AuthorizeIDResponse extends XMSZMessage {
    public static final byte RC_ERROR_PWD = 5;
    public static final byte RC_EXPIRED = 2;
    public static final byte RC_INVALID = 3;
    public static final byte RC_OK = 1;
    public static final byte RC_OTHER_CHARGING = 4;
    private long balance = 0;
    private byte returnCode = 1;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode2) {
        this.returnCode = returnCode2;
    }

    public long getBalance() {
        return this.balance;
    }

    public void setBalance(long balance2) {
        this.balance = balance2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[5];
        bytes[0] = this.returnCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.balance & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 5) {
            Log.e("AuthorizeIDResponse.bodyFromBytes", "body length must be 5 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.returnCode = bytes[0];
        this.balance = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]})) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
