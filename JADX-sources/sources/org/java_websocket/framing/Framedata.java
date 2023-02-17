package org.java_websocket.framing;

import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public interface Framedata {

    /* loaded from: classes.dex */
    public enum Opcode {
        CONTINUOUS,
        TEXT,
        BINARY,
        PING,
        PONG,
        CLOSING
    }

    void append(Framedata framedata);

    Opcode getOpcode();

    ByteBuffer getPayloadData();

    boolean getTransfereMasked();

    boolean isFin();

    boolean isRSV1();

    boolean isRSV2();

    boolean isRSV3();
}
