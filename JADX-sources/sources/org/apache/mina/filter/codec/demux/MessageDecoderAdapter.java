package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class MessageDecoderAdapter implements MessageDecoder {
    @Override // org.apache.mina.filter.codec.demux.MessageDecoder
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
