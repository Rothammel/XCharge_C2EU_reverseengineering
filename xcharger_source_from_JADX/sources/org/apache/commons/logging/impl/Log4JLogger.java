package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public final class Log4JLogger implements Log {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JLogger;
    private Logger logger = null;

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
    }

    public Log4JLogger(String name) {
        this.logger = Logger.getLogger(name);
    }

    public Log4JLogger(Logger logger2) {
        this.logger = logger2;
    }

    public void trace(Object message) {
        this.logger.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    public void trace(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.DEBUG, message, t);
    }

    public void debug(Object message) {
        this.logger.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    public void debug(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.DEBUG, message, t);
    }

    public void info(Object message) {
        this.logger.log(FQCN, Priority.INFO, message, (Throwable) null);
    }

    public void info(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.INFO, message, t);
    }

    public void warn(Object message) {
        this.logger.log(FQCN, Priority.WARN, message, (Throwable) null);
    }

    public void warn(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.WARN, message, t);
    }

    public void error(Object message) {
        this.logger.log(FQCN, Priority.ERROR, message, (Throwable) null);
    }

    public void error(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.ERROR, message, t);
    }

    public void fatal(Object message) {
        this.logger.log(FQCN, Priority.FATAL, message, (Throwable) null);
    }

    public void fatal(Object message, Throwable t) {
        this.logger.log(FQCN, Priority.FATAL, message, t);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.logger.isEnabledFor(Priority.ERROR);
    }

    public boolean isFatalEnabled() {
        return this.logger.isEnabledFor(Priority.FATAL);
    }

    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return this.logger.isDebugEnabled();
    }

    public boolean isWarnEnabled() {
        return this.logger.isEnabledFor(Priority.WARN);
    }
}
