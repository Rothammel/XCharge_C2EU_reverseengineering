package org.apache.mina.filter.keepalive;

/* loaded from: classes.dex */
public class KeepAliveRequestTimeoutException extends RuntimeException {
    private static final long serialVersionUID = -1985092764656546558L;

    public KeepAliveRequestTimeoutException() {
    }

    public KeepAliveRequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeepAliveRequestTimeoutException(String message) {
        super(message);
    }

    public KeepAliveRequestTimeoutException(Throwable cause) {
        super(cause);
    }
}
