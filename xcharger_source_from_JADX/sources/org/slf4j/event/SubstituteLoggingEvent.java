package org.slf4j.event;

import org.slf4j.Marker;
import org.slf4j.helpers.SubstituteLogger;

public class SubstituteLoggingEvent implements LoggingEvent {
    Object[] argArray;
    Level level;
    SubstituteLogger logger;
    String loggerName;
    Marker marker;
    String message;
    String threadName;
    Throwable throwable;
    long timeStamp;

    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level2) {
        this.level = level2;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker2) {
        this.marker = marker2;
    }

    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String loggerName2) {
        this.loggerName = loggerName2;
    }

    public SubstituteLogger getLogger() {
        return this.logger;
    }

    public void setLogger(SubstituteLogger logger2) {
        this.logger = logger2;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message2) {
        this.message = message2;
    }

    public Object[] getArgumentArray() {
        return this.argArray;
    }

    public void setArgumentArray(Object[] argArray2) {
        this.argArray = argArray2;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public void setThreadName(String threadName2) {
        this.threadName = threadName2;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setThrowable(Throwable throwable2) {
        this.throwable = throwable2;
    }
}
