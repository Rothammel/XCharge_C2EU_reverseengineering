package org.apache.mina.filter.codec.demux;

/* loaded from: classes.dex */
public class MessageDecoderResult {
    private final String name;
    public static final MessageDecoderResult OK = new MessageDecoderResult("OK");
    public static final MessageDecoderResult NEED_DATA = new MessageDecoderResult("NEED_DATA");
    public static final MessageDecoderResult NOT_OK = new MessageDecoderResult("NOT_OK");

    private MessageDecoderResult(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
