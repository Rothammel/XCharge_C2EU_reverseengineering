package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class FixedLengthDecodingState implements DecodingState {
    private IoBuffer buffer;
    private final int length;

    protected abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    public FixedLengthDecodingState(int length) {
        this.length = length;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (this.buffer == null) {
            if (in.remaining() >= this.length) {
                int limit = in.limit();
                in.limit(in.position() + this.length);
                IoBuffer product = in.slice();
                in.position(in.position() + this.length);
                in.limit(limit);
                return finishDecode(product, out);
            }
            this.buffer = IoBuffer.allocate(this.length);
            this.buffer.put(in);
            return this;
        } else if (in.remaining() >= this.length - this.buffer.position()) {
            int limit2 = in.limit();
            in.limit((in.position() + this.length) - this.buffer.position());
            this.buffer.put(in);
            in.limit(limit2);
            IoBuffer product2 = this.buffer;
            this.buffer = null;
            return finishDecode(product2.flip(), out);
        } else {
            this.buffer.put(in);
            return this;
        }
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        IoBuffer readData;
        if (this.buffer == null) {
            readData = IoBuffer.allocate(0);
        } else {
            readData = this.buffer.flip();
            this.buffer = null;
        }
        return finishDecode(readData, out);
    }
}
