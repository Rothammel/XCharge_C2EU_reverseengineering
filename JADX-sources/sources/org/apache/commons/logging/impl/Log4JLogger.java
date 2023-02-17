package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/* loaded from: classes.dex */
public final class Log4JLogger implements Log {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JLogger;
    private Logger logger;

    static {
        Class cls;
        if (class$org$apache$commons$logging$impl$Log4JLogger == null) {
            cls = class$("org.apache.commons.logging.impl.Log4JLogger");
            class$org$apache$commons$logging$impl$Log4JLogger = cls;
        } else {
            cls = class$org$apache$commons$logging$impl$Log4JLogger;
        }
        FQCN = cls.getName();
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public Log4JLogger() {
        this.logger = null;
    }

    public Log4JLogger(String name) {
        this.logger = null;
        this.logger = Logger.getLogger(name);
    }

    public Log4JLogger(Logger logger) {
        this.logger = null;
        this.logger = logger;
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message) {
        this.logger.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.DEBUG, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message) {
        this.logger.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.DEBUG, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message) {
        this.logger.log(FQCN, Priority.INFO, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.INFO, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message) {
        this.logger.log(FQCN, Priority.WARN, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.WARN, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message) {
        this.logger.log(FQCN, Priority.ERROR, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.ERROR, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message) {
        this.logger.log(FQCN, Priority.FATAL, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.FATAL, message, t);
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override // org.apache.commons.logging.Log
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isErrorEnabled() {
        return this.logger.isEnabledFor(Priority.ERROR);
    }

    @Override // org.apache.commons.logging.Log
    public boolean isFatalEnabled() {
        return this.logger.isEnabledFor(Priority.FATAL);
    }

    @Override // org.apache.commons.logging.Log
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isTraceEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isWarnEnabled() {
        return this.logger.isEnabledFor(Priority.WARN);
    }
}
