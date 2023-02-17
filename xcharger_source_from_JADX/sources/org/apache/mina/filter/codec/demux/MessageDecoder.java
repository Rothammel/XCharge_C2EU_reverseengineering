package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public interface MessageDecoder {
    public static final MessageDecoderResult NEED_DATA = MessageDecoderResult.NEED_DATA;
    public static final MessageDecoderResult NOT_OK = MessageDecoderResult.NOT_OK;

    /* renamed from: OK */
    public static final MessageDecoderResult f186OK = MessageDecoderResult.f187OK;

    MessageDecoderResult decodable(IoSession ioSession, IoBuffer ioBuffer);

    MessageDecoderResult decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    void finishDecode(IoSession ioSession, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;
}
