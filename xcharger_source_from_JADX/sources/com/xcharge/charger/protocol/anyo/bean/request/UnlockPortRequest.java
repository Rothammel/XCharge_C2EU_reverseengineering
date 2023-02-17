package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class UnlockPortRequest extends AnyoMessage {
    private int billId = 0;
    private byte portNo = 0;
    private int userId = 0;

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId2) {
        this.userId = userId2;
    }

    public int getBillId() {
        return this.billId;
    }

    public void setBillId(int billId2) {
        this.billId = billId2;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[9];
        System.arraycopy(EndianUtils.intToLittleBytes(this.userId), 0, bytes, 0, 4);
        System.arraycopy(EndianUtils.intToLittleBytes(this.billId), 0, bytes, 4, 4);
        bytes[8] = this.portNo;
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 9) {
            Log.e("UnlockPortRequest.bodyFromBytes", "body length must be 9 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.userId = EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
        this.billId = EndianUtils.littleBytesToInt(new byte[]{bytes[4], bytes[5], bytes[6], bytes[7]});
        this.portNo = bytes[8];
        return this;
    }
}
