package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public abstract class ProtocolDecoderAdapter implements ProtocolDecoder {
    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoder
    public void dispose(IoSession session) throws Exception {
    }
}