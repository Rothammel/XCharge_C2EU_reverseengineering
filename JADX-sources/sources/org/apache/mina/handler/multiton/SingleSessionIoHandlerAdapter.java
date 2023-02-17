package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
/* loaded from: classes.dex */
public class SingleSessionIoHandlerAdapter implements SingleSessionIoHandler {
    private final IoSession session;

    public SingleSessionIoHandlerAdapter(IoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session");
        }
        this.session = session;
    }

    protected IoSession getSession() {
        return this.session;
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void exceptionCaught(Throwable th) throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void inputClosed(IoSession session) {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void messageReceived(Object message) throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void messageSent(Object message) throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void sessionClosed() throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void sessionCreated() throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void sessionIdle(IdleStatus status) throws Exception {
    }

    @Override // org.apache.mina.handler.multiton.SingleSessionIoHandler
    public void sessionOpened() throws Exception {
    }
}
