package org.eclipse.paho.client.mqttv3.logging;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LoggerFactory {
    private static final String CLASS_NAME = LoggerFactory.class.getName();
    public static final String MQTT_CLIENT_MSG_CAT = "org.eclipse.paho.client.mqttv3.internal.nls.logcat";
    private static String jsr47LoggerClassName = JSR47Logger.class.getName();
    private static String overrideloggerClassName = null;

    public static Logger getLogger(String messageCatalogName, String loggerID) {
        String loggerClassName = overrideloggerClassName;
        if (loggerClassName == null) {
            loggerClassName = jsr47LoggerClassName;
        }
        Logger logger = getLogger(loggerClassName, ResourceBundle.getBundle(messageCatalogName), loggerID, (String) null);
        if (logger != null) {
            return logger;
        }
        throw new MissingResourceException("Error locating the logging class", CLASS_NAME, loggerID);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: org.eclipse.paho.client.mqttv3.logging.Logger} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static org.eclipse.paho.client.mqttv3.logging.Logger getLogger(java.lang.String r5, java.util.ResourceBundle r6, java.lang.String r7, java.lang.String r8) {
        /*
            r4 = 0
            r2 = 0
            r1 = 0
            java.lang.Class r1 = java.lang.Class.forName(r5)     // Catch:{ NoClassDefFoundError -> 0x0016, ClassNotFoundException -> 0x0019 }
            if (r1 == 0) goto L_0x0014
            java.lang.Object r3 = r1.newInstance()     // Catch:{ IllegalAccessException -> 0x001c, InstantiationException -> 0x001f, ExceptionInInitializerError -> 0x0022, SecurityException -> 0x0025 }
            r0 = r3
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = (org.eclipse.paho.client.mqttv3.logging.Logger) r0     // Catch:{ IllegalAccessException -> 0x001c, InstantiationException -> 0x001f, ExceptionInInitializerError -> 0x0022, SecurityException -> 0x0025 }
            r2 = r0
            r2.initialise(r6, r7, r8)
        L_0x0014:
            r3 = r2
        L_0x0015:
            return r3
        L_0x0016:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        L_0x0019:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        L_0x001c:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        L_0x001f:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        L_0x0022:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        L_0x0025:
            r3 = move-exception
            r3 = r4
            goto L_0x0015
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.logging.LoggerFactory.getLogger(java.lang.String, java.util.ResourceBundle, java.lang.String, java.lang.String):org.eclipse.paho.client.mqttv3.logging.Logger");
    }

    public static String getLoggingProperty(String name) {
        try {
            Class logManagerClass = Class.forName("java.util.logging.LogManager");
            Object logManagerInstance = logManagerClass.getMethod("getLogManager", new Class[0]).invoke((Object) null, (Object[]) null);
            return (String) logManagerClass.getMethod("getProperty", new Class[]{String.class}).invoke(logManagerInstance, new Object[]{name});
        } catch (Exception e) {
            return null;
        }
    }

    public static void setLogger(String loggerClassName) {
        overrideloggerClassName = loggerClassName;
    }
}
