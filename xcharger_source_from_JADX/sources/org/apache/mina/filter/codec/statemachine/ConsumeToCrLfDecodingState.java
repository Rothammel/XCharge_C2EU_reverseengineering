package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class ConsumeToCrLfDecodingState implements DecodingState {

    /* renamed from: CR */
    private static final byte f189CR = 13;

    /* renamed from: LF */
    private static final byte f190LF = 10;
    private IoBuffer buffer;
    private boolean lastIsCR;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    /* Debug info: failed to restart local var, previous not found, register: 11 */
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
            if (b != 13) {
                if (b == 10 && this.lastIsCR) {
                    terminatorPos = i;
                    break;
                }
                this.lastIsCR = false;
            } else {
                this.lastIsCR = true;
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
        if (!this.lastIsCR) {
            return this;
        }
        this.buffer.position(this.buffer.position() - 1);
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
