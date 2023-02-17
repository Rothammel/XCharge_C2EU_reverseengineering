package com.xcharge.charger.protocol.anyo.bean.request;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

/* loaded from: classes.dex */
public class HeartBeatRequest extends AnyoMessage {
    public static final byte PORT_STATUS_CHARGE_STOP = 8;
    public static final byte PORT_STATUS_CHARGING = 6;
    public static final byte PORT_STATUS_FAULT = 15;
    public static final byte PORT_STATUS_IDLE = 0;
    public static final byte PORT_STATUS_OCCUPIED = 17;
    public static final byte PORT_STATUS_RESERVED = 2;
    public static final byte PORT_STATUS_TIMING = 1;
    private byte portStatus = 0;
    private byte portNo = 0;

    public byte getPortStatus() {
        return this.portStatus;
    }

    public void setPortStatus(byte portStatus) {
        this.portStatus = portStatus;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo) {
        this.portNo = portNo;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = {this.portStatus, this.portNo};
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
