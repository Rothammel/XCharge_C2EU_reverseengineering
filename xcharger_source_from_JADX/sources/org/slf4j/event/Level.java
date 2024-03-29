package org.slf4j.event;

import org.apache.http.client.methods.HttpTraceHC4;

public enum Level {
    ERROR(40, "ERROR"),
    WARN(30, "WARN"),
    INFO(20, "INFO"),
    DEBUG(10, "DEBUG"),
    TRACE(0, HttpTraceHC4.METHOD_NAME);
    
    private int levelInt;
    private String levelStr;

    private Level(int i, String s) {
        this.levelInt = i;
        this.levelStr = s;
    }

    public int toInt() {
        return this.levelInt;
    }

    public String toString() {
        return this.levelStr;
    }
}
