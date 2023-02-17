package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
public class SingleSessionIoHandlerAdapter implements SingleSessionIoHandler {
    private final IoSession session;

    public SingleSessionIoHandlerAdapter(IoSession session2) {
        if (session2 == null) {
            throw new IllegalArgumentException("session");
        }
        this.session = session2;
    }

    /* access modifiers changed from: protected */
    public IoSession getSession() {
        return this.session;
    }

    public void exceptionCaught(Throwable th) throws Exception {
    }

    public void inputClosed(IoSession session2) {
    }

    public void messageReceived(Object message) throws Exception {
    }

    public void messageSent(Object message) throws Exception {
    }

    public void sessionClosed() throws Exception {
    }

    public void sessionCreated() throws Exception {
    }

    public void sessionIdle(IdleStatus status) throws Exception {
    }

    public void sessionOpened() throws Exception {
    }
}
