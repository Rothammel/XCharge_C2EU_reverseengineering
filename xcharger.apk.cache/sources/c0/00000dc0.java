package org.apache.mina.filter.codec.textline;

import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException;

/* loaded from: classes.dex */
public class TextLineDecoder implements ProtocolDecoder {
    private final AttributeKey CONTEXT;
    private int bufferLength;
    private final Charset charset;
    private IoBuffer delimBuf;
    private final LineDelimiter delimiter;
    private int maxLineLength;

    public TextLineDecoder() {
        this(LineDelimiter.AUTO);
    }

    public TextLineDecoder(String delimiter) {
        this(new LineDelimiter(delimiter));
    }

    public TextLineDecoder(LineDelimiter delimiter) {
        this(Charset.defaultCharset(), delimiter);
    }

    public TextLineDecoder(Charset charset) {
        this(charset, LineDelimiter.AUTO);
    }

    public TextLineDecoder(Charset charset, String delimiter) {
        this(charset, new LineDelimiter(delimiter));
    }

    public TextLineDecoder(Charset charset, LineDelimiter delimiter) {
        this.CONTEXT = new AttributeKey(getClass(), "context");
        this.maxLineLength = 1024;
        this.bufferLength = 128;
        if (charset == null) {
            throw new IllegalArgumentException("charset parameter shuld not be null");
        }
        if (delimiter == null) {
            throw new IllegalArgumentException("delimiter parameter should not be null");
        }
        this.charset = charset;
        this.delimiter = delimiter;
        if (this.delimBuf == null) {
            IoBuffer tmp = IoBuffer.allocate(2).setAutoExpand(true);
            try {
                tmp.putString(delimiter.getValue(), charset.newEncoder());
            } catch (CharacterCodingException e) {
            }
            tmp.flip();
            this.delimBuf = tmp;
        }
    }

    public int getMaxLineLength() {
        return this.maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength) {
        if (maxLineLength <= 0) {
            throw new IllegalArgumentException("maxLineLength (" + maxLineLength + ") should be a positive value");
        }
        this.maxLineLength = maxLineLength;
    }

    public void setBufferLength(int bufferLength) {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength (" + this.maxLineLength + ") should be a positive value");
        }
        this.bufferLength = bufferLength;
    }

    public int getBufferLength() {
        return this.bufferLength;
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        Context ctx = getContext(session);
        if (LineDelimiter.AUTO.equals(this.delimiter)) {
            decodeAuto(ctx, session, in, out);
        } else {
            decodeNormal(ctx, session, in, out);
        }
    }

    private Context getContext(IoSession session) {
        Context ctx = (Context) session.getAttribute(this.CONTEXT);
        if (ctx == null) {
            Context ctx2 = new Context(this.bufferLength);
            session.setAttribute(this.CONTEXT, ctx2);
            return ctx2;
        }
        return ctx;
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void dispose(IoSession session) throws Exception {
        Context ctx = (Context) session.getAttribute(this.CONTEXT);
        if (ctx != null) {
            session.removeAttribute(this.CONTEXT);
        }
    }

    private void decodeAuto(Context ctx, IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        int oldLimit = in.limit();
        while (in.hasRemaining()) {
            byte b = in.get();
            boolean matched = false;
            switch (b) {
                case 10:
                    matchCount++;
                    matched = true;
                    break;
                case PortRuntimeData.STATUS_EX_11 /* 11 */:
                case PortRuntimeData.STATUS_EX_12 /* 12 */:
                default:
                    matchCount = 0;
                    break;
                case 13:
                    matchCount++;
                    break;
            }
            if (matched) {
                int pos = in.position();
                in.limit(pos);
                in.position(oldPos);
                ctx.append(in);
                in.limit(oldLimit);
                in.position(pos);
                if (ctx.getOverflowPosition() == 0) {
                    IoBuffer buf = ctx.getBuffer();
                    buf.flip();
                    buf.limit(buf.limit() - matchCount);
                    try {
                        byte[] data = new byte[buf.limit()];
                        buf.get(data);
                        CharsetDecoder decoder = ctx.getDecoder();
                        CharBuffer buffer = decoder.decode(ByteBuffer.wrap(data));
                        String str = buffer.toString();
                        writeText(session, str, out);
                        buf.clear();
                        oldPos = pos;
                        matchCount = 0;
                    } catch (Throwable th) {
                        buf.clear();
                        throw th;
                    }
                } else {
                    int overflowPosition = ctx.getOverflowPosition();
                    ctx.reset();
                    throw new RecoverableProtocolDecoderException("Line is too long: " + overflowPosition);
                }
            }
        }
        in.position(oldPos);
        ctx.append(in);
        ctx.setMatchCount(matchCount);
    }

    private void decodeNormal(Context ctx, IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        int oldLimit = in.limit();
        while (in.hasRemaining()) {
            byte b = in.get();
            if (this.delimBuf.get(matchCount) == b) {
                matchCount++;
                if (matchCount == this.delimBuf.limit()) {
                    int pos = in.position();
                    in.limit(pos);
                    in.position(oldPos);
                    ctx.append(in);
                    in.limit(oldLimit);
                    in.position(pos);
                    if (ctx.getOverflowPosition() == 0) {
                        IoBuffer buf = ctx.getBuffer();
                        buf.flip();
                        buf.limit(buf.limit() - matchCount);
                        try {
                            writeText(session, buf.getString(ctx.getDecoder()), out);
                            buf.clear();
                            oldPos = pos;
                            matchCount = 0;
                        } catch (Throwable th) {
                            buf.clear();
                            throw th;
                        }
                    } else {
                        int overflowPosition = ctx.getOverflowPosition();
                        ctx.reset();
                        throw new RecoverableProtocolDecoderException("Line is too long: " + overflowPosition);
                    }
                } else {
                    continue;
                }
            } else {
                in.position(Math.max(0, in.position() - matchCount));
                matchCount = 0;
            }
        }
        in.position(oldPos);
        ctx.append(in);
        ctx.setMatchCount(matchCount);
    }

    protected void writeText(IoSession session, String text, ProtocolDecoderOutput out) {
        out.write(text);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Context {
        private final IoBuffer buf;
        private final CharsetDecoder decoder;
        private int matchCount;
        private int overflowPosition;

        private Context(int bufferLength) {
            this.matchCount = 0;
            this.overflowPosition = 0;
            this.decoder = TextLineDecoder.this.charset.newDecoder();
            this.buf = IoBuffer.allocate(bufferLength).setAutoExpand(true);
        }

        public CharsetDecoder getDecoder() {
            return this.decoder;
        }

        public IoBuffer getBuffer() {
            return this.buf;
        }

        public int getOverflowPosition() {
            return this.overflowPosition;
        }

        public int getMatchCount() {
            return this.matchCount;
        }

        public void setMatchCount(int matchCount) {
            this.matchCount = matchCount;
        }

        public void reset() {
            this.overflowPosition = 0;
            this.matchCount = 0;
            this.decoder.reset();
        }

        public void append(IoBuffer in) {
            if (this.overflowPosition == 0) {
                if (this.buf.position() > TextLineDecoder.this.maxLineLength - in.remaining()) {
                    this.overflowPosition = this.buf.position();
                    this.buf.clear();
                    discard(in);
                    return;
                }
                getBuffer().put(in);
                return;
            }
            discard(in);
        }

        private void discard(IoBuffer in) {
            if (Integer.MAX_VALUE - in.remaining() < this.overflowPosition) {
                this.overflowPosition = Integer.MAX_VALUE;
            } else {
                this.overflowPosition += in.remaining();
            }
            in.position(in.limit());
        }
    }
}