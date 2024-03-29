package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class BootNotificationResponse extends XMSZMessage {
    public static final byte RC_DISABLE = 0;
    public static final byte RC_ENABLE = 1;
    public static final byte RC_SRV_OK = 2;
    private byte returnCode = 1;
    private long heartBeatInterval = 0;
    private long pointId = 0;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
    }

    public long getHeartBeatInterval() {
        return this.heartBeatInterval;
    }

    public void setHeartBeatInterval(long heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public long getPointId() {
        return this.pointId;
    }

    public void setPointId(long pointId) {
        this.pointId = pointId;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[9];
        bytes[0] = this.returnCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.heartBeatInterval & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.pointId & XMSZHead.ID_BROADCAST)), 0, bytes, 5, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 9) {
            Log.e("BootNotificationResponse.bodyFromBytes", "body length must be 9 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.returnCode = bytes[0];
        this.heartBeatInterval = EndianUtils.littleBytesToInt(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]}) & XMSZHead.ID_BROADCAST;
        this.pointId = EndianUtils.littleBytesToInt(new byte[]{bytes[5], bytes[6], bytes[7], bytes[8]}) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
