package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class AuthResponse extends AnyoMessage {
    private int userId = 0;
    private int billId = 0;
    private long balance = 0;

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBillId() {
        return this.billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public long getBalance() {
        return this.balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[12];
        System.arraycopy(EndianUtils.intToLittleBytes(this.userId), 0, bytes, 0, 4);
        System.arraycopy(EndianUtils.intToLittleBytes(this.billId), 0, bytes, 4, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.balance & XMSZHead.ID_BROADCAST)), 0, bytes, 8, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 12) {
            Log.e("AuthResponse.bodyFromBytes", "body length must be 12 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.userId = EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
        this.billId = EndianUtils.littleBytesToInt(new byte[]{bytes[4], bytes[5], bytes[6], bytes[7]});
        this.balance = EndianUtils.littleBytesToInt(new byte[]{bytes[8], bytes[9], bytes[10], bytes[11]}) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
