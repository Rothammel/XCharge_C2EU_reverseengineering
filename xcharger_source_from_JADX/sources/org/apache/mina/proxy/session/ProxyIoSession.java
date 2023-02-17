package org.apache.mina.proxy.session;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.ProxyConnector;
import org.apache.mina.proxy.ProxyLogicHandler;
import org.apache.mina.proxy.event.IoSessionEventQueue;
import org.apache.mina.proxy.filter.ProxyFilter;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpAuthenticationMethods;

public class ProxyIoSession {
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    public static final String PROXY_SESSION = (ProxyConnector.class.getName() + ".ProxySession");
    private boolean authenticationFailed;
    private String charsetName;
    private ProxyConnector connector;
    private IoSessionEventQueue eventQueue = new IoSessionEventQueue(this);
    private ProxyLogicHandler handler;
    private List<HttpAuthenticationMethods> preferedOrder;
    private InetSocketAddress proxyAddress = null;
    private ProxyFilter proxyFilter;
    private boolean reconnectionNeeded = false;
    private ProxyRequest request;
    private IoSession session;

    public ProxyIoSession(InetSocketAddress proxyAddress2, ProxyRequest request2) {
        setProxyAddress(proxyAddress2);
        setRequest(request2);
    }

    public IoSessionEventQueue getEventQueue() {
        return this.eventQueue;
    }

    public List<HttpAuthenticationMethods> getPreferedOrder() {
        return this.preferedOrder;
    }

    public void setPreferedOrder(List<HttpAuthenticationMethods> preferedOrder2) {
        this.preferedOrder = preferedOrder2;
    }

    public ProxyLogicHandler getHandler() {
        return this.handler;
    }

    public void setHandler(ProxyLogicHandler handler2) {
        this.handler = handler2;
    }

    public ProxyFilter getProxyFilter() {
        return this.proxyFilter;
    }

    public void setProxyFilter(ProxyFilter proxyFilter2) {
        this.proxyFilter = proxyFilter2;
    }

    public ProxyRequest getRequest() {
        return this.request;
    }

    private void setRequest(ProxyRequest request2) {
        if (request2 == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        this.request = request2;
    }

    public IoSession getSession() {
        return this.session;
    }

    public void setSession(IoSession session2) {
        this.session = session2;
    }

    public ProxyConnector getConnector() {
        return this.connector;
    }

    public void setConnector(ProxyConnector connector2) {
        this.connector = connector2;
    }

    public InetSocketAddress getProxyAddress() {
        return this.proxyAddress;
    }

    private void setProxyAddress(InetSocketAddress proxyAddress2) {
        if (proxyAddress2 == null) {
            throw new IllegalArgumentException("proxyAddress object cannot be null");
        }
        this.proxyAddress = proxyAddress2;
    }

    public boolean isReconnectionNeeded() {
        return this.reconnectionNeeded;
    }

    public void setReconnectionNeeded(boolean reconnectionNeeded2) {
        this.reconnectionNeeded = reconnectionNeeded2;
    }

    public Charset getCharset() {
        return Charset.forName(getCharsetName());
    }

    public String getCharsetName() {
        if (this.charsetName == null) {
            this.charsetName = "ISO-8859-1";
        }
        return this.charsetName;
    }

    public void setCharsetName(String charsetName2) {
        this.charsetName = charsetName2;
    }

    public boolean isAuthenticationFailed() {
        return this.authenticationFailed;
    }

    public void setAuthenticationFailed(boolean authenticationFailed2) {
        this.authenticationFailed = authenticationFailed2;
    }
}
