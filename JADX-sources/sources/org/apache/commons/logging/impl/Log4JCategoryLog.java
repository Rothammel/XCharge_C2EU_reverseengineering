package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/* loaded from: classes.dex */
public final class Log4JCategoryLog implements Log {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JCategoryLog;
    private Category category;

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
        this.category = null;
    }

    public Log4JCategoryLog(String name) {
        this.category = null;
        this.category = Category.getInstance(name);
    }

    public Log4JCategoryLog(Category category) {
        this.category = null;
        this.category = category;
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message) {
        this.category.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message, Throwable t) {
        this.category.log(FQCN, Priority.DEBUG, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message) {
        this.category.log(FQCN, Priority.DEBUG, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message, Throwable t) {
        this.category.log(FQCN, Priority.DEBUG, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message) {
        this.category.log(FQCN, Priority.INFO, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message, Throwable t) {
        this.category.log(FQCN, Priority.INFO, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message) {
        this.category.log(FQCN, Priority.WARN, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message, Throwable t) {
        this.category.log(FQCN, Priority.WARN, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message) {
        this.category.log(FQCN, Priority.ERROR, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message, Throwable t) {
        this.category.log(FQCN, Priority.ERROR, message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message) {
        this.category.log(FQCN, Priority.FATAL, message, (Throwable) null);
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message, Throwable t) {
        this.category.log(FQCN, Priority.FATAL, message, t);
    }

    public Category getCategory() {
        return this.category;
    }

    @Override // org.apache.commons.logging.Log
    public boolean isDebugEnabled() {
        return this.category.isDebugEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isErrorEnabled() {
        return this.category.isEnabledFor(Priority.ERROR);
    }

    @Override // org.apache.commons.logging.Log
    public boolean isFatalEnabled() {
        return this.category.isEnabledFor(Priority.FATAL);
    }

    @Override // org.apache.commons.logging.Log
    public boolean isInfoEnabled() {
        return this.category.isInfoEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isTraceEnabled() {
        return this.category.isDebugEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isWarnEnabled() {
        return this.category.isEnabledFor(Priority.WARN);
    }
}
