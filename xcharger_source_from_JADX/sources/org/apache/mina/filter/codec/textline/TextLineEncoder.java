package org.apache.mina.filter.codec.textline;

import com.alibaba.sdk.android.oss.common.RequestParameters;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class TextLineEncoder extends ProtocolEncoderAdapter {
    private static final AttributeKey ENCODER = new AttributeKey(TextLineEncoder.class, "encoder");
    private final Charset charset;
    private final LineDelimiter delimiter;
    private int maxLineLength;

    public TextLineEncoder() {
        this(Charset.defaultCharset(), LineDelimiter.UNIX);
    }

    public TextLineEncoder(String delimiter2) {
        this(new LineDelimiter(delimiter2));
    }

    public TextLineEncoder(LineDelimiter delimiter2) {
        this(Charset.defaultCharset(), delimiter2);
    }

    public TextLineEncoder(Charset charset2) {
        this(charset2, LineDelimiter.UNIX);
    }

    public TextLineEncoder(Charset charset2, String delimiter2) {
        this(charset2, new LineDelimiter(delimiter2));
    }

    public TextLineEncoder(Charset charset2, LineDelimiter delimiter2) {
        this.maxLineLength = Integer.MAX_VALUE;
        if (charset2 == null) {
            throw new IllegalArgumentException("charset");
        } else if (delimiter2 == null) {
            throw new IllegalArgumentException(RequestParameters.DELIMITER);
        } else if (LineDelimiter.AUTO.equals(delimiter2)) {
            throw new IllegalArgumentException("AUTO delimiter is not allowed for encoder.");
        } else {
            this.charset = charset2;
            this.delimiter = delimiter2;
        }
    }

    public int getMaxLineLength() {
        return this.maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength2) {
        if (maxLineLength2 <= 0) {
            throw new IllegalArgumentException("maxLineLength: " + maxLineLength2);
        }
        this.maxLineLength = maxLineLength2;
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        CharsetEncoder encoder = (CharsetEncoder) session.getAttribute(ENCODER);
        if (encoder == null) {
            encoder = this.charset.newEncoder();
            session.setAttribute(ENCODER, encoder);
        }
        String value = message == null ? "" : message.toString();
        IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
        buf.putString(value, encoder);
        if (buf.position() > this.maxLineLength) {
            throw new IllegalArgumentException("Line length: " + buf.position());
        }
        buf.putString(this.delimiter.getValue(), encoder);
        buf.flip();
        out.write(buf);
    }

    public void dispose() throws Exception {
    }
}
