package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public abstract class CrLfDecodingState implements DecodingState {
    private static final byte CR = 13;
    private static final byte LF = 10;
    private boolean hasCR;

    protected abstract DecodingState finishDecode(boolean z, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        boolean found = false;
        boolean finished = false;
        while (true) {
            if (!in.hasRemaining()) {
                break;
            }
            byte b = in.get();
            if (!this.hasCR) {
                if (b == 13) {
                    this.hasCR = true;
                } else {
                    if (b == 10) {
                        found = true;
                    } else {
                        in.position(in.position() - 1);
                        found = false;
                    }
                    finished = true;
                }
            } else if (b == 10) {
                found = true;
                finished = true;
            } else {
                throw new ProtocolDecoderException("Expected LF after CR but was: " + (b & 255));
            }
        }
        if (finished) {
            this.hasCR = false;
            return finishDecode(found, out);
        }
        return this;
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        return finishDecode(false, out);
    }
}
