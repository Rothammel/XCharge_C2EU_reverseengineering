package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class RemoteStartChargingRequest extends XMSZMessage {
    public static final byte CT_BATTERY_PERCENT = 2;
    public static final byte CT_FULL = 0;
    public static final byte CT_MONEY = 3;
    public static final byte CT_TIME = 1;
    private byte connectorId = 1;
    private String userIdTag = "";
    private byte chargeType = 0;
    private long chargeParam = 0;

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId) {
        this.connectorId = connectorId;
    }

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag) {
        this.userIdTag = userIdTag;
    }

    public byte getChargeType() {
        return this.chargeType;
    }

    public void setChargeType(byte chargeType) {
        this.chargeType = chargeType;
    }

    public long getChargeParam() {
        return this.chargeParam;
    }

    public void setChargeParam(long chargeParam) {
        this.chargeParam = chargeParam;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[22];
        bytes[0] = this.connectorId;
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        int userIdTagLength = userIdTagBytes.length <= 16 ? userIdTagBytes.length : 16;
        System.arraycopy(userIdTagBytes, 0, bytes, 1, userIdTagLength);
        bytes[17] = this.chargeType;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeParam & XMSZHead.ID_BROADCAST)), 0, bytes, 18, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 22) {
            Log.e("RemoteStartChargingRequest.bodyFromBytes", "body length must be 22 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.connectorId = bytes[0];
        byte[] userIdTagBytes = new byte[16];
        System.arraycopy(bytes, 1, userIdTagBytes, 0, 16);
        this.userIdTag = new String(userIdTagBytes, CHARSET_GBK);
        this.chargeType = bytes[17];
        this.chargeParam = EndianUtils.littleBytesToInt(new byte[]{bytes[18], bytes[19], bytes[20], bytes[21]}) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
