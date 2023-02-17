package org.apache.mina.filter.codec.prefixedstring;

import java.nio.charset.Charset;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/* loaded from: classes.dex */
public class PrefixedStringCodecFactory implements ProtocolCodecFactory {
    private final PrefixedStringDecoder decoder;
    private final PrefixedStringEncoder encoder;

    public PrefixedStringCodecFactory(Charset charset) {
        this.encoder = new PrefixedStringEncoder(charset);
        this.decoder = new PrefixedStringDecoder(charset);
    }

    public PrefixedStringCodecFactory() {
        this(Charset.defaultCharset());
    }

    public int getEncoderMaxDataLength() {
        return this.encoder.getMaxDataLength();
    }

    public void setEncoderMaxDataLength(int maxDataLength) {
        this.encoder.setMaxDataLength(maxDataLength);
    }

    public int getDecoderMaxDataLength() {
        return this.decoder.getMaxDataLength();
    }

    public void setDecoderMaxDataLength(int maxDataLength) {
        this.decoder.setMaxDataLength(maxDataLength);
    }

    public void setDecoderPrefixLength(int prefixLength) {
        this.decoder.setPrefixLength(prefixLength);
    }

    public int getDecoderPrefixLength() {
        return this.decoder.getPrefixLength();
    }

    public void setEncoderPrefixLength(int prefixLength) {
        this.encoder.setPrefixLength(prefixLength);
    }

    public int getEncoderPrefixLength() {
        return this.encoder.getPrefixLength();
    }

    @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return this.encoder;
    }

    @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this.decoder;
    }
}