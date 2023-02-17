package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public abstract class CommonEventFilter extends IoFilterAdapter {
    /* access modifiers changed from: protected */
    public abstract void filter(IoFilterEvent ioFilterEvent) throws Exception;

    public final void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CREATED, session, (Object) null));
    }

    public final void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_OPENED, session, (Object) null));
    }

    public final void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CLOSED, session, (Object) null));
    }

    public final void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_IDLE, session, status));
    }

    public final void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.EXCEPTION_CAUGHT, session, cause));
    }

    public final void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, session, message));
    }

    public final void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_SENT, session, writeRequest));
    }

    public final void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.WRITE, session, writeRequest));
    }

    public final void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        filter(new IoFilterEvent(nextFilter, IoEventType.CLOSE, session, (Object) null));
    }
}
