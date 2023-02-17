package org.apache.mina.filter.codec.statemachine;

import java.util.ArrayList;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecodingStateMachine implements DecodingState {
    private final ProtocolDecoderOutput childOutput = new ProtocolDecoderOutput() {
        public void flush(IoFilter.NextFilter nextFilter, IoSession session) {
        }

        public void write(Object message) {
            DecodingStateMachine.this.childProducts.add(message);
        }
    };
    /* access modifiers changed from: private */
    public final List<Object> childProducts = new ArrayList();
    private DecodingState currentState;
    private boolean initialized;
    private final Logger log = LoggerFactory.getLogger((Class<?>) DecodingStateMachine.class);

    /* access modifiers changed from: protected */
    public abstract void destroy() throws Exception;

    /* access modifiers changed from: protected */
    public abstract DecodingState finishDecode(List<Object> list, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    /* access modifiers changed from: protected */
    public abstract DecodingState init() throws Exception;

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        DecodingState state = getCurrentState();
        int limit = in.limit();
        int pos = in.position();
        while (pos != limit) {
            DecodingState oldState = state;
            try {
                state = state.decode(in, this.childOutput);
                if (state != null) {
                    int newPos = in.position();
                    if (newPos == pos && oldState == state) {
                        break;
                    }
                    pos = newPos;
                } else {
                    DecodingState finishDecode = finishDecode(this.childProducts, out);
                    this.currentState = state;
                    if (state == null) {
                        cleanup();
                    }
                    return finishDecode;
                }
            } catch (Exception e) {
                throw e;
            } catch (Throwable th) {
                this.currentState = null;
                if (0 == 0) {
                    cleanup();
                }
                throw th;
            }
        }
        this.currentState = state;
        if (state != null) {
            return this;
        }
        cleanup();
        return this;
    }

    public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
        DecodingState oldState;
        DecodingState nextState;
        DecodingState state = getCurrentState();
        do {
            oldState = state;
            try {
                state = state.finishDecode(this.childOutput);
                if (state != null) {
                    break;
                    break;
                }
                break;
            } catch (Exception e) {
                this.log.debug("Ignoring the exception caused by a closed session.", (Throwable) e);
                this.currentState = null;
                nextState = finishDecode(this.childProducts, out);
                if (0 == 0) {
                    cleanup();
                }
            } catch (Throwable th) {
                this.currentState = null;
                DecodingState finishDecode = finishDecode(this.childProducts, out);
                if (0 == 0) {
                    cleanup();
                }
                throw th;
            }
        } while (oldState != state);
        this.currentState = state;
        nextState = finishDecode(this.childProducts, out);
        if (state == null) {
            cleanup();
        }
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
        if (state != null) {
            return state;
        }
        DecodingState state2 = init();
        this.initialized = true;
        return state2;
    }
}
