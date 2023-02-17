package org.java_websocket.framing;

import org.java_websocket.framing.Framedata;

public class PingFrame extends ControlFrame {
    public PingFrame() {
        super(Framedata.Opcode.PING);
    }
}
