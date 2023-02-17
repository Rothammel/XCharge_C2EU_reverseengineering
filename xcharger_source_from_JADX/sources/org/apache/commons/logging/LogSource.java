package org.apache.commons.logging;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import org.apache.commons.logging.impl.NoOpLog;

public class LogSource {
    protected static boolean jdk14IsAvailable;
    protected static boolean log4jIsAvailable;
    protected static Constructor logImplctor = null;
    protected static Hashtable logs = new Hashtable();

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0036 A[Catch:{ Throwable -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003e A[SYNTHETIC, Splitter:B:18:0x003e] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x005d A[SYNTHETIC, Splitter:B:36:0x005d] */
    static {
        /*
            r4 = 0
            java.util.Hashtable r3 = new java.util.Hashtable
            r3.<init>()
            logs = r3
            log4jIsAvailable = r4
            jdk14IsAvailable = r4
            r3 = 0
            logImplctor = r3
            java.lang.String r3 = "org.apache.log4j.Logger"
            java.lang.Class r3 = java.lang.Class.forName(r3)     // Catch:{ Throwable -> 0x0046 }
            if (r3 == 0) goto L_0x0042
            r3 = 1
            log4jIsAvailable = r3     // Catch:{ Throwable -> 0x0046 }
        L_0x001a:
            java.lang.String r3 = "java.util.logging.Logger"
            java.lang.Class r3 = java.lang.Class.forName(r3)     // Catch:{ Throwable -> 0x004e }
            if (r3 == 0) goto L_0x004a
            java.lang.String r3 = "org.apache.commons.logging.impl.Jdk14Logger"
            java.lang.Class r3 = java.lang.Class.forName(r3)     // Catch:{ Throwable -> 0x004e }
            if (r3 == 0) goto L_0x004a
            r3 = 1
            jdk14IsAvailable = r3     // Catch:{ Throwable -> 0x004e }
        L_0x002d:
            r0 = 0
            java.lang.String r3 = "org.apache.commons.logging.log"
            java.lang.String r0 = java.lang.System.getProperty(r3)     // Catch:{ Throwable -> 0x0052 }
            if (r0 != 0) goto L_0x003c
            java.lang.String r3 = "org.apache.commons.logging.Log"
            java.lang.String r0 = java.lang.System.getProperty(r3)     // Catch:{ Throwable -> 0x0052 }
        L_0x003c:
            if (r0 == 0) goto L_0x005d
            setLogImplementation((java.lang.String) r0)     // Catch:{ Throwable -> 0x0054 }
        L_0x0041:
            return
        L_0x0042:
            r3 = 0
            log4jIsAvailable = r3     // Catch:{ Throwable -> 0x0046 }
            goto L_0x001a
        L_0x0046:
            r1 = move-exception
            log4jIsAvailable = r4
            goto L_0x001a
        L_0x004a:
            r3 = 0
            jdk14IsAvailable = r3     // Catch:{ Throwable -> 0x004e }
            goto L_0x002d
        L_0x004e:
            r1 = move-exception
            jdk14IsAvailable = r4
            goto L_0x002d
        L_0x0052:
            r1 = move-exception
            goto L_0x003c
        L_0x0054:
            r1 = move-exception
            java.lang.String r3 = "org.apache.commons.logging.impl.NoOpLog"
            setLogImplementation((java.lang.String) r3)     // Catch:{ Throwable -> 0x005b }
            goto L_0x0041
        L_0x005b:
            r2 = move-exception
            goto L_0x0041
        L_0x005d:
            boolean r3 = log4jIsAvailable     // Catch:{ Throwable -> 0x0067 }
            if (r3 == 0) goto L_0x0070
            java.lang.String r3 = "org.apache.commons.logging.impl.Log4JLogger"
            setLogImplementation((java.lang.String) r3)     // Catch:{ Throwable -> 0x0067 }
            goto L_0x0041
        L_0x0067:
            r1 = move-exception
            java.lang.String r3 = "org.apache.commons.logging.impl.NoOpLog"
            setLogImplementation((java.lang.String) r3)     // Catch:{ Throwable -> 0x006e }
            goto L_0x0041
        L_0x006e:
            r2 = move-exception
            goto L_0x0041
        L_0x0070:
            boolean r3 = jdk14IsAvailable     // Catch:{ Throwable -> 0x0067 }
            if (r3 == 0) goto L_0x007a
            java.lang.String r3 = "org.apache.commons.logging.impl.Jdk14Logger"
            setLogImplementation((java.lang.String) r3)     // Catch:{ Throwable -> 0x0067 }
            goto L_0x0041
        L_0x007a:
            java.lang.String r3 = "org.apache.commons.logging.impl.NoOpLog"
            setLogImplementation((java.lang.String) r3)     // Catch:{ Throwable -> 0x0067 }
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogSource.<clinit>():void");
    }

    private LogSource() {
    }

    public static void setLogImplementation(String classname) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException, ClassNotFoundException {
        try {
            logImplctor = Class.forName(classname).getConstructor(new Class[]{"".getClass()});
        } catch (Throwable th) {
            logImplctor = null;
        }
    }

    public static void setLogImplementation(Class logclass) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException {
        logImplctor = logclass.getConstructor(new Class[]{"".getClass()});
    }

    public static Log getInstance(String name) {
        Log log = (Log) logs.get(name);
        if (log != null) {
            return log;
        }
        Log log2 = makeNewLogInstance(name);
        logs.put(name, log2);
        return log2;
    }

    public static Log getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }

    public static Log makeNewLogInstance(String name) {
        Log log;
        try {
            log = (Log) logImplctor.newInstance(new Object[]{name});
        } catch (Throwable th) {
            log = null;
        }
        if (log == null) {
            return new NoOpLog(name);
        }
        return log;
    }

    public static String[] getLogNames() {
        return (String[]) logs.keySet().toArray(new String[logs.size()]);
    }
}
