package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class ConsumeToEndOfSessionDecodingState implements DecodingState {
    private IoBuffer buffer;
    private final int maxLength;

    protected abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    public ConsumeToEndOfSessionDecodingState(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (this.buffer == null) {
            this.buffer = IoBuffer.allocate(256).setAutoExpand(true);
        }
        if (this.buffer.position() + in.remaining() > this.maxLength) {
            throw new ProtocolDecoderException("Received data exceeds " + this.maxLength + " byte(s).");
        }
        this.buffer.put(in);
        return this;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        try {
            if (this.buffer == null) {
                this.buffer = IoBuffer.allocate(0);
            }
            this.buffer.flip();
            return finishDecode(this.buffer, out);
        } finally {
            this.buffer = null;
        }
    }
}