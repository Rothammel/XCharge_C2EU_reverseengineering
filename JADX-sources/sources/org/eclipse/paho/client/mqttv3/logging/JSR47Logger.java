package org.eclipse.paho.client.mqttv3.logging;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

/* loaded from: classes.dex */
public class JSR47Logger implements Logger {
    private java.util.logging.Logger julLogger = null;
    private ResourceBundle logMessageCatalog = null;
    private ResourceBundle traceMessageCatalog = null;
    private String catalogID = null;
    private String resourceName = null;
    private String loggerName = null;

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void initialise(ResourceBundle logMsgCatalog, String loggerID, String resourceContext) {
        this.traceMessageCatalog = this.logMessageCatalog;
        this.resourceName = resourceContext;
        this.loggerName = loggerID;
        this.julLogger = java.util.logging.Logger.getLogger(this.loggerName);
        this.logMessageCatalog = logMsgCatalog;
        this.traceMessageCatalog = logMsgCatalog;
        this.catalogID = this.logMessageCatalog.getString("0");
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void setResourceName(String logContext) {
        this.resourceName = logContext;
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public boolean isLoggable(int level) {
        return this.julLogger.isLoggable(mapJULLevel(level));
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void severe(String sourceClass, String sourceMethod, String msg) {
        log(1, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(1, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(1, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void warning(String sourceClass, String sourceMethod, String msg) {
        log(2, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(2, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(2, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void info(String sourceClass, String sourceMethod, String msg) {
        log(3, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(3, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(3, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void config(String sourceClass, String sourceMethod, String msg) {
        log(4, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(4, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(4, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void log(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Level julLevel = mapJULLevel(level);
        if (this.julLogger.isLoggable(julLevel)) {
            logToJsr47(julLevel, sourceClass, sourceMethod, this.catalogID, this.logMessageCatalog, msg, inserts, thrown);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void fine(String sourceClass, String sourceMethod, String msg) {
        trace(5, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(5, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(5, sourceClass, sourceMethod, msg, inserts, ex);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finer(String sourceClass, String sourceMethod, String msg) {
        trace(6, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(6, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(6, sourceClass, sourceMethod, msg, inserts, ex);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finest(String sourceClass, String sourceMethod, String msg) {
        trace(7, sourceClass, sourceMethod, msg, null, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(7, sourceClass, sourceMethod, msg, inserts, null);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(7, sourceClass, sourceMethod, msg, inserts, ex);
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void trace(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Level julLevel = mapJULLevel(level);
        boolean isJULLoggable = this.julLogger.isLoggable(julLevel);
        if (isJULLoggable) {
            logToJsr47(julLevel, sourceClass, sourceMethod, this.catalogID, this.traceMessageCatalog, msg, inserts, ex);
        }
    }

    private String getResourceMessage(ResourceBundle messageCatalog, String msg) {
        try {
            String message = messageCatalog.getString(msg);
            return message;
        } catch (MissingResourceException e) {
            return msg;
        }
    }

    private void logToJsr47(Level julLevel, String sourceClass, String sourceMethod, String catalogName, ResourceBundle messageCatalog, String msg, Object[] inserts, Throwable thrown) {
        String formattedWithArgs = msg;
        if (msg.indexOf("=====") == -1) {
            formattedWithArgs = MessageFormat.format(getResourceMessage(messageCatalog, msg), inserts);
        }
        LogRecord logRecord = new LogRecord(julLevel, String.valueOf(this.resourceName) + ": " + formattedWithArgs);
        logRecord.setSourceClassName(sourceClass);
        logRecord.setSourceMethodName(sourceMethod);
        logRecord.setLoggerName(this.loggerName);
        if (thrown != null) {
            logRecord.setThrown(thrown);
        }
        this.julLogger.log(logRecord);
    }

    private Level mapJULLevel(int level) {
        switch (level) {
            case 1:
                Level julLevel = Level.SEVERE;
                return julLevel;
            case 2:
                Level julLevel2 = Level.WARNING;
                return julLevel2;
            case 3:
                Level julLevel3 = Level.INFO;
                return julLevel3;
            case 4:
                Level julLevel4 = Level.CONFIG;
                return julLevel4;
            case 5:
                Level julLevel5 = Level.FINE;
                return julLevel5;
            case 6:
                Level julLevel6 = Level.FINER;
                return julLevel6;
            case 7:
                Level julLevel7 = Level.FINEST;
                return julLevel7;
            default:
                return null;
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public String formatMessage(String msg, Object[] inserts) {
        try {
            String formatString = this.logMessageCatalog.getString(msg);
            return formatString;
        } catch (MissingResourceException e) {
            return msg;
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.logging.Logger
    public void dumpTrace() {
        dumpMemoryTrace47(this.julLogger);
    }

    protected static void dumpMemoryTrace47(java.util.logging.Logger logger) {
        if (logger != null) {
            Handler[] handlers = logger.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                if (handlers[i] instanceof MemoryHandler) {
                    synchronized (handlers[i]) {
                        MemoryHandler mHand = (MemoryHandler) handlers[i];
                        mHand.push();
                    }
                    return;
                }
            }
            dumpMemoryTrace47(logger.getParent());
        }
    }
}
