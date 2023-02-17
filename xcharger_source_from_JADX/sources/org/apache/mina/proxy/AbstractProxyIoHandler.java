package org.apache.mina.proxy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProxyIoHandler extends IoHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) AbstractProxyIoHandler.class);

    public abstract void proxySessionOpened(IoSession ioSession) throws Exception;

    public final void sessionOpened(IoSession session) throws Exception {
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        if ((proxyIoSession.getRequest() instanceof SocksProxyRequest) || proxyIoSession.isAuthenticationFailed() || proxyIoSession.getHandler().isHandshakeComplete()) {
            proxySessionOpened(session);
        } else {
            logger.debug("Filtered session opened event !");
        }
    }
}