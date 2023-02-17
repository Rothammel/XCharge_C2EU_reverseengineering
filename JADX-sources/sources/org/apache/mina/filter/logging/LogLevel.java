package org.apache.mina.filter.logging;

/* loaded from: classes.dex */
public enum LogLevel {
    TRACE(5),
    DEBUG(4),
    INFO(3),
    WARN(2),
    ERROR(1),
    NONE(0);
    
    private int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}
