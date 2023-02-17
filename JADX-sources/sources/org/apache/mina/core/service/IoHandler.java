package org.apache.mina.core.service;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface IoHandler {
    void exceptionCaught(IoSession ioSession, Throwable th) throws Exception;

    void inputClosed(IoSession ioSession) throws Exception;

    void messageReceived(IoSession ioSession, Object obj) throws Exception;

    void messageSent(IoSession ioSession, Object obj) throws Exception;

    void sessionClosed(IoSession ioSession) throws Exception;

    void sessionCreated(IoSession ioSession) throws Exception;

    void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception;

    void sessionOpened(IoSession ioSession) throws Exception;
}
