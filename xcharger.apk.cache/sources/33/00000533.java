package com.xcharge.charger.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import java.lang.Thread;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "MultiProtocolCharger.CrashHandler";
    private Application mApplication;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE = new CrashHandler();
    private static String error = "程序错误，额，不对，我应该说，服务器正在维护中，请稍后再试";
    private static final Map<String, String> regexMap = new HashMap();
    private Map<String, String> infos = new HashMap();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        initMap();
        return INSTANCE;
    }

    public void init(Application application) {
        this.mApplication = application;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        Log.d(TAG, "CrashHandler:init");
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "Thread( " + thread.getId() + ", " + thread.toString() + " ): " + Log.getStackTraceString(ex));
        if (!handleException(ex) && this.mDefaultHandler != null) {
            this.mDefaultHandler.uncaughtException(thread, ex);
            Log.d(TAG, "CrashHandler:defalut uncaughtException handle");
            return;
        }
        ((MultiProtocolCharger) this.mApplication).destroy();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Process.killProcess(Process.myPid());
        System.exit(1);
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        saveCrashInfo2File(ex);
        return true;
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 1);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = new StringBuilder(String.valueOf(pi.versionCode)).toString();
                this.infos.put("versionName", versionName);
                this.infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                this.infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, String.valueOf(field.getName()) + " : " + field.get(null));
            } catch (Exception e2) {
                Log.e(TAG, Log.getStackTraceString(e2));
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        LogUtils.syslog("crash exception: " + Log.getStackTraceString(ex));
        try {
            FileUtils.execShell("logcat -b main -b system -v time -d *:d > /data/data/com.xcharge.charger/logcat.log.crash");
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static StringBuffer getTraceInfo(Throwable e) {
        StringBuffer sb = new StringBuffer();
        Throwable ex = e.getCause() == null ? e : e.getCause();
        StackTraceElement[] stacks = ex.getStackTrace();
        for (int i = 0; i < stacks.length; i++) {
            if (i == 0) {
                setError(ex.toString());
            }
            sb.append("class: ").append(stacks[i].getClassName()).append("; method: ").append(stacks[i].getMethodName()).append("; line: ").append(stacks[i].getLineNumber()).append(";  Exception: ").append(String.valueOf(ex.toString()) + StringUtils.LF);
        }
        Log.d(TAG, sb.toString());
        return sb;
    }

    public static void setError(String e) {
        for (Map.Entry<String, String> m : regexMap.entrySet()) {
            Log.d(TAG, String.valueOf(e) + " key:" + m.getKey() + "; value:" + m.getValue());
            Pattern pattern = Pattern.compile(m.getKey());
            Matcher matcher = pattern.matcher(e);
            if (matcher.matches()) {
                error = m.getValue();
                return;
            }
        }
    }

    private static void initMap() {
        regexMap.put(".*NullPointerException.*", "嘿，无中生有~Boom!");
        regexMap.put(".*ClassNotFoundException.*", "你确定你能找得到它？");
        regexMap.put(".*ArithmeticException.*", "我猜你的数学是体育老师教的，对吧？");
        regexMap.put(".*ArrayIndexOutOfBoundsException.*", "恩，无下限=无节操，请不要跟我搭话");
        regexMap.put(".*IllegalArgumentException.*", "你的出生就是一场错误。");
        regexMap.put(".*IllegalAccessException.*", "很遗憾，你的信用卡账号被冻结了，无权支付");
        regexMap.put(".*SecturityException.*", "死神马上降临");
        regexMap.put(".*NumberFormatException.*", "想要改变一下自己形象？去泰国吧，包你满意");
        regexMap.put(".*OutOfMemoryError.*", "或许你该减减肥了");
        regexMap.put(".*StackOverflowError.*", "啊，啊，憋不住了！");
        regexMap.put(".*RuntimeException.*", "你的人生走错了方向，重来吧");
    }
}