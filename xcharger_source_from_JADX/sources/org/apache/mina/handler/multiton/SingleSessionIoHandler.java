package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
public interface SingleSessionIoHandler {
    void exceptionCaught(Throwable th) throws Exception;

    void inputClosed(IoSession ioSession);

    void messageReceived(Object obj) throws Exception;

    void messageSent(Object obj) throws Exception;

    void sessionClosed() throws Exception;

    void sessionCreated() throws Exception;

    void sessionIdle(IdleStatus idleStatus) throws Exception;

    void sessionOpened() throws Exception;
}
