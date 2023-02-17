package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class SkippingState implements DecodingState {
    private int skippedBytes;

    /* access modifiers changed from: protected */
    public abstract boolean canSkip(byte b);

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(int i) throws Exception;

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int beginPos = in.position();
        int limit = in.limit();
        for (int i = beginPos; i < limit; i++) {
            if (!canSkip(in.get(i))) {
                in.position(i);
                int answer = this.skippedBytes;
                this.skippedBytes = 0;
                return finishDecode(answer);
            }
            this.skippedBytes++;
        }
        in.position(limit);
        return this;
    }

    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        return finishDecode(this.skippedBytes);
    }
}
