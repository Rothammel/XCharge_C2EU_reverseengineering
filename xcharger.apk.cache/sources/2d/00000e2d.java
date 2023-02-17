package org.apache.mina.proxy.handlers.http.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class HttpBasicAuthLogicHandler extends AbstractAuthLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpBasicAuthLogicHandler.class);

    public HttpBasicAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        super(proxyIoSession);
        ((HttpProxyRequest) this.request).checkRequiredProperties(HttpProxyConstants.USER_PROPERTY, HttpProxyConstants.PWD_PROPERTY);
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        logger.debug(" doHandshake()");
        if (this.step > 0) {
            throw new ProxyAuthException("Authentication request already sent");
        }
        HttpProxyRequest req = (HttpProxyRequest) this.request;
        Map<String, List<String>> headers = req.getHeaders() != null ? req.getHeaders() : new HashMap<>();
        String username = req.getProperties().get(HttpProxyConstants.USER_PROPERTY);
        String password = req.getProperties().get(HttpProxyConstants.PWD_PROPERTY);
        StringUtilities.addValueToHeader(headers, HttpHeaders.PROXY_AUTHORIZATION, "Basic " + createAuthorization(username, password), true);
        addKeepAliveHeaders(headers);
        req.setHeaders(headers);
        writeRequest(nextFilter, req);
        this.step++;
    }

    public static String createAuthorization(String username, String password) {
        return new String(Base64.encodeBase64((username + ":" + password).getBytes()));
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler
    public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
        if (response.getStatusCode() != 407) {
            throw new ProxyAuthException("Received error response code (" + response.getStatusLine() + ").");
        }
    }
}