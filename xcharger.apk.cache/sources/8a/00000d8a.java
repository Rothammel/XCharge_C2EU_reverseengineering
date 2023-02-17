package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface ProtocolDecoder {
    void decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    void dispose(IoSession ioSession) throws Exception;

    void finishDecode(IoSession ioSession, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;
}