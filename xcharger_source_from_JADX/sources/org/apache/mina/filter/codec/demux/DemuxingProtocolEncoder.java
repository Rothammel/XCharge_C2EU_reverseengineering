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

public class DemuxingProtocolEncoder implements ProtocolEncoder {
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    private final AttributeKey STATE = new AttributeKey(getClass(), "state");
    /* access modifiers changed from: private */
    public final Map<Class<?>, MessageEncoderFactory> type2encoderFactory = new CopyOnWriteMap();

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
        } else if (factory == null) {
            throw new IllegalArgumentException("factory");
        } else {
            synchronized (this.type2encoderFactory) {
                if (this.type2encoderFactory.containsKey(messageType)) {
                    throw new IllegalStateException("The specified message type (" + messageType.getName() + ") is registered already.");
                }
                this.type2encoderFactory.put(messageType, factory);
            }
        }
    }

    public void addMessageEncoder(Iterable<Class<?>> messageTypes, Class<? extends MessageEncoder> encoderClass) {
        for (Class<?> messageType : messageTypes) {
            addMessageEncoder(messageType, encoderClass);
        }
    }

    public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes, MessageEncoder<? super T> encoder) {
        for (Class<? extends T> messageType : messageTypes) {
            addMessageEncoder(messageType, encoder);
        }
    }

    public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes, MessageEncoderFactory<? super T> factory) {
        for (Class<? extends T> messageType : messageTypes) {
            addMessageEncoder(messageType, factory);
        }
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        MessageEncoder<Object> encoder = findEncoder(getState(session), message.getClass());
        if (encoder != null) {
            encoder.encode(session, message, out);
            return;
        }
        throw new UnknownMessageTypeException("No message encoder found for message: " + message);
    }

    /* access modifiers changed from: protected */
    public MessageEncoder<Object> findEncoder(State state, Class<?> type) {
        return findEncoder(state, type, (Set<Class<?>>) null);
    }

    private MessageEncoder<Object> findEncoder(State state, Class<?> type, Set<Class<?>> triedClasses) {
        Class<? super Object> superclass;
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
            for (Class<?> element : type.getInterfaces()) {
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

    public void dispose(IoSession session) throws Exception {
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
        public final ConcurrentHashMap<Class<?>, MessageEncoder> findEncoderCache;
        /* access modifiers changed from: private */
        public final Map<Class<?>, MessageEncoder> type2encoder;

        private State() throws Exception {
            this.findEncoderCache = new ConcurrentHashMap<>();
            this.type2encoder = new ConcurrentHashMap();
            for (Map.Entry<Class<?>, MessageEncoderFactory> e : DemuxingProtocolEncoder.this.type2encoderFactory.entrySet()) {
                this.type2encoder.put(e.getKey(), e.getValue().getEncoder());
            }
        }
    }

    private static class SingletonMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
        private final MessageEncoder<T> encoder;

        private SingletonMessageEncoderFactory(MessageEncoder<T> encoder2) {
            if (encoder2 == null) {
                throw new IllegalArgumentException("encoder");
            }
            this.encoder = encoder2;
        }

        public MessageEncoder<T> getEncoder() {
            return this.encoder;
        }
    }

    private static class DefaultConstructorMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
        private final Class<MessageEncoder<T>> encoderClass;

        private DefaultConstructorMessageEncoderFactory(Class<MessageEncoder<T>> encoderClass2) {
            if (encoderClass2 == null) {
                throw new IllegalArgumentException("encoderClass");
            } else if (!MessageEncoder.class.isAssignableFrom(encoderClass2)) {
                throw new IllegalArgumentException("encoderClass is not assignable to MessageEncoder");
            } else {
                this.encoderClass = encoderClass2;
            }
        }

        public MessageEncoder<T> getEncoder() throws Exception {
            return this.encoderClass.newInstance();
        }
    }
}
