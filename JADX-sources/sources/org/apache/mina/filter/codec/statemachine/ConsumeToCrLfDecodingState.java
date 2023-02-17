package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class ConsumeToCrLfDecodingState implements DecodingState {
    private static final byte CR = 13;
    private static final byte LF = 10;
    private IoBuffer buffer;
    private boolean lastIsCR;

    protected abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        IoBuffer product;
        int beginPos = in.position();
        int limit = in.limit();
        int terminatorPos = -1;
        int i = beginPos;
        while (true) {
            if (i >= limit) {
                break;
            }
            byte b = in.get(i);
            if (b == 13) {
                this.lastIsCR = true;
            } else if (b == 10 && this.lastIsCR) {
                terminatorPos = i;
                break;
            } else {
                this.lastIsCR = false;
            }
            i++;
        }
        if (terminatorPos >= 0) {
            int endPos = terminatorPos - 1;
            if (beginPos < endPos) {
                in.limit(endPos);
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
        in.position(beginPos);
        if (this.buffer == null) {
            this.buffer = IoBuffer.allocate(in.remaining());
            this.buffer.setAutoExpand(true);
        }
        this.buffer.put(in);
        if (this.lastIsCR) {
            this.buffer.position(this.buffer.position() - 1);
            return this;
        }
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
