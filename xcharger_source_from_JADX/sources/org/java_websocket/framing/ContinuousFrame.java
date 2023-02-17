package org.java_websocket.framing;

import org.java_websocket.framing.Framedata;

public class ContinuousFrame extends DataFrame {
    public ContinuousFrame() {
        super(Framedata.Opcode.CONTINUOUS);
    }
}
