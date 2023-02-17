package org.apache.commons.logging.impl;

import com.xcharge.charger.data.bean.UpgradeData;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SimpleLog implements Log {
    public static final int LOG_LEVEL_ALL = 0;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_FATAL = 6;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_OFF = 7;
    public static final int LOG_LEVEL_TRACE = 1;
    public static final int LOG_LEVEL_WARN = 4;
    static Class class$java$lang$Thread = null;
    static Class class$org$apache$commons$logging$impl$SimpleLog = null;
    protected static DateFormat dateFormatter = null;
    protected static boolean showDateTime = false;
    protected static boolean showLogName = false;
    protected static boolean showShortName = false;
    protected static final Properties simpleLogProps = new Properties();
    protected static final String systemPrefix = "org.apache.commons.logging.simplelog.";
    protected int currentLogLevel;
    protected String logName = null;
    private String prefix = null;

    static ClassLoader access$000() {
        return getContextClassLoader();
    }

    static {
        showLogName = false;
        showShortName = true;
        showDateTime = false;
        dateFormatter = null;
        InputStream in = getResourceAsStream("simplelog.properties");
        if (in != null) {
            try {
                simpleLogProps.load(in);
                in.close();
            } catch (IOException e) {
            }
        }
        showLogName = getBooleanProperty("org.apache.commons.logging.simplelog.showlogname", showLogName);
        showShortName = getBooleanProperty("org.apache.commons.logging.simplelog.showShortLogname", showShortName);
        showDateTime = getBooleanProperty("org.apache.commons.logging.simplelog.showdatetime", showDateTime);
        showLogName = getBooleanProperty("org.apache.commons.logging.simplelog.showlogname", showLogName);
        if (showDateTime) {
            dateFormatter = new SimpleDateFormat(getStringProperty("org.apache.commons.logging.simplelog.dateformat", "yyyy/MM/dd HH:mm:ss:SSS zzz"));
        }
    }

    private static String getStringProperty(String name) {
        String prop = System.getProperty(name);
        return prop == null ? simpleLogProps.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : prop;
    }

    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : "true".equalsIgnoreCase(prop);
    }

    public SimpleLog(String name) {
        this.logName = name;
        setLevel(3);
        String lvl = getStringProperty(new StringBuffer().append("org.apache.commons.logging.simplelog.log.").append(this.logName).toString());
        int i = String.valueOf(name).lastIndexOf(".");
        while (lvl == null && i > -1) {
            name = name.substring(0, i);
            lvl = getStringProperty(new StringBuffer().append("org.apache.commons.logging.simplelog.log.").append(name).toString());
            i = String.valueOf(name).lastIndexOf(".");
        }
        lvl = lvl == null ? getStringProperty("org.apache.commons.logging.simplelog.defaultlog") : lvl;
        if (UpgradeData.COM_ALL.equalsIgnoreCase(lvl)) {
            setLevel(0);
        } else if ("trace".equalsIgnoreCase(lvl)) {
            setLevel(1);
        } else if ("debug".equalsIgnoreCase(lvl)) {
            setLevel(2);
        } else if ("info".equalsIgnoreCase(lvl)) {
            setLevel(3);
        } else if ("warn".equalsIgnoreCase(lvl)) {
            setLevel(4);
        } else if ("error".equalsIgnoreCase(lvl)) {
            setLevel(5);
        } else if ("fatal".equalsIgnoreCase(lvl)) {
            setLevel(6);
        } else if ("off".equalsIgnoreCase(lvl)) {
            setLevel(7);
        }
    }

    public void setLevel(int currentLogLevel2) {
        this.currentLogLevel = currentLogLevel2;
    }

    public int getLevel() {
        return this.currentLogLevel;
    }

    /* access modifiers changed from: protected */
    public void log(int type, Object message, Throwable t) {
        StringBuffer buf = new StringBuffer();
        if (showDateTime) {
            buf.append(dateFormatter.format(new Date()));
            buf.append(StringUtils.SPACE);
        }
        switch (type) {
            case 1:
                buf.append("[TRACE] ");
                break;
            case 2:
                buf.append("[DEBUG] ");
                break;
            case 3:
                buf.append("[INFO] ");
                break;
            case 4:
                buf.append("[WARN] ");
                break;
            case 5:
                buf.append("[ERROR] ");
                break;
            case 6:
                buf.append("[FATAL] ");
                break;
        }
        if (showShortName) {
            if (this.prefix == null) {
                this.prefix = new StringBuffer().append(this.logName.substring(this.logName.lastIndexOf(".") + 1)).append(" - ").toString();
                this.prefix = new StringBuffer().append(this.prefix.substring(this.prefix.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR) + 1)).append("-").toString();
            }
            buf.append(this.prefix);
        } else if (showLogName) {
            buf.append(String.valueOf(this.logName)).append(" - ");
        }
        buf.append(String.valueOf(message));
        if (t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");
            StringWriter sw = new StringWriter(1024);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }
        System.err.println(buf.toString());
    }

    /* access modifiers changed from: protected */
    public boolean isLevelEnabled(int logLevel) {
        return logLevel >= this.currentLogLevel;
    }

    public final void debug(Object message) {
        if (isLevelEnabled(2)) {
            log(2, message, (Throwable) null);
        }
    }

    public final void debug(Object message, Throwable t) {
        if (isLevelEnabled(2)) {
            log(2, message, t);
        }
    }

    public final void trace(Object message) {
        if (isLevelEnabled(1)) {
            log(1, message, (Throwable) null);
        }
    }

    public final void trace(Object message, Throwable t) {
        if (isLevelEnabled(1)) {
            log(1, message, t);
        }
    }

    public final void info(Object message) {
        if (isLevelEnabled(3)) {
            log(3, message, (Throwable) null);
        }
    }

    public final void info(Object message, Throwable t) {
        if (isLevelEnabled(3)) {
            log(3, message, t);
        }
    }

    public final void warn(Object message) {
        if (isLevelEnabled(4)) {
            log(4, message, (Throwable) null);
        }
    }

    public final void warn(Object message, Throwable t) {
        if (isLevelEnabled(4)) {
            log(4, message, t);
        }
    }

    public final void error(Object message) {
        if (isLevelEnabled(5)) {
            log(5, message, (Throwable) null);
        }
    }

    public final void error(Object message, Throwable t) {
        if (isLevelEnabled(5)) {
            log(5, message, t);
        }
    }

    public final void fatal(Object message) {
        if (isLevelEnabled(6)) {
            log(6, message, (Throwable) null);
        }
    }

    public final void fatal(Object message, Throwable t) {
        if (isLevelEnabled(6)) {
            log(6, message, t);
        }
    }

    public final boolean isDebugEnabled() {
        return isLevelEnabled(2);
    }

    public final boolean isErrorEnabled() {
        return isLevelEnabled(5);
    }

    public final boolean isFatalEnabled() {
        return isLevelEnabled(6);
    }

    public final boolean isInfoEnabled() {
        return isLevelEnabled(3);
    }

    public final boolean isTraceEnabled() {
        return isLevelEnabled(1);
    }

    public final boolean isWarnEnabled() {
        return isLevelEnabled(4);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.ClassLoader} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.ClassLoader getContextClassLoader() {
        /*
            r1 = 0
            if (r1 != 0) goto L_0x0023
            java.lang.Class r4 = class$java$lang$Thread     // Catch:{ NoSuchMethodException -> 0x0050 }
            if (r4 != 0) goto L_0x0036
            java.lang.String r4 = "java.lang.Thread"
            java.lang.Class r4 = class$(r4)     // Catch:{ NoSuchMethodException -> 0x0050 }
            class$java$lang$Thread = r4     // Catch:{ NoSuchMethodException -> 0x0050 }
        L_0x000f:
            java.lang.String r5 = "getContextClassLoader"
            r6 = 0
            java.lang.reflect.Method r3 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x0050 }
            java.lang.Thread r4 = java.lang.Thread.currentThread()     // Catch:{ IllegalAccessException -> 0x0039, InvocationTargetException -> 0x003b }
            r5 = 0
            java.lang.Object r4 = r3.invoke(r4, r5)     // Catch:{ IllegalAccessException -> 0x0039, InvocationTargetException -> 0x003b }
            r0 = r4
            java.lang.ClassLoader r0 = (java.lang.ClassLoader) r0     // Catch:{ IllegalAccessException -> 0x0039, InvocationTargetException -> 0x003b }
            r1 = r0
        L_0x0023:
            if (r1 != 0) goto L_0x0035
            java.lang.Class r4 = class$org$apache$commons$logging$impl$SimpleLog
            if (r4 != 0) goto L_0x0052
            java.lang.String r4 = "org.apache.commons.logging.impl.SimpleLog"
            java.lang.Class r4 = class$(r4)
            class$org$apache$commons$logging$impl$SimpleLog = r4
        L_0x0031:
            java.lang.ClassLoader r1 = r4.getClassLoader()
        L_0x0035:
            return r1
        L_0x0036:
            java.lang.Class r4 = class$java$lang$Thread     // Catch:{ NoSuchMethodException -> 0x0050 }
            goto L_0x000f
        L_0x0039:
            r2 = move-exception
            goto L_0x0023
        L_0x003b:
            r2 = move-exception
            java.lang.Throwable r4 = r2.getTargetException()     // Catch:{ NoSuchMethodException -> 0x0050 }
            boolean r4 = r4 instanceof java.lang.SecurityException     // Catch:{ NoSuchMethodException -> 0x0050 }
            if (r4 != 0) goto L_0x0023
            org.apache.commons.logging.LogConfigurationException r4 = new org.apache.commons.logging.LogConfigurationException     // Catch:{ NoSuchMethodException -> 0x0050 }
            java.lang.String r5 = "Unexpected InvocationTargetException"
            java.lang.Throwable r6 = r2.getTargetException()     // Catch:{ NoSuchMethodException -> 0x0050 }
            r4.<init>(r5, r6)     // Catch:{ NoSuchMethodException -> 0x0050 }
            throw r4     // Catch:{ NoSuchMethodException -> 0x0050 }
        L_0x0050:
            r2 = move-exception
            goto L_0x0023
        L_0x0052:
            java.lang.Class r4 = class$org$apache$commons$logging$impl$SimpleLog
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.SimpleLog.getContextClassLoader():java.lang.ClassLoader");
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static InputStream getResourceAsStream(String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction(name) {
            private final String val$name;

            {
                this.val$name = val$name;
            }

            public Object run() {
                ClassLoader threadCL = SimpleLog.access$000();
                if (threadCL != null) {
                    return threadCL.getResourceAsStream(this.val$name);
                }
                return ClassLoader.getSystemResourceAsStream(this.val$name);
            }
        });
    }
}
