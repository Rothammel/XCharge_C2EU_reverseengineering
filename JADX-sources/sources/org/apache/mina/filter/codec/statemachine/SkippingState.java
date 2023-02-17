package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class SkippingState implements DecodingState {
    private int skippedBytes;

    protected abstract boolean canSkip(byte b);

    protected abstract DecodingState finishDecode(int i) throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int beginPos = in.position();
        int limit = in.limit();
        for (int i = beginPos; i < limit; i++) {
            byte b = in.get(i);
            if (!canSkip(b)) {
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

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        return finishDecode(this.skippedBytes);
    }
}
