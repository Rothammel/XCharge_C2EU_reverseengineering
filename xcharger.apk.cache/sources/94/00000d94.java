package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class SynchronizedProtocolEncoder implements ProtocolEncoder {
    private final ProtocolEncoder encoder;

    public SynchronizedProtocolEncoder(ProtocolEncoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("encoder");
        }
        this.encoder = encoder;
    }

    public ProtocolEncoder getEncoder() {
        return this.encoder;
    }

    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        synchronized (this.encoder) {
            this.encoder.encode(session, message, out);
        }
    }

    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void dispose(IoSession session) throws Exception {
        synchronized (this.encoder) {
            this.encoder.dispose(session);
        }
    }
}