package org.apache.mina.proxy.handlers.http.basic;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class HttpNoAuthLogicHandler extends AbstractAuthLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpNoAuthLogicHandler.class);

    public HttpNoAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        super(proxyIoSession);
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        logger.debug(" doHandshake()");
        writeRequest(nextFilter, (HttpProxyRequest) this.request);
        this.step++;
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
    public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
        throw new ProxyAuthException("Received error response code (" + response.getStatusLine() + ").");
    }
}