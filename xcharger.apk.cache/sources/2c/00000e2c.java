package org.apache.mina.proxy.handlers.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class HttpSmartProxyHandler extends AbstractHttpLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpSmartProxyHandler.class);
    private AbstractAuthLogicHandler authHandler;
    private boolean requestSent;

    public HttpSmartProxyHandler(ProxyIoSession proxyIoSession) {
        super(proxyIoSession);
        this.requestSent = false;
    }

    @Override // org.apache.mina.proxy.ProxyLogicHandler
    public void doHandshake(IoFilter.NextFilter nextFilter) throws ProxyAuthException {
        logger.debug(" doHandshake()");
        if (this.authHandler != null) {
            this.authHandler.doHandshake(nextFilter);
        } else if (this.requestSent) {
            throw new ProxyAuthException("Authentication request already sent");
        } else {
            logger.debug("  sending HTTP request");
            HttpProxyRequest req = (HttpProxyRequest) getProxyIoSession().getRequest();
            Map<String, List<String>> headers = req.getHeaders() != null ? req.getHeaders() : new HashMap<>();
            AbstractAuthLogicHandler.addKeepAliveHeaders(headers);
            req.setHeaders(headers);
            writeRequest(nextFilter, req);
            this.requestSent = true;
        }
    }

    private void autoSelectAuthHandler(HttpProxyResponse response) throws ProxyAuthException {
        List<String> values = response.getHeaders().get(HttpHeaders.PROXY_AUTHENTICATE);
        ProxyIoSession proxyIoSession = getProxyIoSession();
        if (values == null || values.size() == 0) {
            this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
        } else if (getProxyIoSession().getPreferedOrder() == null) {
            int method = -1;
            Iterator<String> it2 = values.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                String proxyAuthHeader = it2.next().toLowerCase();
                if (proxyAuthHeader.contains("ntlm")) {
                    method = HttpAuthenticationMethods.NTLM.getId();
                    break;
                } else if (proxyAuthHeader.contains("digest") && method != HttpAuthenticationMethods.NTLM.getId()) {
                    method = HttpAuthenticationMethods.DIGEST.getId();
                } else if (proxyAuthHeader.contains("basic") && method == -1) {
                    method = HttpAuthenticationMethods.BASIC.getId();
                }
            }
            if (method != -1) {
                try {
                    this.authHandler = HttpAuthenticationMethods.getNewHandler(method, proxyIoSession);
                } catch (Exception ex) {
                    logger.debug("Following exception occured:", (Throwable) ex);
                }
            }
            if (this.authHandler == null) {
                this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
            }
        } else {
            Iterator<HttpAuthenticationMethods> it3 = proxyIoSession.getPreferedOrder().iterator();
            while (true) {
                if (!it3.hasNext()) {
                    break;
                }
                HttpAuthenticationMethods method2 = it3.next();
                if (this.authHandler != null) {
                    break;
                } else if (method2 == HttpAuthenticationMethods.NO_AUTH) {
                    this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
                    break;
                } else {
                    Iterator<String> it4 = values.iterator();
                    while (true) {
                        if (it4.hasNext()) {
                            String proxyAuthHeader2 = it4.next().toLowerCase();
                            try {
                            } catch (Exception ex2) {
                                logger.debug("Following exception occured:", (Throwable) ex2);
                            }
                            if (proxyAuthHeader2.contains("basic") && method2 == HttpAuthenticationMethods.BASIC) {
                                this.authHandler = HttpAuthenticationMethods.BASIC.getNewHandler(proxyIoSession);
                                break;
                            } else if (proxyAuthHeader2.contains("digest") && method2 == HttpAuthenticationMethods.DIGEST) {
                                this.authHandler = HttpAuthenticationMethods.DIGEST.getNewHandler(proxyIoSession);
                                break;
                            } else if (proxyAuthHeader2.contains("ntlm") && method2 == HttpAuthenticationMethods.NTLM) {
                                this.authHandler = HttpAuthenticationMethods.NTLM.getNewHandler(proxyIoSession);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (this.authHandler == null) {
            throw new ProxyAuthException("Unknown authentication mechanism(s): " + values);
        }
    }

    @Override // org.apache.mina.proxy.handlers.http.AbstractHttpLogicHandler
    public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
        if (!isHandshakeComplete() && ("close".equalsIgnoreCase(StringUtilities.getSingleValuedHeader(response.getHeaders(), "Proxy-Connection")) || "close".equalsIgnoreCase(StringUtilities.getSingleValuedHeader(response.getHeaders(), HttpHeaders.CONNECTION)))) {
            getProxyIoSession().setReconnectionNeeded(true);
        }
        if (response.getStatusCode() == 407) {
            if (this.authHandler == null) {
                autoSelectAuthHandler(response);
            }
            this.authHandler.handleResponse(response);
            return;
        }
        throw new ProxyAuthException("Error: unexpected response code " + response.getStatusLine() + " received from proxy.");
    }
}