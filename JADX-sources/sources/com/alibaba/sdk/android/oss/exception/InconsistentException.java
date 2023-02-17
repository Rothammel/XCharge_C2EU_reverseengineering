package com.alibaba.sdk.android.oss.exception;

import java.io.IOException;

/* loaded from: classes.dex */
public class InconsistentException extends IOException {
    private Long clientChecksum;
    private String requestId;
    private Long serverChecksum;

    public InconsistentException(Long clientChecksum, Long serverChecksum, String requestId) {
        this.clientChecksum = clientChecksum;
        this.serverChecksum = serverChecksum;
        this.requestId = requestId;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return "InconsistentException: inconsistent object\n[RequestId]: " + this.requestId + "\n[ClientChecksum]: " + this.clientChecksum + "\n[ServerChecksum]: " + this.serverChecksum;
    }
}
