package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public class ReferenceCountingFilter extends IoFilterAdapter {
    private int count = 0;
    private final IoFilter filter;

    public ReferenceCountingFilter(IoFilter filter) {
        this.filter = filter;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void init() throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void destroy() throws Exception {
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public synchronized void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        if (this.count == 0) {
            this.filter.init();
        }
        this.count++;
        this.filter.onPreAdd(parent, name, nextFilter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public synchronized void onPostRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        this.filter.onPostRemove(parent, name, nextFilter);
        this.count--;
        if (this.count == 0) {
            this.filter.destroy();
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        this.filter.exceptionCaught(nextFilter, session, cause);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        this.filter.filterClose(nextFilter, session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        this.filter.filterWrite(nextFilter, session, writeRequest);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        this.filter.messageReceived(nextFilter, session, message);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        this.filter.messageSent(nextFilter, session, writeRequest);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPostAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        this.filter.onPostAdd(parent, name, nextFilter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPreRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        this.filter.onPreRemove(parent, name, nextFilter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        this.filter.sessionClosed(nextFilter, session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        this.filter.sessionCreated(nextFilter, session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        this.filter.sessionIdle(nextFilter, session, status);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        this.filter.sessionOpened(nextFilter, session);
    }
}
