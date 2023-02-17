package org.apache.mina.proxy.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.ProxyAuthException;
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

/* loaded from: classes.dex */
public class ProxyFilter extends IoFilterAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyFilter.class);

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPreAdd(IoFilterChain chain, String name, IoFilter.NextFilter nextFilter) {
        if (chain.contains(ProxyFilter.class)) {
            throw new IllegalStateException("A filter chain cannot contain more than one ProxyFilter.");
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPreRemove(IoFilterChain chain, String name, IoFilter.NextFilter nextFilter) {
        IoSession session = chain.getSession();
        session.removeAttribute(ProxyIoSession.PROXY_SESSION);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        proxyIoSession.setAuthenticationFailed(true);
        super.exceptionCaught(nextFilter, session, cause);
    }

    private ProxyLogicHandler getProxyHandler(IoSession session) {
        ProxyLogicHandler handler = ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getHandler();
        if (handler == null) {
            throw new IllegalStateException();
        }
        if (handler.getProxyIoSession().getProxyFilter() != this) {
            throw new IllegalArgumentException("Not managed by this filter.");
        }
        return handler;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws ProxyAuthException {
        ProxyLogicHandler handler = getProxyHandler(session);
        synchronized (handler) {
            IoBuffer buf = (IoBuffer) message;
            if (handler.isHandshakeComplete()) {
                nextFilter.messageReceived(session, buf);
            } else {
                LOGGER.debug(" Data Read: {} ({})", handler, buf);
                while (buf.hasRemaining() && !handler.isHandshakeComplete()) {
                    LOGGER.debug(" Pre-handshake - passing to handler");
                    int pos = buf.position();
                    handler.messageReceived(nextFilter, buf);
                    if (buf.position() != pos || session.isClosing()) {
                        return;
                    }
                    while (buf.hasRemaining()) {
                        LOGGER.debug(" Pre-handshake - passing to handler");
                        int pos2 = buf.position();
                        handler.messageReceived(nextFilter, buf);
                        if (buf.position() != pos2) {
                        }
                        return;
                    }
                }
                if (buf.hasRemaining()) {
                    LOGGER.debug(" Passing remaining data to next filter");
                    nextFilter.messageReceived(session, buf);
                }
            }
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
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

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (writeRequest.getMessage() == null || !(writeRequest.getMessage() instanceof ProxyHandshakeIoBuffer)) {
            nextFilter.messageSent(session, writeRequest);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ProxyLogicHandler handler;
        LOGGER.debug("Session created: " + session);
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        LOGGER.debug("  get proxyIoSession: " + proxyIoSession);
        proxyIoSession.setProxyFilter(this);
        ProxyLogicHandler handler2 = proxyIoSession.getHandler();
        if (handler2 == null) {
            ProxyRequest request = proxyIoSession.getRequest();
            if (request instanceof SocksProxyRequest) {
                SocksProxyRequest req = (SocksProxyRequest) request;
                if (req.getProtocolVersion() == 4) {
                    handler = new Socks4LogicHandler(proxyIoSession);
                } else {
                    handler = new Socks5LogicHandler(proxyIoSession);
                }
            } else {
                handler = new HttpSmartProxyHandler(proxyIoSession);
            }
            proxyIoSession.setHandler(handler);
            handler.doHandshake(nextFilter);
        }
        proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CREATED));
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.OPENED));
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, status));
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CLOSED));
    }
}
