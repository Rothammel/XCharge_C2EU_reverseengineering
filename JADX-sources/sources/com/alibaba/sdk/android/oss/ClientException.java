package com.alibaba.sdk.android.oss;

import com.alibaba.sdk.android.oss.common.OSSLog;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class ClientException extends Exception {
    private Boolean canceled;

    public ClientException() {
        this.canceled = false;
    }

    public ClientException(String message) {
        super("[ErrorMessage]: " + message);
        this.canceled = false;
    }

    public ClientException(Throwable cause) {
        super(cause);
        this.canceled = false;
    }

    public ClientException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public ClientException(String message, Throwable cause, Boolean isCancelled) {
        super("[ErrorMessage]: " + message, cause);
        this.canceled = false;
        this.canceled = isCancelled;
        OSSLog.logThrowable2Local(this);
    }

    public Boolean isCanceledException() {
        return this.canceled;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        String base = super.getMessage();
        return getCause() == null ? base : getCause().getMessage() + StringUtils.LF + base;
    }
}
