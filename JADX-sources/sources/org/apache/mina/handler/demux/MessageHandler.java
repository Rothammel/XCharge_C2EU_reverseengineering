package org.apache.mina.handler.demux;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface MessageHandler<E> {
    public static final MessageHandler<Object> NOOP = new MessageHandler<Object>() { // from class: org.apache.mina.handler.demux.MessageHandler.1
        @Override // org.apache.mina.handler.demux.MessageHandler
        public void handleMessage(IoSession session, Object message) {
        }
    };

    void handleMessage(IoSession ioSession, E e) throws Exception;
}
