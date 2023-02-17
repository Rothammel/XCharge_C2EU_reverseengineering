package org.apache.mina.handler.multiton;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
public class SingleSessionIoHandlerDelegate implements IoHandler {
    public static final AttributeKey HANDLER = new AttributeKey(SingleSessionIoHandlerDelegate.class, "handler");
    private final SingleSessionIoHandlerFactory factory;

    public SingleSessionIoHandlerDelegate(SingleSessionIoHandlerFactory factory2) {
        if (factory2 == null) {
            throw new IllegalArgumentException("factory");
        }
        this.factory = factory2;
    }

    public SingleSessionIoHandlerFactory getFactory() {
        return this.factory;
    }

    public void sessionCreated(IoSession session) throws Exception {
        SingleSessionIoHandler handler = this.factory.getHandler(session);
        session.setAttribute(HANDLER, handler);
        handler.sessionCreated();
    }

    public void sessionOpened(IoSession session) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).sessionOpened();
    }

    public void sessionClosed(IoSession session) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).sessionClosed();
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).sessionIdle(status);
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).exceptionCaught(cause);
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).messageReceived(message);
    }

    public void messageSent(IoSession session, Object message) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).messageSent(message);
    }

    public void inputClosed(IoSession session) throws Exception {
        ((SingleSessionIoHandler) session.getAttribute(HANDLER)).inputClosed(session);
    }
}
