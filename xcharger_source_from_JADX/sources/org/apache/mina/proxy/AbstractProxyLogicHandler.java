package org.apache.mina.proxy;

import java.util.LinkedList;
import java.util.Queue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.filter.ProxyFilter;
import org.apache.mina.proxy.filter.ProxyHandshakeIoBuffer;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProxyLogicHandler implements ProxyLogicHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) AbstractProxyLogicHandler.class);
    private boolean handshakeComplete = false;
    private ProxyIoSession proxyIoSession;
    private Queue<Event> writeRequestQueue = null;

    public AbstractProxyLogicHandler(ProxyIoSession proxyIoSession2) {
        this.proxyIoSession = proxyIoSession2;
    }

    /* access modifiers changed from: protected */
    public ProxyFilter getProxyFilter() {
        return this.proxyIoSession.getProxyFilter();
    }

    /* access modifiers changed from: protected */
    public IoSession getSession() {
        return this.proxyIoSession.getSession();
    }

    public ProxyIoSession getProxyIoSession() {
        return this.proxyIoSession;
    }

    /* access modifiers changed from: protected */
    public WriteFuture writeData(IoFilter.NextFilter nextFilter, IoBuffer data) {
        ProxyHandshakeIoBuffer writeBuffer = new ProxyHandshakeIoBuffer(data);
        LOGGER.debug("   session write: {}", (Object) writeBuffer);
        WriteFuture writeFuture = new DefaultWriteFuture(getSession());
        getProxyFilter().writeData(nextFilter, getSession(), new DefaultWriteRequest(writeBuffer, writeFuture), true);
        return writeFuture;
    }

    public boolean isHandshakeComplete() {
        boolean z;
        synchronized (this) {
            z = this.handshakeComplete;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public final void setHandshakeComplete() {
        synchronized (this) {
            this.handshakeComplete = true;
        }
        ProxyIoSession proxyIoSession2 = getProxyIoSession();
        proxyIoSession2.getConnector().fireConnected(proxyIoSession2.getSession()).awaitUninterruptibly();
        LOGGER.debug("  handshake completed");
        try {
            proxyIoSession2.getEventQueue().flushPendingSessionEvents();
            flushPendingWriteRequests();
        } catch (Exception ex) {
            LOGGER.error("Unable to flush pending write requests", (Throwable) ex);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r5.writeRequestQueue = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void flushPendingWriteRequests() throws java.lang.Exception {
        /*
            r5 = this;
            monitor-enter(r5)
            org.slf4j.Logger r1 = LOGGER     // Catch:{ all -> 0x0039 }
            java.lang.String r2 = " flushPendingWriteRequests()"
            r1.debug(r2)     // Catch:{ all -> 0x0039 }
            java.util.Queue<org.apache.mina.proxy.AbstractProxyLogicHandler$Event> r1 = r5.writeRequestQueue     // Catch:{ all -> 0x0039 }
            if (r1 != 0) goto L_0x000e
        L_0x000c:
            monitor-exit(r5)
            return
        L_0x000e:
            java.util.Queue<org.apache.mina.proxy.AbstractProxyLogicHandler$Event> r1 = r5.writeRequestQueue     // Catch:{ all -> 0x0039 }
            java.lang.Object r0 = r1.poll()     // Catch:{ all -> 0x0039 }
            org.apache.mina.proxy.AbstractProxyLogicHandler$Event r0 = (org.apache.mina.proxy.AbstractProxyLogicHandler.Event) r0     // Catch:{ all -> 0x0039 }
            if (r0 == 0) goto L_0x003c
            org.slf4j.Logger r1 = LOGGER     // Catch:{ all -> 0x0039 }
            java.lang.String r2 = " Flushing buffered write request: {}"
            java.lang.Object r3 = r0.data     // Catch:{ all -> 0x0039 }
            r1.debug((java.lang.String) r2, (java.lang.Object) r3)     // Catch:{ all -> 0x0039 }
            org.apache.mina.proxy.filter.ProxyFilter r2 = r5.getProxyFilter()     // Catch:{ all -> 0x0039 }
            org.apache.mina.core.filterchain.IoFilter$NextFilter r3 = r0.nextFilter     // Catch:{ all -> 0x0039 }
            org.apache.mina.core.session.IoSession r4 = r5.getSession()     // Catch:{ all -> 0x0039 }
            java.lang.Object r1 = r0.data     // Catch:{ all -> 0x0039 }
            org.apache.mina.core.write.WriteRequest r1 = (org.apache.mina.core.write.WriteRequest) r1     // Catch:{ all -> 0x0039 }
            r2.filterWrite(r3, r4, r1)     // Catch:{ all -> 0x0039 }
            goto L_0x000e
        L_0x0039:
            r1 = move-exception
            monitor-exit(r5)
            throw r1
        L_0x003c:
            r1 = 0
            r5.writeRequestQueue = r1     // Catch:{ all -> 0x0039 }
            goto L_0x000c
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.proxy.AbstractProxyLogicHandler.flushPendingWriteRequests():void");
    }

    public synchronized void enqueueWriteRequest(IoFilter.NextFilter nextFilter, WriteRequest writeRequest) {
        if (this.writeRequestQueue == null) {
            this.writeRequestQueue = new LinkedList();
        }
        this.writeRequestQueue.offer(new Event(nextFilter, writeRequest));
    }

    /* access modifiers changed from: protected */
    public void closeSession(String message, Throwable t) {
        if (t != null) {
            LOGGER.error(message, t);
            this.proxyIoSession.setAuthenticationFailed(true);
        } else {
            LOGGER.error(message);
        }
        getSession().closeNow();
    }

    /* access modifiers changed from: protected */
    public void closeSession(String message) {
        closeSession(message, (Throwable) null);
    }

    private static final class Event {
        /* access modifiers changed from: private */
        public final Object data;
        /* access modifiers changed from: private */
        public final IoFilter.NextFilter nextFilter;

        Event(IoFilter.NextFilter nextFilter2, Object data2) {
            this.nextFilter = nextFilter2;
            this.data = data2;
        }
    }
}
