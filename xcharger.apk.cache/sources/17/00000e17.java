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

/* loaded from: classes.dex */
public abstract class AbstractProxyLogicHandler implements ProxyLogicHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProxyLogicHandler.class);
    private ProxyIoSession proxyIoSession;
    private Queue<Event> writeRequestQueue = null;
    private boolean handshakeComplete = false;

    public AbstractProxyLogicHandler(ProxyIoSession proxyIoSession) {
        this.proxyIoSession = proxyIoSession;
    }

    protected ProxyFilter getProxyFilter() {
        return this.proxyIoSession.getProxyFilter();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public IoSession getSession() {
        return this.proxyIoSession.getSession();
    }

    @Override // org.apache.mina.proxy.ProxyLogicHandler
    public ProxyIoSession getProxyIoSession() {
        return this.proxyIoSession;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public WriteFuture writeData(IoFilter.NextFilter nextFilter, IoBuffer data) {
        ProxyHandshakeIoBuffer writeBuffer = new ProxyHandshakeIoBuffer(data);
        LOGGER.debug("   session write: {}", writeBuffer);
        WriteFuture writeFuture = new DefaultWriteFuture(getSession());
        getProxyFilter().writeData(nextFilter, getSession(), new DefaultWriteRequest(writeBuffer, writeFuture), true);
        return writeFuture;
    }

    @Override // org.apache.mina.proxy.ProxyLogicHandler
    public boolean isHandshakeComplete() {
        boolean z;
        synchronized (this) {
            z = this.handshakeComplete;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void setHandshakeComplete() {
        synchronized (this) {
            this.handshakeComplete = true;
        }
        ProxyIoSession proxyIoSession = getProxyIoSession();
        proxyIoSession.getConnector().fireConnected(proxyIoSession.getSession()).awaitUninterruptibly();
        LOGGER.debug("  handshake completed");
        try {
            proxyIoSession.getEventQueue().flushPendingSessionEvents();
            flushPendingWriteRequests();
        } catch (Exception ex) {
            LOGGER.error("Unable to flush pending write requests", (Throwable) ex);
        }
    }

    protected synchronized void flushPendingWriteRequests() throws Exception {
        LOGGER.debug(" flushPendingWriteRequests()");
        if (this.writeRequestQueue != null) {
            while (true) {
                Event scheduledWrite = this.writeRequestQueue.poll();
                if (scheduledWrite == null) {
                    break;
                }
                LOGGER.debug(" Flushing buffered write request: {}", scheduledWrite.data);
                getProxyFilter().filterWrite(scheduledWrite.nextFilter, getSession(), (WriteRequest) scheduledWrite.data);
            }
            this.writeRequestQueue = null;
        }
    }

    @Override // org.apache.mina.proxy.ProxyLogicHandler
    public synchronized void enqueueWriteRequest(IoFilter.NextFilter nextFilter, WriteRequest writeRequest) {
        if (this.writeRequestQueue == null) {
            this.writeRequestQueue = new LinkedList();
        }
        this.writeRequestQueue.offer(new Event(nextFilter, writeRequest));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void closeSession(String message, Throwable t) {
        if (t != null) {
            LOGGER.error(message, t);
            this.proxyIoSession.setAuthenticationFailed(true);
        } else {
            LOGGER.error(message);
        }
        getSession().closeNow();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void closeSession(String message) {
        closeSession(message, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Event {
        private final Object data;
        private final IoFilter.NextFilter nextFilter;

        Event(IoFilter.NextFilter nextFilter, Object data) {
            this.nextFilter = nextFilter;
            this.data = data;
        }
    }
}