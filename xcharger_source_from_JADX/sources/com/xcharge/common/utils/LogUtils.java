package com.xcharge.common.utils;

import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import org.apache.commons.lang3.StringUtils;

public class LogUtils {
    private static final String LOG_FILE_FULL_NAME = "/data/data/com.xcharge.charger/logs/charger_app.log";
    public static final String LOG_TYPE_APP = "APP";
    public static final String LOG_TYPE_CLOUD = "CLOUD";
    public static final String LOG_TYPE_SYS = "SYS";
    private static final int MAX_ROTATE_FILE_NUM = 10;
    private static final long SINGLE_FILE_MAX_SIZE = 524288;

    public static synchronized void syslog(String text) {
        synchronized (LogUtils.class) {
            log(LOG_TYPE_SYS, text);
        }
    }

    public static synchronized void applog(String text) {
        synchronized (LogUtils.class) {
            log(LOG_TYPE_APP, text);
        }
    }

    public static synchronized void cloudlog(String text) {
        synchronized (LogUtils.class) {
            log(LOG_TYPE_CLOUD, text);
        }
    }

    public static synchronized void log(String tag, String text) {
        synchronized (LogUtils.class) {
            try {
                writeLog(String.valueOf(String.valueOf(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new GregorianCalendar().getTime())) + "\t" + tag) + "\t" + text) + StringUtils.f146LF);
            } catch (Exception e) {
                Log.e("LogUtils.log", Log.getStackTraceString(e));
            }
        }
        return;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x007d A[SYNTHETIC, Splitter:B:38:0x007d] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008f A[SYNTHETIC, Splitter:B:46:0x008f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized void writeLog(java.lang.String r9) {
        /*
            java.lang.Class<com.xcharge.common.utils.LogUtils> r6 = com.xcharge.common.utils.LogUtils.class
            monitor-enter(r6)
            r3 = 0
            java.io.File r1 = new java.io.File     // Catch:{ Exception -> 0x0071 }
            java.lang.String r5 = "/data/data/com.xcharge.charger/logs/charger_app.log"
            r1.<init>(r5)     // Catch:{ Exception -> 0x0071 }
            java.io.File r2 = r1.getParentFile()     // Catch:{ Exception -> 0x0071 }
            boolean r5 = r2.exists()     // Catch:{ Exception -> 0x0071 }
            if (r5 != 0) goto L_0x0048
            boolean r5 = r2.mkdirs()     // Catch:{ Exception -> 0x0071 }
            if (r5 != 0) goto L_0x0048
            java.lang.String r5 = "LogUtils.writeLog"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0071 }
            java.lang.String r8 = "failed to create log path: "
            r7.<init>(r8)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r8 = r2.getAbsolutePath()     // Catch:{ Exception -> 0x0071 }
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0071 }
            android.util.Log.e(r5, r7)     // Catch:{ Exception -> 0x0071 }
            if (r3 == 0) goto L_0x0038
            r3.close()     // Catch:{ Exception -> 0x003a }
        L_0x0038:
            monitor-exit(r6)
            return
        L_0x003a:
            r0 = move-exception
            java.lang.String r5 = "LogUtils.writeLog"
            java.lang.String r7 = android.util.Log.getStackTraceString(r0)     // Catch:{ all -> 0x0045 }
            android.util.Log.e(r5, r7)     // Catch:{ all -> 0x0045 }
            goto L_0x0038
        L_0x0045:
            r5 = move-exception
        L_0x0046:
            monitor-exit(r6)
            throw r5
        L_0x0048:
            boolean r5 = r1.exists()     // Catch:{ Exception -> 0x0071 }
            if (r5 != 0) goto L_0x0051
            r1.createNewFile()     // Catch:{ Exception -> 0x0071 }
        L_0x0051:
            java.io.FileOutputStream r4 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x0071 }
            r5 = 1
            r4.<init>(r1, r5)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r5 = "UTF-8"
            java.nio.charset.Charset r5 = java.nio.charset.Charset.forName(r5)     // Catch:{ Exception -> 0x00ad, all -> 0x00aa }
            byte[] r5 = r9.getBytes(r5)     // Catch:{ Exception -> 0x00ad, all -> 0x00aa }
            r4.write(r5)     // Catch:{ Exception -> 0x00ad, all -> 0x00aa }
            r4.flush()     // Catch:{ Exception -> 0x00ad, all -> 0x00aa }
            rotateFile()     // Catch:{ Exception -> 0x00ad, all -> 0x00aa }
            if (r4 == 0) goto L_0x00a8
            r4.close()     // Catch:{ Exception -> 0x009e }
            r3 = r4
            goto L_0x0038
        L_0x0071:
            r0 = move-exception
        L_0x0072:
            java.lang.String r5 = "LogUtils.writeLog"
            java.lang.String r7 = android.util.Log.getStackTraceString(r0)     // Catch:{ all -> 0x008c }
            android.util.Log.e(r5, r7)     // Catch:{ all -> 0x008c }
            if (r3 == 0) goto L_0x0038
            r3.close()     // Catch:{ Exception -> 0x0081 }
            goto L_0x0038
        L_0x0081:
            r0 = move-exception
            java.lang.String r5 = "LogUtils.writeLog"
            java.lang.String r7 = android.util.Log.getStackTraceString(r0)     // Catch:{ all -> 0x0045 }
            android.util.Log.e(r5, r7)     // Catch:{ all -> 0x0045 }
            goto L_0x0038
        L_0x008c:
            r5 = move-exception
        L_0x008d:
            if (r3 == 0) goto L_0x0092
            r3.close()     // Catch:{ Exception -> 0x0093 }
        L_0x0092:
            throw r5     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            java.lang.String r7 = "LogUtils.writeLog"
            java.lang.String r8 = android.util.Log.getStackTraceString(r0)     // Catch:{ all -> 0x0045 }
            android.util.Log.e(r7, r8)     // Catch:{ all -> 0x0045 }
            goto L_0x0092
        L_0x009e:
            r0 = move-exception
            java.lang.String r5 = "LogUtils.writeLog"
            java.lang.String r7 = android.util.Log.getStackTraceString(r0)     // Catch:{ all -> 0x00b0 }
            android.util.Log.e(r5, r7)     // Catch:{ all -> 0x00b0 }
        L_0x00a8:
            r3 = r4
            goto L_0x0038
        L_0x00aa:
            r5 = move-exception
            r3 = r4
            goto L_0x008d
        L_0x00ad:
            r0 = move-exception
            r3 = r4
            goto L_0x0072
        L_0x00b0:
            r5 = move-exception
            r3 = r4
            goto L_0x0046
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.LogUtils.writeLog(java.lang.String):void");
    }

    private static synchronized void rotateFile() {
        synchronized (LogUtils.class) {
            File logFile = new File(LOG_FILE_FULL_NAME);
            try {
                if (logFile.length() >= SINGLE_FILE_MAX_SIZE) {
                    rotateFile(10);
                    logFile.renameTo(new File("/data/data/com.xcharge.charger/logs/charger_app.log.1"));
                }
            } catch (Exception e) {
                Log.e("LogUtils.writeLog", Log.getStackTraceString(e));
            }
        }
        return;
    }

    private static synchronized void rotateFile(int num) throws Exception {
        synchronized (LogUtils.class) {
            int prev = num - 1;
            if (prev > 0) {
                File prevFile = new File("/data/data/com.xcharge.charger/logs/charger_app.log." + prev);
                if (prevFile.exists()) {
                    prevFile.renameTo(new File("/data/data/com.xcharge.charger/logs/charger_app.log." + num));
                }
                rotateFile(prev);
            }
        }
    }
}
