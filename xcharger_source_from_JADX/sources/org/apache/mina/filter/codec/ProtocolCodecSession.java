package org.apache.mina.filter.codec;

import java.util.Queue;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;

public class ProtocolCodecSession extends DummySession {
    private final AbstractProtocolDecoderOutput decoderOutput = new AbstractProtocolDecoderOutput() {
        public void flush(IoFilter.NextFilter nextFilter, IoSession session) {
        }
    };
    private final AbstractProtocolEncoderOutput encoderOutput = new AbstractProtocolEncoderOutput() {
        public WriteFuture flush() {
            return ProtocolCodecSession.this.notWrittenFuture;
        }
    };
    /* access modifiers changed from: private */
    public final WriteFuture notWrittenFuture = DefaultWriteFuture.newNotWrittenFuture(this, new UnsupportedOperationException());

    public ProtocolEncoderOutput getEncoderOutput() {
        return this.encoderOutput;
    }

    public Queue<Object> getEncoderOutputQueue() {
        return this.encoderOutput.getMessageQueue();
    }

    public ProtocolDecoderOutput getDecoderOutput() {
        return this.decoderOutput;
    }

    public Queue<Object> getDecoderOutputQueue() {
        return this.decoderOutput.getMessageQueue();
    }
}
