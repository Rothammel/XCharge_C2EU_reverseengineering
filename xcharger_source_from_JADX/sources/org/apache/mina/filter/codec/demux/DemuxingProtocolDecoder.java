package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class DemuxingProtocolDecoder extends CumulativeProtocolDecoder {
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    private final AttributeKey STATE = new AttributeKey(getClass(), "state");
    /* access modifiers changed from: private */
    public MessageDecoderFactory[] decoderFactories = new MessageDecoderFactory[0];

    public void addMessageDecoder(Class<? extends MessageDecoder> decoderClass) {
        if (decoderClass == null) {
            throw new IllegalArgumentException("decoderClass");
        }
        try {
            decoderClass.getConstructor(EMPTY_PARAMS);
            boolean registered = false;
            if (MessageDecoder.class.isAssignableFrom(decoderClass)) {
                addMessageDecoder((MessageDecoderFactory) new DefaultConstructorMessageDecoderFactory(decoderClass));
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
        addMessageDecoder((MessageDecoderFactory) new SingletonMessageDecoderFactory(decoder));
    }

    public void addMessageDecoder(MessageDecoderFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        MessageDecoderFactory[] decoderFactories2 = this.decoderFactories;
        MessageDecoderFactory[] newDecoderFactories = new MessageDecoderFactory[(decoderFactories2.length + 1)];
        System.arraycopy(decoderFactories2, 0, newDecoderFactories, 0, decoderFactories2.length);
        newDecoderFactories[decoderFactories2.length] = factory;
        this.decoderFactories = newDecoderFactories;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        State state = getState(session);
        if (state.currentDecoder == null) {
            MessageDecoder[] decoders = state.decoders;
            int undecodables = 0;
            int i = decoders.length - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                MessageDecoder decoder = decoders[i];
                int limit = in.limit();
                int pos = in.position();
                try {
                    MessageDecoderResult result = decoder.decodable(session, in);
                    in.position(pos);
                    in.limit(limit);
                    if (result == MessageDecoder.f186OK) {
                        MessageDecoder unused = state.currentDecoder = decoder;
                        break;
                    }
                    if (result == MessageDecoder.NOT_OK) {
                        undecodables++;
                    } else if (result != MessageDecoder.NEED_DATA) {
                        throw new IllegalStateException("Unexpected decode result (see your decodable()): " + result);
                    }
                    i--;
                } catch (Throwable th) {
                    in.position(pos);
                    in.limit(limit);
                    throw th;
                }
            }
            if (undecodables == decoders.length) {
                String dump = in.getHexDump();
                in.position(in.limit());
                ProtocolDecoderException e = new ProtocolDecoderException("No appropriate message decoder: " + dump);
                e.setHexdump(dump);
                throw e;
            } else if (state.currentDecoder == null) {
                return false;
            }
        }
        try {
            MessageDecoderResult result2 = state.currentDecoder.decode(session, in, out);
            if (result2 == MessageDecoder.f186OK) {
                MessageDecoder unused2 = state.currentDecoder = null;
                return true;
            } else if (result2 == MessageDecoder.NEED_DATA) {
                return false;
            } else {
                if (result2 == MessageDecoder.NOT_OK) {
                    MessageDecoder unused3 = state.currentDecoder = null;
                    throw new ProtocolDecoderException("Message decoder returned NOT_OK.");
                }
                MessageDecoder unused4 = state.currentDecoder = null;
                throw new IllegalStateException("Unexpected decode result (see your decode()): " + result2);
            }
        } catch (Exception e2) {
            MessageDecoder unused5 = state.currentDecoder = null;
            throw e2;
        }
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        super.finishDecode(session, out);
        MessageDecoder currentDecoder = getState(session).currentDecoder;
        if (currentDecoder != null) {
            currentDecoder.finishDecode(session, out);
        }
    }

    public void dispose(IoSession session) throws Exception {
        super.dispose(session);
        session.removeAttribute(this.STATE);
    }

    private State getState(IoSession session) throws Exception {
        State state = (State) session.getAttribute(this.STATE);
        if (state != null) {
            return state;
        }
        State state2 = new State();
        State oldState = (State) session.setAttributeIfAbsent(this.STATE, state2);
        if (oldState != null) {
            return oldState;
        }
        return state2;
    }

    private class State {
        /* access modifiers changed from: private */
        public MessageDecoder currentDecoder;
        /* access modifiers changed from: private */
        public final MessageDecoder[] decoders;

        private State() throws Exception {
            MessageDecoderFactory[] decoderFactories = DemuxingProtocolDecoder.this.decoderFactories;
            this.decoders = new MessageDecoder[decoderFactories.length];
            for (int i = decoderFactories.length - 1; i >= 0; i--) {
                this.decoders[i] = decoderFactories[i].getDecoder();
            }
        }
    }

    private static class SingletonMessageDecoderFactory implements MessageDecoderFactory {
        private final MessageDecoder decoder;

        private SingletonMessageDecoderFactory(MessageDecoder decoder2) {
            if (decoder2 == null) {
                throw new IllegalArgumentException("decoder");
            }
            this.decoder = decoder2;
        }

        public MessageDecoder getDecoder() {
            return this.decoder;
        }
    }

    private static class DefaultConstructorMessageDecoderFactory implements MessageDecoderFactory {
        private final Class<?> decoderClass;

        private DefaultConstructorMessageDecoderFactory(Class<?> decoderClass2) {
            if (decoderClass2 == null) {
                throw new IllegalArgumentException("decoderClass");
            } else if (!MessageDecoder.class.isAssignableFrom(decoderClass2)) {
                throw new IllegalArgumentException("decoderClass is not assignable to MessageDecoder");
            } else {
                this.decoderClass = decoderClass2;
            }
        }

        public MessageDecoder getDecoder() throws Exception {
            return (MessageDecoder) this.decoderClass.newInstance();
        }
    }
}
