package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class SingleByteDecodingState implements DecodingState {
    protected abstract DecodingState finishDecode(byte b, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.hasRemaining()) {
            return finishDecode(in.get(), out);
        }
        return this;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        throw new ProtocolDecoderException("Unexpected end of session while waiting for a single byte.");
    }
}
