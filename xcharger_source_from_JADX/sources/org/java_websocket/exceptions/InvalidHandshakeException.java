package org.java_websocket.exceptions;

import org.java_websocket.framing.CloseFrame;

public class InvalidHandshakeException extends InvalidDataException {
    private static final long serialVersionUID = -1426533877490484964L;

    public InvalidHandshakeException() {
        super(CloseFrame.PROTOCOL_ERROR);
    }

    public InvalidHandshakeException(String s, Throwable t) {
        super(CloseFrame.PROTOCOL_ERROR, s, t);
    }

    public InvalidHandshakeException(String s) {
        super((int) CloseFrame.PROTOCOL_ERROR, s);
    }

    public InvalidHandshakeException(Throwable t) {
        super((int) CloseFrame.PROTOCOL_ERROR, t);
    }
}
