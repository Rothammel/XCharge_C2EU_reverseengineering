package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public interface MessageDecoder {
    public static final MessageDecoderResult OK = MessageDecoderResult.OK;
    public static final MessageDecoderResult NEED_DATA = MessageDecoderResult.NEED_DATA;
    public static final MessageDecoderResult NOT_OK = MessageDecoderResult.NOT_OK;

    MessageDecoderResult decodable(IoSession ioSession, IoBuffer ioBuffer);

    MessageDecoderResult decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    void finishDecode(IoSession ioSession, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;
}
