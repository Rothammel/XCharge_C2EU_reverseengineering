package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public class DemuxingProtocolDecoder extends CumulativeProtocolDecoder {
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    private final AttributeKey STATE = new AttributeKey(getClass(), "state");
    private MessageDecoderFactory[] decoderFactories = new MessageDecoderFactory[0];

    public void addMessageDecoder(Class<? extends MessageDecoder> decoderClass) {
        if (decoderClass == null) {
            throw new IllegalArgumentException("decoderClass");
        }
        try {
            decoderClass.getConstructor(EMPTY_PARAMS);
            boolean registered = false;
            if (MessageDecoder.class.isAssignableFrom(decoderClass)) {
                addMessageDecoder(new DefaultConstructorMessageDecoderFactory(decoderClass));
                registered = true;
            }
            if (!registered) {
                throw new IllegalArgumentException("Unregisterable type: " + decoderClass);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The specified class doesn't have a public default constructor.");
        }
    }

    public void addMessageDecoder(MessageDecoder decoder) {
        addMessageDecoder(new SingletonMessageDecoderFactory(decoder));
    }

    public void addMessageDecoder(MessageDecoderFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        MessageDecoderFactory[] decoderFactories = this.decoderFactories;
        MessageDecoderFactory[] newDecoderFactories = new MessageDecoderFactory[decoderFactories.length + 1];
        System.arraycopy(decoderFactories, 0, newDecoderFactories, 0, decoderFactories.length);
        newDecoderFactories[decoderFactories.length] = factory;
        this.decoderFactories = newDecoderFactories;
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0038, code lost:
        if (r12 != r4.length) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x003a, code lost:
        r5 = r18.getHexDump();
        r18.position(r18.limit());
        r6 = new org.apache.mina.filter.codec.ProtocolDecoderException("No appropriate message decoder: " + r5);
        r6.setHexdump(r5);
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x0062, code lost:
        throw r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x0099, code lost:
        if (r11.currentDecoder != null) goto L36;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x009b, code lost:
        return false;
     */
    @Override // org.apache.mina.filter.codec.CumulativeProtocolDecoder
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected boolean doDecode(org.apache.mina.core.session.IoSession r17, org.apache.mina.core.buffer.IoBuffer r18, org.apache.mina.filter.codec.ProtocolDecoderOutput r19) throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 238
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.codec.demux.DemuxingProtocolDecoder.doDecode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput):boolean");
    }

    @Override // org.apache.mina.filter.codec.ProtocolDecoderAdapter, org.apache.mina.filter.codec.ProtocolDecoder
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        super.finishDecode(session, out);
        State state = getState(session);
        MessageDecoder currentDecoder = state.currentDecoder;
        if (currentDecoder != null) {
            currentDecoder.finishDecode(session, out);
        }
    }

    @Override // org.apache.mina.filter.codec.CumulativeProtocolDecoder, org.apache.mina.filter.codec.ProtocolDecoderAdapter, org.apache.mina.filter.codec.ProtocolDecoder
    public void dispose(IoSession session) throws Exception {
        super.dispose(session);
        session.removeAttribute(this.STATE);
    }

    private State getState(IoSession session) throws Exception {
        State state = (State) session.getAttribute(this.STATE);
        if (state == null) {
            State state2 = new State();
            State oldState = (State) session.setAttributeIfAbsent(this.STATE, state2);
            if (oldState != null) {
                return oldState;
            }
            return state2;
        }
        return state;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class State {
        private MessageDecoder currentDecoder;
        private final MessageDecoder[] decoders;

        private State() throws Exception {
            MessageDecoderFactory[] decoderFactories = DemuxingProtocolDecoder.this.decoderFactories;
            this.decoders = new MessageDecoder[decoderFactories.length];
            for (int i = decoderFactories.length - 1; i >= 0; i--) {
                this.decoders[i] = decoderFactories[i].getDecoder();
            }
        }
    }

    /* loaded from: classes.dex */
    private static class SingletonMessageDecoderFactory implements MessageDecoderFactory {
        private final MessageDecoder decoder;

        private SingletonMessageDecoderFactory(MessageDecoder decoder) {
            if (decoder == null) {
                throw new IllegalArgumentException("decoder");
            }
            this.decoder = decoder;
        }

        @Override // org.apache.mina.filter.codec.demux.MessageDecoderFactory
        public MessageDecoder getDecoder() {
            return this.decoder;
        }
    }

    /* loaded from: classes.dex */
    private static class DefaultConstructorMessageDecoderFactory implements MessageDecoderFactory {
        private final Class<?> decoderClass;

        private DefaultConstructorMessageDecoderFactory(Class<?> decoderClass) {
            if (decoderClass == null) {
                throw new IllegalArgumentException("decoderClass");
            }
            if (!MessageDecoder.class.isAssignableFrom(decoderClass)) {
                throw new IllegalArgumentException("decoderClass is not assignable to MessageDecoder");
            }
            this.decoderClass = decoderClass;
        }

        @Override // org.apache.mina.filter.codec.demux.MessageDecoderFactory
        public MessageDecoder getDecoder() throws Exception {
            return (MessageDecoder) this.decoderClass.newInstance();
        }
    }
}
