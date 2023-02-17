package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class SynchronizedProtocolDecoder implements ProtocolDecoder {
    private final ProtocolDecoder decoder;

    public SynchronizedProtocolDecoder(ProtocolDecoder decoder) {
        if (decoder == null) {
            throw new IllegalArgumentException("decoder");
        }
        this.decoder = decoder;
    }

    public ProtocolDecoder getDecoder() {
        return this.decoder;
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        synchronized (this.decoder) {
            this.decoder.decode(session, in, out);
        }
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        synchronized (this.decoder) {
            this.decoder.finishDecode(session, out);
        }
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void dispose(IoSession session) throws Exception {
        synchronized (this.decoder) {
            this.decoder.dispose(session);
        }
    }
}
