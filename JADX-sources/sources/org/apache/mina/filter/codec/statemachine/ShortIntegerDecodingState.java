package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class ShortIntegerDecodingState implements DecodingState {
    private int counter;

    protected abstract DecodingState finishDecode(short s, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int highByte = 0;
        while (in.hasRemaining()) {
            switch (this.counter) {
                case 0:
                    highByte = in.getUnsigned();
                    this.counter++;
                case 1:
                    this.counter = 0;
                    return finishDecode((short) ((highByte << 8) | in.getUnsigned()), out);
                default:
                    throw new InternalError();
            }
        }
        return this;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        throw new ProtocolDecoderException("Unexpected end of session while waiting for a short integer.");
    }
}
