package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

public class StartTransactionRequest extends XMSZMessage {
    public static final byte CT_BATTERY_PERCENT = 2;
    public static final byte CT_FULL = 0;
    public static final byte CT_MONEY = 3;
    public static final byte CT_TIME = 1;
    private long chargeParam = 0;
    private byte chargeType = 0;
    private byte connectorId = 1;
    private long expectedStoptime = XMSZHead.ID_BROADCAST;
    private long pointTransactionTag = 0;
    private long startTime = 0;
    private String userIdTag = "";

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag2) {
        this.userIdTag = userIdTag2;
    }

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId2) {
        this.connectorId = connectorId2;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime2) {
        this.startTime = startTime2;
    }

    public long getExpectedStoptime() {
        return this.expectedStoptime;
    }

    public void setExpectedStoptime(long expectedStoptime2) {
        this.expectedStoptime = expectedStoptime2;
    }

    public long getPointTransactionTag() {
        return this.pointTransactionTag;
    }

    public void setPointTransactionTag(long pointTransactionTag2) {
        this.pointTransactionTag = pointTransactionTag2;
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
        byte[] bytes = new byte[34];
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(userIdTagBytes, 0, bytes, 0, userIdTagBytes.length > 16 ? 16 : userIdTagBytes.length);
        bytes[16] = this.connectorId;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.startTime & XMSZHead.ID_BROADCAST)), 0, bytes, 17, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.expectedStoptime & XMSZHead.ID_BROADCAST)), 0, bytes, 21, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.pointTransactionTag & XMSZHead.ID_BROADCAST)), 0, bytes, 25, 4);
        bytes[29] = this.chargeType;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeParam & XMSZHead.ID_BROADCAST)), 0, bytes, 30, 4);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
