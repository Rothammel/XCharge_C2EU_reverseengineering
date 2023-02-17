package org.eclipse.paho.client.mqttv3.logging;

import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/* loaded from: classes.dex */
public class LoggerFactory {
    public static final String MQTT_CLIENT_MSG_CAT = "org.eclipse.paho.client.mqttv3.internal.nls.logcat";
    private static final String CLASS_NAME = LoggerFactory.class.getName();
    private static String overrideloggerClassName = null;
    private static String jsr47LoggerClassName = JSR47Logger.class.getName();

    public static Logger getLogger(String messageCatalogName, String loggerID) {
        String loggerClassName = overrideloggerClassName;
        if (loggerClassName == null) {
            loggerClassName = jsr47LoggerClassName;
        }
        Logger logger = getLogger(loggerClassName, ResourceBundle.getBundle(messageCatalogName), loggerID, null);
        if (logger == null) {
            throw new MissingResourceException("Error locating the logging class", CLASS_NAME, loggerID);
        }
        return logger;
    }

    private static Logger getLogger(String loggerClassName, ResourceBundle messageCatalog, String loggerID, String resourceName) {
        Logger logger = null;
        try {
            Class logClass = Class.forName(loggerClassName);
            if (logClass != null) {
                try {
                    logger = (Logger) logClass.newInstance();
                    logger.initialise(messageCatalog, loggerID, resourceName);
                } catch (ExceptionInInitializerError e) {
                    return null;
                } catch (IllegalAccessException e2) {
                    return null;
                } catch (InstantiationException e3) {
                    return null;
                } catch (SecurityException e4) {
                    return null;
                }
            }
            return logger;
        } catch (ClassNotFoundException e5) {
            return null;
        } catch (NoClassDefFoundError e6) {
            return null;
        }
    }

    public static String getLoggingProperty(String name) {
        try {
            Class logManagerClass = Class.forName("java.util.logging.LogManager");
            Method m1 = logManagerClass.getMethod("getLogManager", new Class[0]);
            Object logManagerInstance = m1.invoke(null, null);
            Method m2 = logManagerClass.getMethod("getProperty", String.class);
            String result = (String) m2.invoke(logManagerInstance, name);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setLogger(String loggerClassName) {
        overrideloggerClassName = loggerClassName;
    }
}
