package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

public final class Log4JCategoryLog implements Log {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JCategoryLog;
    private Category category = null;

    static {
        Class cls;
        if (class$org$apache$commons$logging$impl$Log4JCategoryLog == null) {
            cls = class$("org.apache.commons.logging.impl.Log4JCategoryLog");
            class$org$apache$commons$logging$impl$Log4JCategoryLog = cls;
        } else {
            cls = class$org$apache$commons$logging$impl$Log4JCategoryLog;
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

    public Log4JCategoryLog() {
    }

    public Log4JCategoryLog(String name) {
        this.category = Category.getInstance(name);
    }

    public Log4JCategoryLog(Category category2) {
        this.category = category2;
    }

    public void trace(Object message) {
        this.category.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    public void trace(Object message, Throwable t) {
        this.category.log(FQCN, Priority.DEBUG, message, t);
    }

    public void debug(Object message) {
        this.category.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    public void debug(Object message, Throwable t) {
        this.category.log(FQCN, Priority.DEBUG, message, t);
    }

    public void info(Object message) {
        this.category.log(FQCN, Priority.INFO, message, (Throwable) null);
    }

    public void info(Object message, Throwable t) {
        this.category.log(FQCN, Priority.INFO, message, t);
    }

    public void warn(Object message) {
        this.category.log(FQCN, Priority.WARN, message, (Throwable) null);
    }

    public void warn(Object message, Throwable t) {
        this.category.log(FQCN, Priority.WARN, message, t);
    }

    public void error(Object message) {
        this.category.log(FQCN, Priority.ERROR, message, (Throwable) null);
    }

    public void error(Object message, Throwable t) {
        this.category.log(FQCN, Priority.ERROR, message, t);
    }

    public void fatal(Object message) {
        this.category.log(FQCN, Priority.FATAL, message, (Throwable) null);
    }

    public void fatal(Object message, Throwable t) {
        this.category.log(FQCN, Priority.FATAL, message, t);
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isDebugEnabled() {
        return this.category.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.category.isEnabledFor(Priority.ERROR);
    }

    public boolean isFatalEnabled() {
        return this.category.isEnabledFor(Priority.FATAL);
    }

    public boolean isInfoEnabled() {
        return this.category.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return this.category.isDebugEnabled();
    }

    public boolean isWarnEnabled() {
        return this.category.isEnabledFor(Priority.WARN);
    }
}
