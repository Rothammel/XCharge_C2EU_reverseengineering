package org.apache.mina.core.session;

/* loaded from: classes.dex */
public class IdleStatus {
    private final String strValue;
    public static final IdleStatus READER_IDLE = new IdleStatus("reader idle");
    public static final IdleStatus WRITER_IDLE = new IdleStatus("writer idle");
    public static final IdleStatus BOTH_IDLE = new IdleStatus("both idle");

    private IdleStatus(String strValue) {
        this.strValue = strValue;
    }

    public String toString() {
        return this.strValue;
    }
}
