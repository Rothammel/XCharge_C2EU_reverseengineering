package org.apache.mina.filter.logging;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter extends IoFilterAdapter {
    private LogLevel exceptionCaughtLevel;
    private final Logger logger;
    private LogLevel messageReceivedLevel;
    private LogLevel messageSentLevel;
    private final String name;
    private LogLevel sessionClosedLevel;
    private LogLevel sessionCreatedLevel;
    private LogLevel sessionIdleLevel;
    private LogLevel sessionOpenedLevel;

    public LoggingFilter() {
        this(LoggingFilter.class.getName());
    }

    public LoggingFilter(Class<?> clazz) {
        this(clazz.getName());
    }

    public LoggingFilter(String name2) {
        this.exceptionCaughtLevel = LogLevel.WARN;
        this.messageSentLevel = LogLevel.INFO;
        this.messageReceivedLevel = LogLevel.INFO;
        this.sessionCreatedLevel = LogLevel.INFO;
        this.sessionOpenedLevel = LogLevel.INFO;
        this.sessionIdleLevel = LogLevel.INFO;
        this.sessionClosedLevel = LogLevel.INFO;
        if (name2 == null) {
            this.name = LoggingFilter.class.getName();
        } else {
            this.name = name2;
        }
        this.logger = LoggerFactory.getLogger(this.name);
    }

    public String getName() {
        return this.name;
    }

    private void log(LogLevel eventLevel, String message, Throwable cause) {
        switch (eventLevel) {
            case TRACE:
                this.logger.trace(message, cause);
                return;
            case DEBUG:
                this.logger.debug(message, cause);
                return;
            case INFO:
                this.logger.info(message, cause);
                return;
            case WARN:
                this.logger.warn(message, cause);
                return;
            case ERROR:
                this.logger.error(message, cause);
                return;
            default:
                return;
        }
    }

    private void log(LogLevel eventLevel, String message, Object param) {
        switch (eventLevel) {
            case TRACE:
                this.logger.trace(message, param);
                return;
            case DEBUG:
                this.logger.debug(message, param);
                return;
            case INFO:
                this.logger.info(message, param);
                return;
            case WARN:
                this.logger.warn(message, param);
                return;
            case ERROR:
                this.logger.error(message, param);
                return;
            default:
                return;
        }
    }

    private void log(LogLevel eventLevel, String message) {
        switch (eventLevel) {
            case TRACE:
                this.logger.trace(message);
                return;
            case DEBUG:
                this.logger.debug(message);
                return;
            case INFO:
                this.logger.info(message);
                return;
            case WARN:
                this.logger.warn(message);
                return;
            case ERROR:
                this.logger.error(message);
                return;
            default:
                return;
        }
    }

    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        log(this.exceptionCaughtLevel, "EXCEPTION :", cause);
        nextFilter.exceptionCaught(session, cause);
    }

    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        log(this.messageReceivedLevel, "RECEIVED: {}", message);
        nextFilter.messageReceived(session, message);
    }

    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        log(this.messageSentLevel, "SENT: {}", writeRequest.getOriginalRequest().getMessage());
        nextFilter.messageSent(session, writeRequest);
    }

    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        log(this.sessionCreatedLevel, "CREATED");
        nextFilter.sessionCreated(session);
    }

    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        log(this.sessionOpenedLevel, "OPENED");
        nextFilter.sessionOpened(session);
    }

    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        log(this.sessionIdleLevel, "IDLE");
        nextFilter.sessionIdle(session, status);
    }

    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        log(this.sessionClosedLevel, "CLOSED");
        nextFilter.sessionClosed(session);
    }

    public void setExceptionCaughtLogLevel(LogLevel level) {
        this.exceptionCaughtLevel = level;
    }

    public LogLevel getExceptionCaughtLogLevel() {
        return this.exceptionCaughtLevel;
    }

    public void setMessageReceivedLogLevel(LogLevel level) {
        this.messageReceivedLevel = level;
    }

    public LogLevel getMessageReceivedLogLevel() {
        return this.messageReceivedLevel;
    }

    public void setMessageSentLogLevel(LogLevel level) {
        this.messageSentLevel = level;
    }

    public LogLevel getMessageSentLogLevel() {
        return this.messageSentLevel;
    }

    public void setSessionCreatedLogLevel(LogLevel level) {
        this.sessionCreatedLevel = level;
    }

    public LogLevel getSessionCreatedLogLevel() {
        return this.sessionCreatedLevel;
    }

    public void setSessionOpenedLogLevel(LogLevel level) {
        this.sessionOpenedLevel = level;
    }

    public LogLevel getSessionOpenedLogLevel() {
        return this.sessionOpenedLevel;
    }

    public void setSessionIdleLogLevel(LogLevel level) {
        this.sessionIdleLevel = level;
    }

    public LogLevel getSessionIdleLogLevel() {
        return this.sessionIdleLevel;
    }

    public void setSessionClosedLogLevel(LogLevel level) {
        this.sessionClosedLevel = level;
    }

    public LogLevel getSessionClosedLogLevel() {
        return this.sessionClosedLevel;
    }
}
