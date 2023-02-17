package com.nostra13.universalimageloader.core.assist;

/* loaded from: classes.dex */
public class FailReason {
    private final Throwable cause;
    private final FailType type;

    /* loaded from: classes.dex */
    public enum FailType {
        IO_ERROR,
        DECODING_ERROR,
        NETWORK_DENIED,
        OUT_OF_MEMORY,
        UNKNOWN
    }

    public FailReason(FailType type, Throwable cause) {
        this.type = type;
        this.cause = cause;
    }

    public FailType getType() {
        return this.type;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
