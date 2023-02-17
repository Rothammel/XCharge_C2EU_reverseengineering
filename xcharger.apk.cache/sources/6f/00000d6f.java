package org.apache.mina.core.session;

/* loaded from: classes.dex */
public class UnknownMessageTypeException extends RuntimeException {
    private static final long serialVersionUID = 3257290227428047158L;

    public UnknownMessageTypeException() {
    }

    public UnknownMessageTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownMessageTypeException(String message) {
        super(message);
    }

    public UnknownMessageTypeException(Throwable cause) {
        super(cause);
    }
}