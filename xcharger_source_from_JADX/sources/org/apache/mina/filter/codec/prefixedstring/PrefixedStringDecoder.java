package org.apache.mina.filter.codec.prefixedstring;

import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PrefixedStringDecoder extends CumulativeProtocolDecoder {
    public static final int DEFAULT_MAX_DATA_LENGTH = 2048;
    public static final int DEFAULT_PREFIX_LENGTH = 4;
    private final Charset charset;
    private int maxDataLength;
    private int prefixLength;

    public PrefixedStringDecoder(Charset charset2, int prefixLength2, int maxDataLength2) {
        this.prefixLength = 4;
        this.maxDataLength = 2048;
        this.charset = charset2;
        this.prefixLength = prefixLength2;
        this.maxDataLength = maxDataLength2;
    }

    public PrefixedStringDecoder(Charset charset2, int prefixLength2) {
        this(charset2, prefixLength2, 2048);
    }

    public PrefixedStringDecoder(Charset charset2) {
        this(charset2, 4);
    }

    public void setPrefixLength(int prefixLength2) {
        this.prefixLength = prefixLength2;
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

    /* access modifiers changed from: protected */
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (!in.prefixedDataAvailable(this.prefixLength, this.maxDataLength)) {
            return false;
        }
        out.write(in.getPrefixedString(this.prefixLength, this.charset.newDecoder()));
        return true;
    }
}
