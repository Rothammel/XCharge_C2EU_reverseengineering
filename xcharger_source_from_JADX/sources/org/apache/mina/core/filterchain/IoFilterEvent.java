package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoFilterEvent extends IoEvent {
    private static final boolean DEBUG = LOGGER.isDebugEnabled();
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) IoFilterEvent.class);
    private final IoFilter.NextFilter nextFilter;

    public IoFilterEvent(IoFilter.NextFilter nextFilter2, IoEventType type, IoSession session, Object parameter) {
        super(type, session, parameter);
        if (nextFilter2 == null) {
            throw new IllegalArgumentException("nextFilter must not be null");
        }
        this.nextFilter = nextFilter2;
    }

    public IoFilter.NextFilter getNextFilter() {
        return this.nextFilter;
    }

    public void fire() {
        IoSession session = getSession();
        IoFilter.NextFilter nextFilter2 = getNextFilter();
        IoEventType type = getType();
        if (DEBUG) {
            LOGGER.debug("Firing a {} event for session {}", (Object) type, (Object) Long.valueOf(session.getId()));
        }
        switch (type) {
            case MESSAGE_RECEIVED:
                nextFilter2.messageReceived(session, getParameter());
                break;
            case MESSAGE_SENT:
                nextFilter2.messageSent(session, (WriteRequest) getParameter());
                break;
            case WRITE:
                nextFilter2.filterWrite(session, (WriteRequest) getParameter());
                break;
            case CLOSE:
                nextFilter2.filterClose(session);
                break;
            case EXCEPTION_CAUGHT:
                nextFilter2.exceptionCaught(session, (Throwable) getParameter());
                break;
            case SESSION_IDLE:
                nextFilter2.sessionIdle(session, (IdleStatus) getParameter());
                break;
            case SESSION_OPENED:
                nextFilter2.sessionOpened(session);
                break;
            case SESSION_CREATED:
                nextFilter2.sessionCreated(session);
                break;
            case SESSION_CLOSED:
                nextFilter2.sessionClosed(session);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + type);
        }
        if (DEBUG) {
            LOGGER.debug("Event {} has been fired for session {}", (Object) type, (Object) Long.valueOf(session.getId()));
        }
    }
}
