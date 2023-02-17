package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

public class StatusNotificationRequest extends XMSZMessage {
    public static final byte CPS_CONNECTED = 1;
    public static final byte CPS_DISCONNECTED = 0;
    public static final byte PEC_AMMETER_FAULT = 6;
    public static final byte PEC_CARD_READER_FAULT = 8;
    public static final byte PEC_GROUND_ERROR = 2;
    public static final byte PEC_LOCK_ERROR = 1;
    public static final byte PEC_LOWER_VOLTAGE = 16;
    public static final byte PEC_NO_ERROR = 4;
    public static final byte PEC_OVER_CURRENT = 18;
    public static final byte PEC_OVER_CURRENT_PROTECT_FAULT = 5;
    public static final byte PEC_OVER_TEMPERATURE = 3;
    public static final byte PEC_POWER_CONTACTOR_FAULT = 7;
    public static final byte PEC_RESET_ERROR = 9;
    public static final byte PEC_UNKNOWN_ERROR = -1;
    public static final byte PEC_WIRELESS_SIGNAL_ERROR = 17;
    public static final byte PSC_FAULT = 2;
    public static final byte PSC_IDLE = 0;
    public static final byte PSC_IN_RESERVE = 4;
    public static final byte PSC_UNAVAILABLE = 3;
    public static final byte PSC_USING = 1;
    private byte connectorId = 1;
    private byte connectorPlugStatus = 0;
    private byte pointErrorCode = 4;
    private byte pointStatusCode = 0;
    private long time = 0;

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId2) {
        this.connectorId = connectorId2;
    }

    public byte getPointStatusCode() {
        return this.pointStatusCode;
    }

    public void setPointStatusCode(byte pointStatusCode2) {
        this.pointStatusCode = pointStatusCode2;
    }

    public byte getConnectorPlugStatus() {
        return this.connectorPlugStatus;
    }

    public void setConnectorPlugStatus(byte connectorPlugStatus2) {
        this.connectorPlugStatus = connectorPlugStatus2;
    }

    public byte getPointErrorCode() {
        return this.pointErrorCode;
    }

    public void setPointErrorCode(byte pointErrorCode2) {
        this.pointErrorCode = pointErrorCode2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[8];
        bytes[0] = this.connectorId;
        bytes[1] = this.pointStatusCode;
        bytes[2] = this.connectorPlugStatus;
        bytes[3] = this.pointErrorCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.time & XMSZHead.ID_BROADCAST)), 0, bytes, 4, 4);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
