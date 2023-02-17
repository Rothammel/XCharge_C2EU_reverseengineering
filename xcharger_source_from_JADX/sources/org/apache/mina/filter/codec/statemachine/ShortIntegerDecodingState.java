package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class ShortIntegerDecodingState implements DecodingState {
    private int counter;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(short s, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

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

    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        throw new ProtocolDecoderException("Unexpected end of session while waiting for a short integer.");
    }
}
