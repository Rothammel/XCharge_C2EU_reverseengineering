package com.nostra13.universalimageloader.utils;

import android.util.Log;
import com.nostra13.universalimageloader.core.ImageLoader;

/* renamed from: com.nostra13.universalimageloader.utils.L */
public final class C0219L {
    private static final String LOG_FORMAT = "%1$s\n%2$s";
    private static volatile boolean writeDebugLogs = false;
    private static volatile boolean writeLogs = true;

    private C0219L() {
    }

    @Deprecated
    public static void enableLogging() {
        writeLogs(true);
    }

    @Deprecated
    public static void disableLogging() {
        writeLogs(false);
    }

    public static void writeDebugLogs(boolean writeDebugLogs2) {
        writeDebugLogs = writeDebugLogs2;
    }

    public static void writeLogs(boolean writeLogs2) {
        writeLogs = writeLogs2;
    }

    /* renamed from: d */
    public static void m12d(String message, Object... args) {
        if (writeDebugLogs) {
            log(3, (Throwable) null, message, args);
        }
    }

    /* renamed from: i */
    public static void m16i(String message, Object... args) {
        log(4, (Throwable) null, message, args);
    }

    /* renamed from: w */
    public static void m17w(String message, Object... args) {
        log(5, (Throwable) null, message, args);
    }

    /* renamed from: e */
    public static void m14e(Throwable ex) {
        log(6, ex, (String) null, new Object[0]);
    }

    /* renamed from: e */
    public static void m13e(String message, Object... args) {
        log(6, (Throwable) null, message, args);
    }

    /* renamed from: e */
    public static void m15e(Throwable ex, String message, Object... args) {
        log(6, ex, message, args);
    }

    private static void log(int priority, Throwable ex, String message, Object... args) {
        String logMessage;
        String log;
        if (writeLogs) {
            if (args.length > 0) {
                message = String.format(message, args);
            }
            if (ex == null) {
                log = message;
            } else {
                if (message == null) {
                    logMessage = ex.getMessage();
                } else {
                    logMessage = message;
                }
                log = String.format(LOG_FORMAT, new Object[]{logMessage, Log.getStackTraceString(ex)});
            }
            Log.println(priority, ImageLoader.TAG, log);
        }
    }
}
