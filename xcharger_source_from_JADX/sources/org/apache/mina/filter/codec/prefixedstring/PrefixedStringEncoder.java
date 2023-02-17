package org.apache.mina.filter.codec.prefixedstring;

import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class PrefixedStringEncoder extends ProtocolEncoderAdapter {
    public static final int DEFAULT_MAX_DATA_LENGTH = 2048;
    public static final int DEFAULT_PREFIX_LENGTH = 4;
    private final Charset charset;
    private int maxDataLength;
    private int prefixLength;

    public PrefixedStringEncoder(Charset charset2, int prefixLength2, int maxDataLength2) {
        this.prefixLength = 4;
        this.maxDataLength = 2048;
        this.charset = charset2;
        this.prefixLength = prefixLength2;
        this.maxDataLength = maxDataLength2;
    }

    public PrefixedStringEncoder(Charset charset2, int prefixLength2) {
        this(charset2, prefixLength2, 2048);
    }

    public PrefixedStringEncoder(Charset charset2) {
        this(charset2, 4);
    }

    public PrefixedStringEncoder() {
        this(Charset.defaultCharset());
    }

    public void setPrefixLength(int prefixLength2) {
        if (prefixLength2 == 1 || prefixLength2 == 2 || prefixLength2 == 4) {
            this.prefixLength = prefixLength2;
            return;
        }
        throw new IllegalArgumentException("prefixLength: " + prefixLength2);
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public void setMaxDataLength(int maxDataLength2) {
        this.maxDataLength = maxDataLength2;
    }

    public int getMaxDataLength() {
        return this.maxDataLength;
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        String value = (String) message;
        IoBuffer buffer = IoBuffer.allocate(value.length()).setAutoExpand(true);
        buffer.putPrefixedString(value, this.prefixLength, this.charset.newEncoder());
        if (buffer.position() > this.maxDataLength) {
            throw new IllegalArgumentException("Data length: " + buffer.position());
        }
        buffer.flip();
        out.write(buffer);
    }
}
