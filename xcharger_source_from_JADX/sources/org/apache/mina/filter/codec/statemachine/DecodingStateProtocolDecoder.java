package org.apache.mina.filter.codec.statemachine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class DecodingStateProtocolDecoder implements ProtocolDecoder {
    private IoSession session;
    private final DecodingState state;
    private final Queue<IoBuffer> undecodedBuffers = new ConcurrentLinkedQueue();

    public DecodingStateProtocolDecoder(DecodingState state2) {
        if (state2 == null) {
            throw new IllegalArgumentException("state");
        }
        this.state = state2;
    }

    public void decode(IoSession session2, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (this.session == null) {
            this.session = session2;
        } else if (this.session != session2) {
            throw new IllegalStateException(getClass().getSimpleName() + " is a stateful decoder.  " + "You have to create one per session.");
        }
        this.undecodedBuffers.offer(in);
        while (true) {
            IoBuffer b = this.undecodedBuffers.peek();
            if (b != null) {
                int oldRemaining = b.remaining();
                this.state.decode(b, out);
                int newRemaining = b.remaining();
                if (newRemaining == 0) {
                    this.undecodedBuffers.poll();
                } else if (oldRemaining == newRemaining) {
                    throw new IllegalStateException(DecodingState.class.getSimpleName() + " must " + "consume at least one byte per decode().");
                }
            } else {
                return;
            }
        }
    }

    public void finishDecode(IoSession session2, ProtocolDecoderOutput out) throws Exception {
        this.state.finishDecode(out);
    }

    public void dispose(IoSession session2) throws Exception {
    }
}
