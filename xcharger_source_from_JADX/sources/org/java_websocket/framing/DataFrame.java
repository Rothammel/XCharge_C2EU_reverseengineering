package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

public abstract class DataFrame extends FramedataImpl1 {
    public DataFrame(Framedata.Opcode opcode) {
        super(opcode);
    }

    public void isValid() throws InvalidDataException {
    }
}
