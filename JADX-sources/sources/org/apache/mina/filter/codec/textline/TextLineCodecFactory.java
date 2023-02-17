package org.apache.mina.filter.codec.textline;

import java.nio.charset.Charset;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/* loaded from: classes.dex */
public class TextLineCodecFactory implements ProtocolCodecFactory {
    private final TextLineDecoder decoder;
    private final TextLineEncoder encoder;

    public TextLineCodecFactory() {
        this(Charset.defaultCharset());
    }

    public TextLineCodecFactory(Charset charset) {
        this.encoder = new TextLineEncoder(charset, LineDelimiter.UNIX);
        this.decoder = new TextLineDecoder(charset, LineDelimiter.AUTO);
    }

    public TextLineCodecFactory(Charset charset, String encodingDelimiter, String decodingDelimiter) {
        this.encoder = new TextLineEncoder(charset, encodingDelimiter);
        this.decoder = new TextLineDecoder(charset, decodingDelimiter);
    }

    public TextLineCodecFactory(Charset charset, LineDelimiter encodingDelimiter, LineDelimiter decodingDelimiter) {
        this.encoder = new TextLineEncoder(charset, encodingDelimiter);
        this.decoder = new TextLineDecoder(charset, decodingDelimiter);
    }

    @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
    public ProtocolEncoder getEncoder(IoSession session) {
        return this.encoder;
    }

    @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
    public ProtocolDecoder getDecoder(IoSession session) {
        return this.decoder;
    }

    public int getEncoderMaxLineLength() {
        return this.encoder.getMaxLineLength();
    }

    public void setEncoderMaxLineLength(int maxLineLength) {
        this.encoder.setMaxLineLength(maxLineLength);
    }

    public int getDecoderMaxLineLength() {
        return this.decoder.getMaxLineLength();
    }

    public void setDecoderMaxLineLength(int maxLineLength) {
        this.decoder.setMaxLineLength(maxLineLength);
    }
}
