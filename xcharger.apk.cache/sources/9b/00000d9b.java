package org.apache.mina.filter.codec.demux;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.UnknownMessageTypeException;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.util.CopyOnWriteMap;
import org.apache.mina.util.IdentityHashSet;

/* loaded from: classes.dex */
public class DemuxingProtocolEncoder implements ProtocolEncoder {
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    private final AttributeKey STATE = new AttributeKey(getClass(), "state");
    private final Map<Class<?>, MessageEncoderFactory> type2encoderFactory = new CopyOnWriteMap();

    public void addMessageEncoder(Class<?> messageType, Class<? extends MessageEncoder> encoderClass) {
        if (encoderClass == null) {
            throw new IllegalArgumentException("encoderClass");
        }
        try {
            encoderClass.getConstructor(EMPTY_PARAMS);
            boolean registered = false;
            if (MessageEncoder.class.isAssignableFrom(encoderClass)) {
                addMessageEncoder(messageType, new DefaultConstructorMessageEncoderFactory(encoderClass));
                registered = true;
            }
            if (!registered) {
                throw new IllegalArgumentException("Unregisterable type: " + encoderClass);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The specified class doesn't have a public default constructor.");
        }
    }

    public <T> void addMessageEncoder(Class<T> messageType, MessageEncoder<? super T> encoder) {
        addMessageEncoder(messageType, new SingletonMessageEncoderFactory(encoder));
    }

    public <T> void addMessageEncoder(Class<T> messageType, MessageEncoderFactory<? super T> factory) {
        if (messageType == null) {
            throw new IllegalArgumentException("messageType");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        synchronized (this.type2encoderFactory) {
            if (this.type2encoderFactory.containsKey(messageType)) {
                throw new IllegalStateException("The specified message type (" + messageType.getName() + ") is registered already.");
            }
            this.type2encoderFactory.put(messageType, factory);
        }
    }

    public void addMessageEncoder(Iterable<Class<?>> messageTypes, Class<? extends MessageEncoder> encoderClass) {
        for (Class<?> messageType : messageTypes) {
            addMessageEncoder(messageType, encoderClass);
        }
    }

    public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes, MessageEncoder<? super T> encoder) {
        for (Class<? extends T> cls : messageTypes) {
            addMessageEncoder(cls, encoder);
        }
    }

    public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes, MessageEncoderFactory<? super T> factory) {
        for (Class<? extends T> cls : messageTypes) {
            addMessageEncoder(cls, factory);
        }
    }

    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        State state = getState(session);
        MessageEncoder<Object> encoder = findEncoder(state, message.getClass());
        if (encoder != null) {
            encoder.encode(session, message, out);
            return;
        }
        throw new UnknownMessageTypeException("No message encoder found for message: " + message);
    }

    protected MessageEncoder<Object> findEncoder(State state, Class<?> type) {
        return findEncoder(state, type, null);
    }

    private MessageEncoder<Object> findEncoder(State state, Class<?> type, Set<Class<?>> triedClasses) {
        Class<?> superclass;
        if (triedClasses != null && triedClasses.contains(type)) {
            return null;
        }
        MessageEncoder encoder = (MessageEncoder) state.findEncoderCache.get(type);
        if (encoder != null) {
            return encoder;
        }
        MessageEncoder encoder2 = (MessageEncoder) state.type2encoder.get(type);
        if (encoder2 == null) {
            if (triedClasses == null) {
                triedClasses = new IdentityHashSet<>();
            }
            triedClasses.add(type);
            Class<?>[] interfaces = type.getInterfaces();
            for (Class<?> element : interfaces) {
                encoder2 = findEncoder(state, element, triedClasses);
                if (encoder2 != null) {
                    break;
                }
            }
        }
        if (encoder2 == null && (superclass = type.getSuperclass()) != null) {
            encoder2 = findEncoder(state, superclass);
        }
        if (encoder2 != null) {
            state.findEncoderCache.put(type, encoder2);
            MessageEncoder tmpEncoder = (MessageEncoder) state.findEncoderCache.putIfAbsent(type, encoder2);
            if (tmpEncoder != null) {
                encoder2 = tmpEncoder;
            }
        }
        return encoder2;
    }

    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void dispose(IoSession session) throws Exception {
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
        private final ConcurrentHashMap<Class<?>, MessageEncoder> findEncoderCache;
        private final Map<Class<?>, MessageEncoder> type2encoder;

        /* JADX WARN: Multi-variable type inference failed */
        private State() throws Exception {
            this.findEncoderCache = new ConcurrentHashMap<>();
            this.type2encoder = new ConcurrentHashMap();
            for (Map.Entry<Class<?>, MessageEncoderFactory> e : DemuxingProtocolEncoder.this.type2encoderFactory.entrySet()) {
                this.type2encoder.put(e.getKey(), e.getValue().getEncoder());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SingletonMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
        private final MessageEncoder<T> encoder;

        private SingletonMessageEncoderFactory(MessageEncoder<T> encoder) {
            if (encoder == null) {
                throw new IllegalArgumentException("encoder");
            }
            this.encoder = encoder;
        }

        @Override // org.apache.mina.filter.codec.demux.MessageEncoderFactory
        public MessageEncoder<T> getEncoder() {
            return this.encoder;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DefaultConstructorMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
        private final Class<MessageEncoder<T>> encoderClass;

        private DefaultConstructorMessageEncoderFactory(Class<MessageEncoder<T>> encoderClass) {
            if (encoderClass == null) {
                throw new IllegalArgumentException("encoderClass");
            }
            if (!MessageEncoder.class.isAssignableFrom(encoderClass)) {
                throw new IllegalArgumentException("encoderClass is not assignable to MessageEncoder");
            }
            this.encoderClass = encoderClass;
        }

        @Override // org.apache.mina.filter.codec.demux.MessageEncoderFactory
        public MessageEncoder<T> getEncoder() throws Exception {
            return this.encoderClass.newInstance();
        }
    }
}