package org.apache.mina.proxy.event;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class IoSessionEvent {
    private static final Logger logger = LoggerFactory.getLogger(IoSessionEvent.class);
    private final IoFilter.NextFilter nextFilter;
    private final IoSession session;
    private IdleStatus status;
    private final IoSessionEventType type;

    public IoSessionEvent(IoFilter.NextFilter nextFilter, IoSession session, IoSessionEventType type) {
        this.nextFilter = nextFilter;
        this.session = session;
        this.type = type;
    }

    public IoSessionEvent(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) {
        this(nextFilter, session, IoSessionEventType.IDLE);
        this.status = status;
    }

    public void deliverEvent() {
        logger.debug("Delivering event {}", this);
        deliverEvent(this.nextFilter, this.session, this.type, this.status);
    }

    private static void deliverEvent(IoFilter.NextFilter nextFilter, IoSession session, IoSessionEventType type, IdleStatus status) {
        switch (type) {
            case CREATED:
                nextFilter.sessionCreated(session);
                return;
            case OPENED:
                nextFilter.sessionOpened(session);
                return;
            case IDLE:
                nextFilter.sessionIdle(session, status);
                return;
            case CLOSED:
                nextFilter.sessionClosed(session);
                return;
            default:
                return;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(IoSessionEvent.class.getSimpleName());
        sb.append('@');
        sb.append(Integer.toHexString(hashCode()));
        sb.append(" - [ ").append(this.session);
        sb.append(", ").append(this.type);
        sb.append(']');
        return sb.toString();
    }

    public IdleStatus getStatus() {
        return this.status;
    }

    public IoFilter.NextFilter getNextFilter() {
        return this.nextFilter;
    }

    public IoSession getSession() {
        return this.session;
    }

    public IoSessionEventType getType() {
        return this.type;
    }
}
