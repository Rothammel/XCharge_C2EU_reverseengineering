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

public class HttpSmartProxyHandler extends AbstractHttpLogicHandler {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) HttpSmartProxyHandler.class);
    private AbstractAuthLogicHandler authHandler;
    private boolean requestSent = false;

    public HttpSmartProxyHandler(ProxyIoSession proxyIoSession) {
        super(proxyIoSession);
    }

    /* JADX WARNING: type inference failed for: r2v7, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
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

    /* JADX WARNING: type inference failed for: r5v2, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    private void autoSelectAuthHandler(HttpProxyResponse response) throws ProxyAuthException {
        List<String> values = response.getHeaders().get(HttpHeaders.PROXY_AUTHENTICATE);
        ProxyIoSession proxyIoSession = getProxyIoSession();
        if (values != null && values.size() != 0) {
            if (getProxyIoSession().getPreferedOrder() != null) {
                Iterator<HttpAuthenticationMethods> it = proxyIoSession.getPreferedOrder().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    HttpAuthenticationMethods method = it.next();
                    if (this.authHandler == null) {
                        if (method != HttpAuthenticationMethods.NO_AUTH) {
                            Iterator<String> it2 = values.iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                String proxyAuthHeader = it2.next().toLowerCase();
                                try {
                                    if (!proxyAuthHeader.contains("basic") || method != HttpAuthenticationMethods.BASIC) {
                                        if (!proxyAuthHeader.contains("digest") || method != HttpAuthenticationMethods.DIGEST) {
                                            if (proxyAuthHeader.contains("ntlm") && method == HttpAuthenticationMethods.NTLM) {
                                                this.authHandler = HttpAuthenticationMethods.NTLM.getNewHandler(proxyIoSession);
                                                break;
                                            }
                                        } else {
                                            this.authHandler = HttpAuthenticationMethods.DIGEST.getNewHandler(proxyIoSession);
                                            break;
                                        }
                                    } else {
                                        this.authHandler = HttpAuthenticationMethods.BASIC.getNewHandler(proxyIoSession);
                                        break;
                                    }
                                } catch (Exception ex) {
                                    logger.debug("Following exception occured:", (Throwable) ex);
                                }
                            }
                        } else {
                            this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                int method2 = -1;
                Iterator<String> it3 = values.iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        break;
                    }
                    String proxyAuthHeader2 = it3.next().toLowerCase();
                    if (proxyAuthHeader2.contains("ntlm")) {
                        method2 = HttpAuthenticationMethods.NTLM.getId();
                        break;
                    } else if (proxyAuthHeader2.contains("digest") && method2 != HttpAuthenticationMethods.NTLM.getId()) {
                        method2 = HttpAuthenticationMethods.DIGEST.getId();
                    } else if (proxyAuthHeader2.contains("basic") && method2 == -1) {
                        method2 = HttpAuthenticationMethods.BASIC.getId();
                    }
                }
                if (method2 != -1) {
                    try {
                        this.authHandler = HttpAuthenticationMethods.getNewHandler(method2, proxyIoSession);
                    } catch (Exception ex2) {
                        logger.debug("Following exception occured:", (Throwable) ex2);
                    }
                }
                if (this.authHandler == null) {
                    this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
                }
            }
        } else {
            this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
        }
        if (this.authHandler == null) {
            throw new ProxyAuthException("Unknown authentication mechanism(s): " + values);
        }
    }

    /* JADX WARNING: type inference failed for: r0v2, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
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
