package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

public class RemoteStartChargingRequest extends XMSZMessage {
    public static final byte CT_BATTERY_PERCENT = 2;
    public static final byte CT_FULL = 0;
    public static final byte CT_MONEY = 3;
    public static final byte CT_TIME = 1;
    private long chargeParam = 0;
    private byte chargeType = 0;
    private byte connectorId = 1;
    private String userIdTag = "";

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId2) {
        this.connectorId = connectorId2;
    }

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag2) {
        this.userIdTag = userIdTag2;
    }

    public byte getChargeType() {
        return this.chargeType;
    }

    public void setChargeType(byte chargeType2) {
        this.chargeType = chargeType2;
    }

    public long getChargeParam() {
        return this.chargeParam;
    }

    public void setChargeParam(long chargeParam2) {
        this.chargeParam = chargeParam2;
    }

    public byte[] bodyToBytes() throws Exception {
        int userIdTagLength = 16;
        byte[] bytes = new byte[22];
        bytes[0] = this.connectorId;
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        if (userIdTagBytes.length <= 16) {
            userIdTagLength = userIdTagBytes.length;
        }
        System.arraycopy(userIdTagBytes, 0, bytes, 1, userIdTagLength);
        bytes[17] = this.chargeType;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeParam & XMSZHead.ID_BROADCAST)), 0, bytes, 18, 4);
        return bytes;
    }

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
        this.chargeParam = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[18], bytes[19], bytes[20], bytes[21]})) & XMSZHead.ID_BROADCAST;
        return this;
    }
}
