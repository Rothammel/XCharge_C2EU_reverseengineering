package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

/* loaded from: classes.dex */
public class StartTransactionRequest extends XMSZMessage {
    public static final byte CT_BATTERY_PERCENT = 2;
    public static final byte CT_FULL = 0;
    public static final byte CT_MONEY = 3;
    public static final byte CT_TIME = 1;
    private String userIdTag = "";
    private byte connectorId = 1;
    private long startTime = 0;
    private long expectedStoptime = XMSZHead.ID_BROADCAST;
    private long pointTransactionTag = 0;
    private byte chargeType = 0;
    private long chargeParam = 0;

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag) {
        this.userIdTag = userIdTag;
    }

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId) {
        this.connectorId = connectorId;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getExpectedStoptime() {
        return this.expectedStoptime;
    }

    public void setExpectedStoptime(long expectedStoptime) {
        this.expectedStoptime = expectedStoptime;
    }

    public long getPointTransactionTag() {
        return this.pointTransactionTag;
    }

    public void setPointTransactionTag(long pointTransactionTag) {
        this.pointTransactionTag = pointTransactionTag;
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
        byte[] bytes = new byte[34];
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        int userIdTagLength = userIdTagBytes.length > 16 ? 16 : userIdTagBytes.length;
        System.arraycopy(userIdTagBytes, 0, bytes, 0, userIdTagLength);
        bytes[16] = this.connectorId;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.startTime & XMSZHead.ID_BROADCAST)), 0, bytes, 17, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.expectedStoptime & XMSZHead.ID_BROADCAST)), 0, bytes, 21, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.pointTransactionTag & XMSZHead.ID_BROADCAST)), 0, bytes, 25, 4);
        bytes[29] = this.chargeType;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeParam & XMSZHead.ID_BROADCAST)), 0, bytes, 30, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
