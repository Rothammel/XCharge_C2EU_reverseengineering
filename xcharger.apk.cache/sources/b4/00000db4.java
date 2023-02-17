package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public interface DecodingState {
    DecodingState decode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    DecodingState finishDecode(ProtocolDecoderOutput protocolDecoderOutput) throws Exception;
}