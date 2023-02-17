package org.apache.mina.core.session;

import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public class IoEvent implements Runnable {
    private final Object parameter;
    private final IoSession session;
    private final IoEventType type;

    public IoEvent(IoEventType type, IoSession session, Object parameter) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
        if (session == null) {
            throw new IllegalArgumentException("session");
        }
        this.type = type;
        this.session = session;
        this.parameter = parameter;
    }

    public IoEventType getType() {
        return this.type;
    }

    public IoSession getSession() {
        return this.session;
    }

    public Object getParameter() {
        return this.parameter;
    }

    @Override // java.lang.Runnable
    public void run() {
        fire();
    }

    public void fire() {
        switch (getType()) {
            case MESSAGE_RECEIVED:
                getSession().getFilterChain().fireMessageReceived(getParameter());
                return;
            case MESSAGE_SENT:
                getSession().getFilterChain().fireMessageSent((WriteRequest) getParameter());
                return;
            case WRITE:
                getSession().getFilterChain().fireFilterWrite((WriteRequest) getParameter());
                return;
            case CLOSE:
                getSession().getFilterChain().fireFilterClose();
                return;
            case EXCEPTION_CAUGHT:
                getSession().getFilterChain().fireExceptionCaught((Throwable) getParameter());
                return;
            case SESSION_IDLE:
                getSession().getFilterChain().fireSessionIdle((IdleStatus) getParameter());
                return;
            case SESSION_OPENED:
                getSession().getFilterChain().fireSessionOpened();
                return;
            case SESSION_CREATED:
                getSession().getFilterChain().fireSessionCreated();
                return;
            case SESSION_CLOSED:
                getSession().getFilterChain().fireSessionClosed();
                return;
            default:
                throw new IllegalArgumentException("Unknown event type: " + getType());
        }
    }

    public String toString() {
        return getParameter() == null ? "[" + getSession() + "] " + getType().name() : "[" + getSession() + "] " + getType().name() + ": " + getParameter();
    }
}