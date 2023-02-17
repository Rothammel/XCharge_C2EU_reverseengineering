package org.apache.mina.handler.multiton;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
/* loaded from: classes.dex */
public class SingleSessionIoHandlerDelegate implements IoHandler {
    public static final AttributeKey HANDLER = new AttributeKey(SingleSessionIoHandlerDelegate.class, "handler");
    private final SingleSessionIoHandlerFactory factory;

    public SingleSessionIoHandlerDelegate(SingleSessionIoHandlerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        this.factory = factory;
    }

    public SingleSessionIoHandlerFactory getFactory() {
        return this.factory;
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionCreated(IoSession session) throws Exception {
        SingleSessionIoHandler handler = this.factory.getHandler(session);
        session.setAttribute(HANDLER, handler);
        handler.sessionCreated();
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionOpened(IoSession session) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.sessionOpened();
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionClosed(IoSession session) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.sessionClosed();
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.sessionIdle(status);
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.exceptionCaught(cause);
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.messageReceived(message);
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void messageSent(IoSession session, Object message) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.messageSent(message);
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void inputClosed(IoSession session) throws Exception {
        SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
        handler.inputClosed(session);
    }
}