package org.java_websocket.exceptions;

import org.java_websocket.framing.CloseFrame;

public class InvalidFrameException extends InvalidDataException {
    private static final long serialVersionUID = -9016496369828887591L;

    public InvalidFrameException() {
        super(CloseFrame.PROTOCOL_ERROR);
    }

    public InvalidFrameException(String s) {
        super((int) CloseFrame.PROTOCOL_ERROR, s);
    }

    public InvalidFrameException(Throwable t) {
        super((int) CloseFrame.PROTOCOL_ERROR, t);
    }

    public InvalidFrameException(String s, Throwable t) {
        super(CloseFrame.PROTOCOL_ERROR, s, t);
    }
}
