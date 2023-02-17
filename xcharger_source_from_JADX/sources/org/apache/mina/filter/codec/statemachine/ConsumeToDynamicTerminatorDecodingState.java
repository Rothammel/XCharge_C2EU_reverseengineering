package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class ConsumeToDynamicTerminatorDecodingState implements DecodingState {
    private IoBuffer buffer;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    /* access modifiers changed from: protected */
    public abstract boolean isTerminator(byte b);

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        IoBuffer product;
        int beginPos = in.position();
        int terminatorPos = -1;
        int limit = in.limit();
        int i = beginPos;
        while (true) {
            if (i >= limit) {
                break;
            } else if (isTerminator(in.get(i))) {
                terminatorPos = i;
                break;
            } else {
                i++;
            }
        }
        if (terminatorPos >= 0) {
            if (beginPos < terminatorPos) {
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
