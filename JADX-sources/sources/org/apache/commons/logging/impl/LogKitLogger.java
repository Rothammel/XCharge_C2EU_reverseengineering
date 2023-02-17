package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/* loaded from: classes.dex */
public final class LogKitLogger implements Log {
    protected Logger logger;

    public LogKitLogger(String name) {
        this.logger = null;
        this.logger = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message) {
        debug(message);
    }

    @Override // org.apache.commons.logging.Log
    public void trace(Object message, Throwable t) {
        debug(message, t);
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message) {
        if (message != null) {
            this.logger.debug(String.valueOf(message));
        }
    }

    @Override // org.apache.commons.logging.Log
    public void debug(Object message, Throwable t) {
        if (message != null) {
            this.logger.debug(String.valueOf(message), t);
        }
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message) {
        if (message != null) {
            this.logger.info(String.valueOf(message));
        }
    }

    @Override // org.apache.commons.logging.Log
    public void info(Object message, Throwable t) {
        if (message != null) {
            this.logger.info(String.valueOf(message), t);
        }
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message) {
        if (message != null) {
            this.logger.warn(String.valueOf(message));
        }
    }

    @Override // org.apache.commons.logging.Log
    public void warn(Object message, Throwable t) {
        if (message != null) {
            this.logger.warn(String.valueOf(message), t);
        }
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message) {
        if (message != null) {
            this.logger.error(String.valueOf(message));
        }
    }

    @Override // org.apache.commons.logging.Log
    public void error(Object message, Throwable t) {
        if (message != null) {
            this.logger.error(String.valueOf(message), t);
        }
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message) {
        if (message != null) {
            this.logger.fatalError(String.valueOf(message));
        }
    }

    @Override // org.apache.commons.logging.Log
    public void fatal(Object message, Throwable t) {
        if (message != null) {
            this.logger.fatalError(String.valueOf(message), t);
        }
    }

    @Override // org.apache.commons.logging.Log
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override // org.apache.commons.logging.Log
    public boolean isFatalEnabled() {
        return this.logger.isFatalErrorEnabled();
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
        return this.logger.isWarnEnabled();
    }
}
