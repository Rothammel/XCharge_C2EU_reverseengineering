package org.apache.http.impl.client;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.protocol.HttpContext;

@Immutable
public class ProxyAuthenticationStrategy extends AuthenticationStrategyImpl {
    public static final ProxyAuthenticationStrategy INSTANCE = new ProxyAuthenticationStrategy();

    public /* bridge */ /* synthetic */ void authFailed(HttpHost httpHost, AuthScheme authScheme, HttpContext httpContext) {
        super.authFailed(httpHost, authScheme, httpContext);
    }

    public /* bridge */ /* synthetic */ void authSucceeded(HttpHost httpHost, AuthScheme authScheme, HttpContext httpContext) {
        super.authSucceeded(httpHost, authScheme, httpContext);
    }

    public /* bridge */ /* synthetic */ Map getChallenges(HttpHost httpHost, HttpResponse httpResponse, HttpContext httpContext) throws MalformedChallengeException {
        return super.getChallenges(httpHost, httpResponse, httpContext);
    }

    public /* bridge */ /* synthetic */ boolean isAuthenticationRequested(HttpHost httpHost, HttpResponse httpResponse, HttpContext httpContext) {
        return super.isAuthenticationRequested(httpHost, httpResponse, httpContext);
    }

    public /* bridge */ /* synthetic */ Queue select(Map map, HttpHost httpHost, HttpResponse httpResponse, HttpContext httpContext) throws MalformedChallengeException {
        return super.select(map, httpHost, httpResponse, httpContext);
    }

    public ProxyAuthenticationStrategy() {
        super(407, HttpHeaders.PROXY_AUTHENTICATE);
    }

    /* access modifiers changed from: package-private */
    public Collection<String> getPreferredAuthSchemes(RequestConfig config) {
        return config.getProxyPreferredAuthSchemes();
    }
}
