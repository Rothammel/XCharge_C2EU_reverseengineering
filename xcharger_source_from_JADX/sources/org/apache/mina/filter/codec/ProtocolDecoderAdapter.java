package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

public abstract class ProtocolDecoderAdapter implements ProtocolDecoder {
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }

    public void dispose(IoSession session) throws Exception {
    }
}
