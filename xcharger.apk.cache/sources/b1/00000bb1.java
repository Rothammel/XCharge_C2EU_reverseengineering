package org.apache.http.client.params;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.params.HttpParams;

@Deprecated
/* loaded from: classes.dex */
public final class HttpClientParamConfig {
    private HttpClientParamConfig() {
    }

    public static RequestConfig getRequestConfig(HttpParams params) {
        Map<?, ?> map;
        if (params == null) {
            return null;
        }
        try {
            Field f = params.getClass().getDeclaredField("parameters");
            f.setAccessible(true);
            map = (Map) f.get(params);
        } catch (Exception e) {
        }
        if (map != null) {
            if (map.isEmpty()) {
                return null;
            }
            return RequestConfig.custom().setSocketTimeout(params.getIntParameter("http.socket.timeout", 0)).setStaleConnectionCheckEnabled(params.getBooleanParameter("http.connection.stalecheck", true)).setConnectTimeout(params.getIntParameter("http.connection.timeout", 0)).setExpectContinueEnabled(params.getBooleanParameter("http.protocol.expect-continue", false)).setProxy((HttpHost) params.getParameter("http.route.default-proxy")).setLocalAddress((InetAddress) params.getParameter("http.route.local-address")).setAuthenticationEnabled(params.getBooleanParameter("http.protocol.handle-authentication", true)).setCircularRedirectsAllowed(params.getBooleanParameter("http.protocol.allow-circular-redirects", false)).setCookieSpec((String) params.getParameter("http.protocol.cookie-policy")).setMaxRedirects(params.getIntParameter("http.protocol.max-redirects", 50)).setRedirectsEnabled(params.getBooleanParameter("http.protocol.handle-redirects", true)).setRelativeRedirectsAllowed(!params.getBooleanParameter("http.protocol.reject-relative-redirect", false)).build();
        }
        return null;
    }
}