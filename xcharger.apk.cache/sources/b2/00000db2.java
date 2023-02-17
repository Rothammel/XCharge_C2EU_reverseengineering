package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class ConsumeToTerminatorDecodingState implements DecodingState {
    private IoBuffer buffer;
    private final byte terminator;

    protected abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    public ConsumeToTerminatorDecodingState(byte terminator) {
        this.terminator = terminator;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        IoBuffer product;
        int terminatorPos = in.indexOf(this.terminator);
        if (terminatorPos >= 0) {
            int limit = in.limit();
            if (in.position() < terminatorPos) {
                in.limit(terminatorPos);
                if (this.buffer == null) {
                    product = in.slice();
                } else {
                    this.buffer.put(in);
                    product = this.buffer.flip();
                    this.buffer = null;
                }
                in.limit(limit);
            } else if (this.buffer == null) {
                product = IoBuffer.allocate(0);
            } else {
                product = this.buffer.flip();
                this.buffer = null;
            }
            in.position(terminatorPos + 1);
            return finishDecode(product, out);
        }
        if (this.buffer == null) {
            this.buffer = IoBuffer.allocate(in.remaining());
            this.buffer.setAutoExpand(true);
        }
        this.buffer.put(in);
        return this;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        IoBuffer product;
        if (this.buffer == null) {
            product = IoBuffer.allocate(0);
        } else {
            product = this.buffer.flip();
            this.buffer = null;
        }
        return finishDecode(product, out);
    }
}