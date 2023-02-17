package com.xcharge.charger.protocol.anyo.bean.request;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;

public class ReportEventRequest extends AnyoMessage {
    public static final byte EVENT_PILE_LOCKED = 16;
    public static final byte EVENT_PILE_UNLOCKED = 1;
    public static final byte EVENT_PORT_CONTACTOR_CLOSED = 6;
    public static final byte EVENT_PORT_CONTACTOR_OPENED = 5;
    public static final byte EVENT_PORT_LOCKED = 7;
    public static final byte EVENT_PORT_PLUGIN = 3;
    public static final byte EVENT_PORT_PLUGOUT_IDLE = 9;
    public static final byte EVENT_PORT_PLUGOUT_OCCUPIED = 19;
    public static final byte EVENT_PORT_PULLED = 2;
    public static final byte EVENT_PORT_UNLOCKED = 8;
    private byte event = 0;
    private byte portNo = 0;

    public byte getEvent() {
        return this.event;
    }

    public void setEvent(byte event2) {
        this.event = event2;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.event, this.portNo};
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
