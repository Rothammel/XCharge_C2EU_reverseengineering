package org.apache.mina.proxy.event;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoSessionEvent {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) IoSessionEvent.class);
    private final IoFilter.NextFilter nextFilter;
    private final IoSession session;
    private IdleStatus status;
    private final IoSessionEventType type;

    public IoSessionEvent(IoFilter.NextFilter nextFilter2, IoSession session2, IoSessionEventType type2) {
        this.nextFilter = nextFilter2;
        this.session = session2;
        this.type = type2;
    }

    public IoSessionEvent(IoFilter.NextFilter nextFilter2, IoSession session2, IdleStatus status2) {
        this(nextFilter2, session2, IoSessionEventType.IDLE);
        this.status = status2;
    }

    public void deliverEvent() {
        logger.debug("Delivering event {}", (Object) this);
        deliverEvent(this.nextFilter, this.session, this.type, this.status);
    }

    private static void deliverEvent(IoFilter.NextFilter nextFilter2, IoSession session2, IoSessionEventType type2, IdleStatus status2) {
        switch (type2) {
            case CREATED:
                nextFilter2.sessionCreated(session2);
                return;
            case OPENED:
                nextFilter2.sessionOpened(session2);
                return;
            case IDLE:
                nextFilter2.sessionIdle(session2, status2);
                return;
            case CLOSED:
                nextFilter2.sessionClosed(session2);
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
