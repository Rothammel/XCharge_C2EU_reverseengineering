package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

public abstract class ProtocolEncoderAdapter implements ProtocolEncoder {
    public void dispose(IoSession session) throws Exception {
    }
}
