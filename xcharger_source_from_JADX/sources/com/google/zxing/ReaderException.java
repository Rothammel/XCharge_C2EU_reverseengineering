package com.google.zxing;

public abstract class ReaderException extends Exception {
    protected static final boolean isStackTrace = (System.getProperty("surefire.test.class.path") != null);

    ReaderException() {
    }

    ReaderException(Throwable cause) {
        super(cause);
    }

    public final Throwable fillInStackTrace() {
        return null;
    }
}
