package com.google.zxing;

public final class FormatException extends ReaderException {
    private static final FormatException instance = new FormatException();

    private FormatException() {
    }

    private FormatException(Throwable cause) {
        super(cause);
    }

    public static FormatException getFormatInstance() {
        if (isStackTrace) {
            return new FormatException();
        }
        return instance;
    }

    public static FormatException getFormatInstance(Throwable cause) {
        if (isStackTrace) {
            return new FormatException(cause);
        }
        return instance;
    }
}
