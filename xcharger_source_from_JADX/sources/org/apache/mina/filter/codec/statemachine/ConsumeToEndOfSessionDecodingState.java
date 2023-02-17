package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class ConsumeToEndOfSessionDecodingState implements DecodingState {
    private IoBuffer buffer;
    private final int maxLength;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    public ConsumeToEndOfSessionDecodingState(int maxLength2) {
        this.maxLength = maxLength2;
    }

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

    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        IoBuffer ioBuffer = null;
        try {
            if (this.buffer == null) {
                ioBuffer = IoBuffer.allocate(0);
            }
            this.buffer.flip();
            DecodingState finishDecode = finishDecode(this.buffer, out);
            this.buffer = ioBuffer;
            return finishDecode;
        } finally {
            this.buffer = ioBuffer;
        }
    }
}
