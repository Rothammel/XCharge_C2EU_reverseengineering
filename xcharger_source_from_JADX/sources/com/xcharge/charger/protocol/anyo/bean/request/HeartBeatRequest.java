package com.xcharge.charger.protocol.anyo.bean.request;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

public class HeartBeatRequest extends AnyoMessage {
    public static final byte PORT_STATUS_CHARGE_STOP = 8;
    public static final byte PORT_STATUS_CHARGING = 6;
    public static final byte PORT_STATUS_FAULT = 15;
    public static final byte PORT_STATUS_IDLE = 0;
    public static final byte PORT_STATUS_OCCUPIED = 17;
    public static final byte PORT_STATUS_RESERVED = 2;
    public static final byte PORT_STATUS_TIMING = 1;
    private byte portNo = 0;
    private byte portStatus = 0;

    public byte getPortStatus() {
        return this.portStatus;
    }

    public void setPortStatus(byte portStatus2) {
        this.portStatus = portStatus2;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.portStatus, this.portNo};
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
