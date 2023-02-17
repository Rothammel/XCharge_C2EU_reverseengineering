package org.apache.mina.proxy.filter;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.ProxyLogicHandler;
import org.apache.mina.proxy.event.IoSessionEvent;
import org.apache.mina.proxy.event.IoSessionEventType;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpSmartProxyHandler;
import org.apache.mina.proxy.handlers.socks.Socks4LogicHandler;
import org.apache.mina.proxy.handlers.socks.Socks5LogicHandler;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyFilter extends IoFilterAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) ProxyFilter.class);

    public void onPreAdd(IoFilterChain chain, String name, IoFilter.NextFilter nextFilter) {
        if (chain.contains((Class<? extends IoFilter>) ProxyFilter.class)) {
            throw new IllegalStateException("A filter chain cannot contain more than one ProxyFilter.");
        }
    }

    public void onPreRemove(IoFilterChain chain, String name, IoFilter.NextFilter nextFilter) {
        chain.getSession().removeAttribute(ProxyIoSession.PROXY_SESSION);
    }

    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).setAuthenticationFailed(true);
        super.exceptionCaught(nextFilter, session, cause);
    }

    private ProxyLogicHandler getProxyHandler(IoSession session) {
        ProxyLogicHandler handler = ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getHandler();
        if (handler == null) {
            throw new IllegalStateException();
        } else if (handler.getProxyIoSession().getProxyFilter() == this) {
            return handler;
        } else {
            throw new IllegalArgumentException("Not managed by this filter.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0027  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void messageReceived(org.apache.mina.core.filterchain.IoFilter.NextFilter r7, org.apache.mina.core.session.IoSession r8, java.lang.Object r9) throws org.apache.mina.proxy.ProxyAuthException {
        /*
            r6 = this;
            org.apache.mina.proxy.ProxyLogicHandler r2 = r6.getProxyHandler(r8)
            monitor-enter(r2)
            r0 = r9
            org.apache.mina.core.buffer.IoBuffer r0 = (org.apache.mina.core.buffer.IoBuffer) r0     // Catch:{ all -> 0x0043 }
            r1 = r0
            boolean r4 = r2.isHandshakeComplete()     // Catch:{ all -> 0x0043 }
            if (r4 == 0) goto L_0x0014
            r7.messageReceived(r8, r1)     // Catch:{ all -> 0x0043 }
        L_0x0012:
            monitor-exit(r2)     // Catch:{ all -> 0x0043 }
        L_0x0013:
            return
        L_0x0014:
            org.slf4j.Logger r4 = LOGGER     // Catch:{ all -> 0x0043 }
            java.lang.String r5 = " Data Read: {} ({})"
            r4.debug((java.lang.String) r5, (java.lang.Object) r2, (java.lang.Object) r1)     // Catch:{ all -> 0x0043 }
        L_0x001b:
            boolean r4 = r1.hasRemaining()     // Catch:{ all -> 0x0043 }
            if (r4 == 0) goto L_0x0046
            boolean r4 = r2.isHandshakeComplete()     // Catch:{ all -> 0x0043 }
            if (r4 != 0) goto L_0x0046
            org.slf4j.Logger r4 = LOGGER     // Catch:{ all -> 0x0043 }
            java.lang.String r5 = " Pre-handshake - passing to handler"
            r4.debug(r5)     // Catch:{ all -> 0x0043 }
            int r3 = r1.position()     // Catch:{ all -> 0x0043 }
            r2.messageReceived(r7, r1)     // Catch:{ all -> 0x0043 }
            int r4 = r1.position()     // Catch:{ all -> 0x0043 }
            if (r4 == r3) goto L_0x0041
            boolean r4 = r8.isClosing()     // Catch:{ all -> 0x0043 }
            if (r4 == 0) goto L_0x001b
        L_0x0041:
            monitor-exit(r2)     // Catch:{ all -> 0x0043 }
            goto L_0x0013
        L_0x0043:
            r4 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0043 }
            throw r4
        L_0x0046:
            boolean r4 = r1.hasRemaining()     // Catch:{ all -> 0x0043 }
            if (r4 == 0) goto L_0x0012
            org.slf4j.Logger r4 = LOGGER     // Catch:{ all -> 0x0043 }
            java.lang.String r5 = " Passing remaining data to next filter"
            r4.debug(r5)     // Catch:{ all -> 0x0043 }
            r7.messageReceived(r8, r1)     // Catch:{ all -> 0x0043 }
            goto L_0x0012
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.proxy.filter.ProxyFilter.messageReceived(org.apache.mina.core.filterchain.IoFilter$NextFilter, org.apache.mina.core.session.IoSession, java.lang.Object):void");
    }

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
        writeData(nextFilter, session, writeRequest, false);
    }

    public void writeData(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest, boolean isHandshakeData) {
        ProxyLogicHandler handler = getProxyHandler(session);
        synchronized (handler) {
            if (handler.isHandshakeComplete()) {
                nextFilter.filterWrite(session, writeRequest);
            } else if (isHandshakeData) {
                LOGGER.debug("   handshake data: {}", writeRequest.getMessage());
                nextFilter.filterWrite(session, writeRequest);
            } else if (!session.isConnected()) {
                LOGGER.debug(" Write request on closed session. Request ignored.");
            } else {
                LOGGER.debug(" Handshaking is not complete yet. Buffering write request.");
                handler.enqueueWriteRequest(nextFilter, writeRequest);
            }
        }
    }

    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (writeRequest.getMessage() == null || !(writeRequest.getMessage() instanceof ProxyHandshakeIoBuffer)) {
            nextFilter.messageSent(session, writeRequest);
        }
    }

    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ProxyLogicHandler handler;
        LOGGER.debug("Session created: " + session);
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        LOGGER.debug("  get proxyIoSession: " + proxyIoSession);
        proxyIoSession.setProxyFilter(this);
        if (proxyIoSession.getHandler() == null) {
            ProxyRequest request = proxyIoSession.getRequest();
            if (!(request instanceof SocksProxyRequest)) {
                handler = new HttpSmartProxyHandler(proxyIoSession);
            } else if (((SocksProxyRequest) request).getProtocolVersion() == 4) {
                handler = new Socks4LogicHandler(proxyIoSession);
            } else {
                handler = new Socks5LogicHandler(proxyIoSession);
            }
            proxyIoSession.setHandler(handler);
            handler.doHandshake(nextFilter);
        }
        proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CREATED));
    }

    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.OPENED));
    }

    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, status));
    }

    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CLOSED));
    }
}
