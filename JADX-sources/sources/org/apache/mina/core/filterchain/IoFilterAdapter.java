package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public class IoFilterAdapter implements IoFilter {
    @Override // org.apache.mina.core.filterchain.IoFilter
    public void init() throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void destroy() throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void onPostAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void onPreRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void onPostRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.sessionCreated(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.sessionOpened(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.sessionClosed(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        nextFilter.sessionIdle(session, status);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        nextFilter.exceptionCaught(session, cause);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        nextFilter.messageReceived(session, message);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        nextFilter.messageSent(session, writeRequest);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        nextFilter.filterWrite(session, writeRequest);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.filterClose(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilter
    public void inputClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.inputClosed(session);
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
