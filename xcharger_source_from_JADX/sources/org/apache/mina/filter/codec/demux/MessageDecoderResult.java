package org.apache.mina.filter.codec.demux;

public class MessageDecoderResult {
    public static final MessageDecoderResult NEED_DATA = new MessageDecoderResult("NEED_DATA");
    public static final MessageDecoderResult NOT_OK = new MessageDecoderResult("NOT_OK");

    /* renamed from: OK */
    public static final MessageDecoderResult f187OK = new MessageDecoderResult("OK");
    private final String name;

    private MessageDecoderResult(String name2) {
        this.name = name2;
    }

    public String toString() {
        return this.name;
    }
}
