package org.apache.mina.proxy.handlers.http;

import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.basic.HttpBasicAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.basic.HttpNoAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.digest.HttpDigestAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.ntlm.HttpNTLMAuthLogicHandler;
import org.apache.mina.proxy.session.ProxyIoSession;

public enum HttpAuthenticationMethods {
    NO_AUTH(1),
    BASIC(2),
    NTLM(3),
    DIGEST(4);
    

    /* renamed from: id */
    private final int f195id;

    private HttpAuthenticationMethods(int id) {
        this.f195id = id;
    }

    public int getId() {
        return this.f195id;
    }

    public AbstractAuthLogicHandler getNewHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
        return getNewHandler(this.f195id, proxyIoSession);
    }

    public static AbstractAuthLogicHandler getNewHandler(int method, ProxyIoSession proxyIoSession) throws ProxyAuthException {
        if (method == BASIC.f195id) {
            return new HttpBasicAuthLogicHandler(proxyIoSession);
        }
        if (method == DIGEST.f195id) {
            return new HttpDigestAuthLogicHandler(proxyIoSession);
        }
        if (method == NTLM.f195id) {
            return new HttpNTLMAuthLogicHandler(proxyIoSession);
        }
        if (method == NO_AUTH.f195id) {
            return new HttpNoAuthLogicHandler(proxyIoSession);
        }
        return null;
    }
}
