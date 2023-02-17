package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class CrLfDecodingState implements DecodingState {

    /* renamed from: CR */
    private static final byte f191CR = 13;

    /* renamed from: LF */
    private static final byte f192LF = 10;
    private boolean hasCR;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(boolean z, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    /* Debug info: failed to restart local var, previous not found, register: 6 */
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
        if (!finished) {
            return this;
        }
        this.hasCR = false;
        return finishDecode(found, out);
    }

    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        return finishDecode(false, out);
    }
}
