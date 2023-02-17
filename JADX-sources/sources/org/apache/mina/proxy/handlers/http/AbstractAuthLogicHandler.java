package org.apache.mina.proxy.handlers.http;

import java.util.List;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public abstract class AbstractAuthLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthLogicHandler.class);
    protected ProxyIoSession proxyIoSession;
    protected ProxyRequest request;
    protected int step = 0;

    public abstract void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException;

    public abstract void handleResponse(HttpProxyResponse httpProxyResponse) throws ProxyAuthException;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        this.proxyIoSession = proxyIoSession;
        this.request = proxyIoSession.getRequest();
        if (this.request == null || !(this.request instanceof HttpProxyRequest)) {
            throw new IllegalArgumentException("request parameter should be a non null HttpProxyRequest instance");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void writeRequest(IoFilter.NextFilter nextFilter, HttpProxyRequest request) throws ProxyAuthException {
        logger.debug("  sending HTTP request");
        ((AbstractHttpLogicHandler) this.proxyIoSession.getHandler()).writeRequest(nextFilter, request);
    }

    public static void addKeepAliveHeaders(Map<String, List<String>> headers) {
        StringUtilities.addValueToHeader(headers, "Keep-Alive", HttpProxyConstants.DEFAULT_KEEP_ALIVE_TIME, true);
        StringUtilities.addValueToHeader(headers, "Proxy-Connection", "keep-Alive", true);
    }
}
