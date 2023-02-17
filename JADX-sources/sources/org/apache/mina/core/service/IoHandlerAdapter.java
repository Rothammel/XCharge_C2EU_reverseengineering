package org.apache.mina.core.service;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class IoHandlerAdapter implements IoHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoHandlerAdapter.class);

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionOpened(IoSession session) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionClosed(IoSession session) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("EXCEPTION, please implement " + getClass().getName() + ".exceptionCaught() for proper handling:", cause);
        }
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override // org.apache.mina.core.service.IoHandler
    public void inputClosed(IoSession session) throws Exception {
        session.closeNow();
    }
}
