package org.apache.mina.core.filterchain;

/* loaded from: classes.dex */
public class IoFilterLifeCycleException extends RuntimeException {
    private static final long serialVersionUID = -5542098881633506449L;

    public IoFilterLifeCycleException() {
    }

    public IoFilterLifeCycleException(String message) {
        super(message);
    }

    public IoFilterLifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoFilterLifeCycleException(Throwable cause) {
        super(cause);
    }
}