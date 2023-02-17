package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class StopTransactionResponse extends XMSZMessage {
    public static final byte RC_FAIL = 2;
    public static final byte RC_OK = 1;
    private byte returnCode = 1;
    private long meterValue = 0;
    private long money = 0;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
    }

    public long getMeterValue() {
        return this.meterValue;
    }

    public void setMeterValue(long meterValue) {
        this.meterValue = meterValue;
    }

    public long getMoney() {
        return this.money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[9];
        bytes[0] = this.returnCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.meterValue & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.money & XMSZHead.ID_BROADCAST)), 0, bytes, 5, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 9) {
            Log.e("StopTransactionResponse.bodyFromBytes", "body length must be 9 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.returnCode = bytes[0];
        this.meterValue = EndianUtils.littleBytesToInt(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]}) & XMSZHead.ID_BROADCAST;
        this.money = EndianUtils.littleBytesToInt(new byte[]{bytes[5], bytes[6], bytes[7], bytes[8]}) & XMSZHead.ID_BROADCAST;
        return this;
    }
}