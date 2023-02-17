package org.apache.mina.filter.codec.statemachine;

import java.util.ArrayList;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public abstract class DecodingStateMachine implements DecodingState {
    private DecodingState currentState;
    private boolean initialized;
    private final Logger log = LoggerFactory.getLogger(DecodingStateMachine.class);
    private final List<Object> childProducts = new ArrayList();
    private final ProtocolDecoderOutput childOutput = new ProtocolDecoderOutput() { // from class: org.apache.mina.filter.codec.statemachine.DecodingStateMachine.1
        @Override // org.apache.mina.filter.codec.ProtocolDecoderOutput
        public void flush(IoFilter.NextFilter nextFilter, IoSession session) {
        }

        @Override // org.apache.mina.filter.codec.ProtocolDecoderOutput
        public void write(Object message) {
            DecodingStateMachine.this.childProducts.add(message);
        }
    };

    protected abstract void destroy() throws Exception;

    protected abstract DecodingState finishDecode(List<Object> list, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    protected abstract DecodingState init() throws Exception;

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        DecodingState state = getCurrentState();
        int limit = in.limit();
        int pos = in.position();
        while (pos != limit) {
            DecodingState oldState = state;
            try {
                try {
                    state = state.decode(in, this.childOutput);
                    if (state == null) {
                        DecodingState finishDecode = finishDecode(this.childProducts, out);
                        this.currentState = state;
                        if (state == null) {
                            cleanup();
                        }
                        return finishDecode;
                    }
                    int newPos = in.position();
                    if (newPos == pos && oldState == state) {
                        break;
                    }
                    pos = newPos;
                } catch (Exception e) {
                    state = null;
                    throw e;
                }
            } finally {
                this.currentState = state;
                if (state == null) {
                    cleanup();
                }
            }
        }
    }

    @Override // org.apache.mina.filter.codec.statemachine.DecodingState
    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        DecodingState oldState;
        DecodingState nextState;
        DecodingState state = getCurrentState();
        do {
            oldState = state;
            try {
                try {
                    state = state.finishDecode(this.childOutput);
                    if (state != null) {
                        break;
                        break;
                    }
                    break;
                } catch (Exception e) {
                    state = null;
                    this.log.debug("Ignoring the exception caused by a closed session.", (Throwable) e);
                    this.currentState = null;
                    nextState = finishDecode(this.childProducts, out);
                    if (0 == 0) {
                        cleanup();
                    }
                }
            } finally {
                this.currentState = state;
                finishDecode(this.childProducts, out);
                if (state == null) {
                    cleanup();
                }
            }
        } while (oldState != state);
        return nextState;
    }

    private void cleanup() {
        if (!this.initialized) {
            throw new IllegalStateException();
        }
        this.initialized = false;
        this.childProducts.clear();
        try {
            destroy();
        } catch (Exception e2) {
            this.log.warn("Failed to destroy a decoding state machine.", (Throwable) e2);
        }
    }

    private DecodingState getCurrentState() throws Exception {
        DecodingState state = this.currentState;
        if (state == null) {
            DecodingState state2 = init();
            this.initialized = true;
            return state2;
        }
        return state;
    }
}