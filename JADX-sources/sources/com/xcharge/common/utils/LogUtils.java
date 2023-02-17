package com.xcharge.common.utils;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
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
                Calendar calendar = new GregorianCalendar();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String formatLog = dateFormat.format(calendar.getTime());
                writeLog(String.valueOf(String.valueOf(String.valueOf(formatLog) + "\t" + tag) + "\t" + text) + StringUtils.LF);
            } catch (Exception e) {
                Log.e("LogUtils.log", Log.getStackTraceString(e));
            }
        }
    }

    private static synchronized void writeLog(String LogLine) {
        synchronized (LogUtils.class) {
            FileOutputStream outputStream = null;
            try {
                try {
                    try {
                        File logFile = new File(LOG_FILE_FULL_NAME);
                        File logFilePath = logFile.getParentFile();
                        if (logFilePath.exists() || logFilePath.mkdirs()) {
                            if (!logFile.exists()) {
                                logFile.createNewFile();
                            }
                            FileOutputStream outputStream2 = new FileOutputStream(logFile, true);
                            try {
                                outputStream2.write(LogLine.getBytes(Charset.forName(CharEncoding.UTF_8)));
                                outputStream2.flush();
                                rotateFile();
                                try {
                                    if (outputStream2 != null) {
                                        try {
                                            outputStream2.close();
                                        } catch (Exception e) {
                                            Log.e("LogUtils.writeLog", Log.getStackTraceString(e));
                                        }
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            } catch (Exception e2) {
                                e = e2;
                                outputStream = outputStream2;
                                Log.e("LogUtils.writeLog", Log.getStackTraceString(e));
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (Exception e3) {
                                        Log.e("LogUtils.writeLog", Log.getStackTraceString(e3));
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                outputStream = outputStream2;
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (Exception e4) {
                                        Log.e("LogUtils.writeLog", Log.getStackTraceString(e4));
                                    }
                                }
                                throw th;
                            }
                        } else {
                            Log.e("LogUtils.writeLog", "failed to create log path: " + logFilePath.getAbsolutePath());
                            if (0 != 0) {
                                try {
                                    outputStream.close();
                                } catch (Exception e5) {
                                    Log.e("LogUtils.writeLog", Log.getStackTraceString(e5));
                                }
                            }
                        }
                    } catch (Exception e6) {
                        e = e6;
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        }
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
