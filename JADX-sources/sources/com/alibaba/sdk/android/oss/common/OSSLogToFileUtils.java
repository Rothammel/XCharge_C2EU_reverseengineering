package com.alibaba.sdk.android.oss.common;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class OSSLogToFileUtils {
    private static final String LOG_DIR_NAME = "OSSLog";
    private static OSSLogToFileUtils instance;
    private static Context sContext;
    private static File sLogFile;
    private boolean useSdCard = true;
    private static LogThreadPoolManager logService = LogThreadPoolManager.newInstance();
    private static SimpleDateFormat sLogSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static long LOG_MAX_SIZE = 5242880;

    private OSSLogToFileUtils() {
    }

    public static void init(Context context, ClientConfiguration cfg) {
        OSSLog.logDebug("init ...", false);
        if (cfg != null) {
            LOG_MAX_SIZE = cfg.getMaxLogSize();
        }
        if (sContext == null || instance == null || sLogFile == null || !sLogFile.exists()) {
            sContext = context.getApplicationContext();
            instance = getInstance();
            logService.addExecuteTask(new Runnable() { // from class: com.alibaba.sdk.android.oss.common.OSSLogToFileUtils.1
                @Override // java.lang.Runnable
                public void run() {
                    File unused = OSSLogToFileUtils.sLogFile = OSSLogToFileUtils.instance.getLogFile();
                    if (OSSLogToFileUtils.sLogFile != null) {
                        OSSLog.logInfo("LogFilePath is: " + OSSLogToFileUtils.sLogFile.getPath(), false);
                        long logFileSize = OSSLogToFileUtils.getLogFileSize(OSSLogToFileUtils.sLogFile);
                        if (OSSLogToFileUtils.LOG_MAX_SIZE < logFileSize) {
                            OSSLog.logInfo("init reset log file", false);
                            OSSLogToFileUtils.instance.resetLogFile();
                        }
                    }
                }
            });
            return;
        }
        OSSLog.logDebug("LogToFileUtils has been init ...", false);
    }

    public static OSSLogToFileUtils getInstance() {
        if (instance == null) {
            synchronized (OSSLogToFileUtils.class) {
                if (instance == null) {
                    instance = new OSSLogToFileUtils();
                }
            }
        }
        return instance;
    }

    private long readSDCardSpace() {
        long sdCardSize = 0;
        String state = Environment.getExternalStorageState();
        if ("mounted".equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            sdCardSize = availCount * blockSize;
        }
        OSSLog.logDebug("sd卡存储空间:" + String.valueOf(sdCardSize) + "kb", false);
        return sdCardSize;
    }

    private long readSystemSpace() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        long systemSpaceSize = (availCount * blockSize) / 1024;
        OSSLog.logDebug("内部存储空间:" + String.valueOf(systemSpaceSize) + "kb", false);
        return systemSpaceSize;
    }

    public void setUseSdCard(boolean useSdCard) {
        this.useSdCard = useSdCard;
    }

    public void resetLogFile() {
        OSSLog.logDebug("Reset Log File ... ", false);
        if (!sLogFile.getParentFile().exists()) {
            OSSLog.logDebug("Reset Log make File dir ... ", false);
            sLogFile.getParentFile().mkdir();
        }
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            logFile.delete();
        }
        createNewFile(logFile);
    }

    public void deleteLogFile() {
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            OSSLog.logDebug("delete Log File ... ", false);
            logFile.delete();
        }
    }

    public void deleteLogFileDir() {
        deleteLogFile();
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_DIR_NAME);
        if (dir.exists()) {
            OSSLog.logDebug("delete Log FileDir ... ", false);
            dir.delete();
        }
    }

    public static void reset() {
        sContext = null;
        instance = null;
        sLogFile = null;
    }

    public static long getLogFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0L;
        }
        long size = file.length();
        return size;
    }

    public static long getLocalLogFileSize() {
        return getLogFileSize(sLogFile);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File getLogFile() {
        boolean canStorage;
        File file;
        if (this.useSdCard && Environment.getExternalStorageState().equals("mounted")) {
            canStorage = readSDCardSpace() > LOG_MAX_SIZE / 1024;
            file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_DIR_NAME);
        } else {
            canStorage = readSystemSpace() > LOG_MAX_SIZE / 1024;
            file = new File(sContext.getFilesDir().getPath() + File.separator + LOG_DIR_NAME);
        }
        File logFile = null;
        if (canStorage) {
            if (!file.exists()) {
                file.mkdirs();
            }
            logFile = new File(file.getPath() + "/logs.csv");
            if (!logFile.exists()) {
                createNewFile(logFile);
            }
        }
        return logFile;
    }

    public void createNewFile(File logFile) {
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            OSSLog.logError("Create log file failure !!! " + e.toString(), false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getFunctionInfo(StackTraceElement[] ste) {
        if (ste != null) {
            return null;
        }
        String msg = "[" + sLogSDF.format(new Date()) + "]";
        return msg;
    }

    public synchronized void write(Object str) {
        if (OSSLog.isEnableLog() && sContext != null && instance != null && sLogFile != null) {
            if (!sLogFile.exists()) {
                resetLogFile();
            }
            WriteCall writeCall = new WriteCall(str);
            logService.addExecuteTask(writeCall);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class WriteCall implements Runnable {
        private Object mStr;

        public WriteCall(Object mStr) {
            this.mStr = mStr;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (OSSLogToFileUtils.sLogFile != null) {
                OSSLogToFileUtils.getInstance();
                long logFileSize = OSSLogToFileUtils.getLogFileSize(OSSLogToFileUtils.sLogFile);
                if (logFileSize > OSSLogToFileUtils.LOG_MAX_SIZE) {
                    OSSLogToFileUtils.getInstance().resetLogFile();
                }
                try {
                    PrintWriter pw = new PrintWriter((Writer) new FileWriter(OSSLogToFileUtils.sLogFile, true), true);
                    if (pw != null) {
                        if (!(this.mStr instanceof Throwable)) {
                            pw.println(OSSLogToFileUtils.getInstance().getFunctionInfo(null) + " - " + this.mStr.toString());
                        } else {
                            printEx(pw);
                        }
                        pw.println("------>end of log");
                        pw.println();
                        pw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private PrintWriter printEx(PrintWriter pw) {
            pw.println("crash_time：" + OSSLogToFileUtils.sLogSDF.format(new Date()));
            ((Throwable) this.mStr).printStackTrace(pw);
            return pw;
        }
    }
}
