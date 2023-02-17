package org.apache.mina.handler.demux;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.UnknownMessageTypeException;
import org.apache.mina.util.IdentityHashSet;

/* loaded from: classes.dex */
public class DemuxingIoHandler extends IoHandlerAdapter {
    private final Map<Class<?>, MessageHandler<?>> receivedMessageHandlerCache = new ConcurrentHashMap();
    private final Map<Class<?>, MessageHandler<?>> receivedMessageHandlers = new ConcurrentHashMap();
    private final Map<Class<?>, MessageHandler<?>> sentMessageHandlerCache = new ConcurrentHashMap();
    private final Map<Class<?>, MessageHandler<?>> sentMessageHandlers = new ConcurrentHashMap();
    private final Map<Class<?>, ExceptionHandler<?>> exceptionHandlerCache = new ConcurrentHashMap();
    private final Map<Class<?>, ExceptionHandler<?>> exceptionHandlers = new ConcurrentHashMap();

    public <E> MessageHandler<? super E> addReceivedMessageHandler(Class<E> type, MessageHandler<? super E> handler) {
        this.receivedMessageHandlerCache.clear();
        return (MessageHandler<? super E>) this.receivedMessageHandlers.put(type, handler);
    }

    public <E> MessageHandler<? super E> removeReceivedMessageHandler(Class<E> type) {
        this.receivedMessageHandlerCache.clear();
        return (MessageHandler<? super E>) this.receivedMessageHandlers.remove(type);
    }

    public <E> MessageHandler<? super E> addSentMessageHandler(Class<E> type, MessageHandler<? super E> handler) {
        this.sentMessageHandlerCache.clear();
        return (MessageHandler<? super E>) this.sentMessageHandlers.put(type, handler);
    }

    public <E> MessageHandler<? super E> removeSentMessageHandler(Class<E> type) {
        this.sentMessageHandlerCache.clear();
        return (MessageHandler<? super E>) this.sentMessageHandlers.remove(type);
    }

    public <E extends Throwable> ExceptionHandler<? super E> addExceptionHandler(Class<E> type, ExceptionHandler<? super E> handler) {
        this.exceptionHandlerCache.clear();
        return (ExceptionHandler<? super E>) this.exceptionHandlers.put(type, handler);
    }

    public <E extends Throwable> ExceptionHandler<? super E> removeExceptionHandler(Class<E> type) {
        this.exceptionHandlerCache.clear();
        return (ExceptionHandler<? super E>) this.exceptionHandlers.remove(type);
    }

    public <E> MessageHandler<? super E> getMessageHandler(Class<E> type) {
        return (MessageHandler<? super E>) this.receivedMessageHandlers.get(type);
    }

    public Map<Class<?>, MessageHandler<?>> getReceivedMessageHandlerMap() {
        return Collections.unmodifiableMap(this.receivedMessageHandlers);
    }

    public Map<Class<?>, MessageHandler<?>> getSentMessageHandlerMap() {
        return Collections.unmodifiableMap(this.sentMessageHandlers);
    }

    public Map<Class<?>, ExceptionHandler<?>> getExceptionHandlerMap() {
        return Collections.unmodifiableMap(this.exceptionHandlers);
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
        MessageHandler<Object> handler = findReceivedMessageHandler(message.getClass());
        if (handler != null) {
            handler.handleMessage(session, message);
            return;
        }
        throw new UnknownMessageTypeException("No message handler found for message type: " + message.getClass().getSimpleName());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageSent(IoSession session, Object message) throws Exception {
        MessageHandler<Object> handler = findSentMessageHandler(message.getClass());
        if (handler != null) {
            handler.handleMessage(session, message);
            return;
        }
        throw new UnknownMessageTypeException("No handler found for message type: " + message.getClass().getSimpleName());
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        ExceptionHandler<Throwable> handler = findExceptionHandler(cause.getClass());
        if (handler != null) {
            handler.exceptionCaught(session, cause);
            return;
        }
        throw new UnknownMessageTypeException("No handler found for exception type: " + cause.getClass().getSimpleName());
    }

    protected MessageHandler<Object> findReceivedMessageHandler(Class<?> type) {
        return findReceivedMessageHandler(type, null);
    }

    protected MessageHandler<Object> findSentMessageHandler(Class<?> type) {
        return findSentMessageHandler(type, null);
    }

    protected ExceptionHandler<Throwable> findExceptionHandler(Class<? extends Throwable> type) {
        return findExceptionHandler(type, null);
    }

    private MessageHandler<Object> findReceivedMessageHandler(Class<?> type, Set<Class<?>> triedClasses) {
        return (MessageHandler) findHandler(this.receivedMessageHandlers, this.receivedMessageHandlerCache, type, triedClasses);
    }

    private MessageHandler<Object> findSentMessageHandler(Class<?> type, Set<Class<?>> triedClasses) {
        return (MessageHandler) findHandler(this.sentMessageHandlers, this.sentMessageHandlerCache, type, triedClasses);
    }

    private ExceptionHandler<Throwable> findExceptionHandler(Class<?> type, Set<Class<?>> triedClasses) {
        return (ExceptionHandler) findHandler(this.exceptionHandlers, this.exceptionHandlerCache, type, triedClasses);
    }

    private Object findHandler(Map<Class<?>, ?> handlers, Map handlerCache, Class<?> type, Set<Class<?>> triedClasses) {
        Class<?> superclass;
        if (triedClasses == null || !triedClasses.contains(type)) {
            Object handler = handlerCache.get(type);
            if (handler == null) {
                Object handler2 = handlers.get(type);
                if (handler2 == null) {
                    if (triedClasses == null) {
                        triedClasses = new IdentityHashSet<>();
                    }
                    triedClasses.add(type);
                    Class<?>[] interfaces = type.getInterfaces();
                    for (Class<?> element : interfaces) {
                        handler2 = findHandler(handlers, handlerCache, element, triedClasses);
                        if (handler2 != null) {
                            break;
                        }
                    }
                }
                if (handler2 == null && (superclass = type.getSuperclass()) != null) {
                    handler2 = findHandler(handlers, handlerCache, superclass, null);
                }
                if (handler2 != null) {
                    handlerCache.put(type, handler2);
                    return handler2;
                }
                return handler2;
            }
            return handler;
        }
        return null;
    }
}
