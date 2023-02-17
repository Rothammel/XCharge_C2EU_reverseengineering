package org.apache.mina.filter.codec.textline;

import java.nio.ByteBuffer;
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

public class TextLineDecoder implements ProtocolDecoder {
    private final AttributeKey CONTEXT;
    private int bufferLength;
    /* access modifiers changed from: private */
    public final Charset charset;
    private IoBuffer delimBuf;
    private final LineDelimiter delimiter;
    /* access modifiers changed from: private */
    public int maxLineLength;

    public TextLineDecoder() {
        this(LineDelimiter.AUTO);
    }

    public TextLineDecoder(String delimiter2) {
        this(new LineDelimiter(delimiter2));
    }

    public TextLineDecoder(LineDelimiter delimiter2) {
        this(Charset.defaultCharset(), delimiter2);
    }

    public TextLineDecoder(Charset charset2) {
        this(charset2, LineDelimiter.AUTO);
    }

    public TextLineDecoder(Charset charset2, String delimiter2) {
        this(charset2, new LineDelimiter(delimiter2));
    }

    public TextLineDecoder(Charset charset2, LineDelimiter delimiter2) {
        this.CONTEXT = new AttributeKey(getClass(), "context");
        this.maxLineLength = 1024;
        this.bufferLength = 128;
        if (charset2 == null) {
            throw new IllegalArgumentException("charset parameter shuld not be null");
        } else if (delimiter2 == null) {
            throw new IllegalArgumentException("delimiter parameter should not be null");
        } else {
            this.charset = charset2;
            this.delimiter = delimiter2;
            if (this.delimBuf == null) {
                IoBuffer tmp = IoBuffer.allocate(2).setAutoExpand(true);
                try {
                    tmp.putString(delimiter2.getValue(), charset2.newEncoder());
                } catch (CharacterCodingException e) {
                }
                tmp.flip();
                this.delimBuf = tmp;
            }
        }
    }

    public int getMaxLineLength() {
        return this.maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength2) {
        if (maxLineLength2 <= 0) {
            throw new IllegalArgumentException("maxLineLength (" + maxLineLength2 + ") should be a positive value");
        }
        this.maxLineLength = maxLineLength2;
    }

    public void setBufferLength(int bufferLength2) {
        if (bufferLength2 <= 0) {
            throw new IllegalArgumentException("bufferLength (" + this.maxLineLength + ") should be a positive value");
        }
        this.bufferLength = bufferLength2;
    }

    public int getBufferLength() {
        return this.bufferLength;
    }

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
        if (ctx != null) {
            return ctx;
        }
        Context ctx2 = new Context(this.bufferLength);
        session.setAttribute(this.CONTEXT, ctx2);
        return ctx2;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }

    public void dispose(IoSession session) throws Exception {
        if (((Context) session.getAttribute(this.CONTEXT)) != null) {
            session.removeAttribute(this.CONTEXT);
        }
    }

    /* JADX INFO: finally extract failed */
    private void decodeAuto(Context ctx, IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        int oldLimit = in.limit();
        while (in.hasRemaining()) {
            boolean matched = false;
            switch (in.get()) {
                case 10:
                    matchCount++;
                    matched = true;
                    break;
                case 13:
                    matchCount++;
                    break;
                default:
                    matchCount = 0;
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
                        writeText(session, ctx.getDecoder().decode(ByteBuffer.wrap(data)).toString(), out);
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

    /* JADX INFO: finally extract failed */
    private void decodeNormal(Context ctx, IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws CharacterCodingException, ProtocolDecoderException {
        int matchCount = ctx.getMatchCount();
        int oldPos = in.position();
        int oldLimit = in.limit();
        while (in.hasRemaining()) {
            if (this.delimBuf.get(matchCount) == in.get()) {
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

    /* access modifiers changed from: protected */
    public void writeText(IoSession session, String text, ProtocolDecoderOutput out) {
        out.write(text);
    }

    private class Context {
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

        public void setMatchCount(int matchCount2) {
            this.matchCount = matchCount2;
        }

        public void reset() {
            this.overflowPosition = 0;
            this.matchCount = 0;
            this.decoder.reset();
        }

        public void append(IoBuffer in) {
            if (this.overflowPosition != 0) {
                discard(in);
            } else if (this.buf.position() > TextLineDecoder.this.maxLineLength - in.remaining()) {
                this.overflowPosition = this.buf.position();
                this.buf.clear();
                discard(in);
            } else {
                getBuffer().put(in);
            }
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
