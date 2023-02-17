package org.apache.http.client.protocol;

import android.util.Log;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class RequestAuthCache implements HttpRequestInterceptor {
    private static final String TAG = "HttpClient";

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        AuthScheme authScheme;
        AuthScheme authScheme2;
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        AuthCache authCache = clientContext.getAuthCache();
        if (authCache == null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Auth cache not set in the context");
                return;
            }
            return;
        }
        CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
        if (credsProvider == null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Credentials provider not set in the context");
                return;
            }
            return;
        }
        RouteInfo route = clientContext.getHttpRoute();
        if (route == null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Route info not set in the context");
                return;
            }
            return;
        }
        HttpHost target = clientContext.getTargetHost();
        if (target == null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Target host not set in the context");
                return;
            }
            return;
        }
        if (target.getPort() < 0) {
            target = new HttpHost(target.getHostName(), route.getTargetHost().getPort(), target.getSchemeName());
        }
        AuthStateHC4 targetState = clientContext.getTargetAuthState();
        if (targetState != null && targetState.getState() == AuthProtocolState.UNCHALLENGED && (authScheme2 = authCache.get(target)) != null) {
            doPreemptiveAuth(target, authScheme2, targetState, credsProvider);
        }
        HttpHost proxy = route.getProxyHost();
        AuthStateHC4 proxyState = clientContext.getProxyAuthState();
        if (proxy != null && proxyState != null && proxyState.getState() == AuthProtocolState.UNCHALLENGED && (authScheme = authCache.get(proxy)) != null) {
            doPreemptiveAuth(proxy, authScheme, proxyState, credsProvider);
        }
    }

    private void doPreemptiveAuth(HttpHost host, AuthScheme authScheme, AuthStateHC4 authState, CredentialsProvider credsProvider) {
        String schemeName = authScheme.getSchemeName();
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Re-using cached '" + schemeName + "' auth scheme for " + host);
        }
        AuthScope authScope = new AuthScope(host.getHostName(), host.getPort(), AuthScope.ANY_REALM, schemeName);
        Credentials creds = credsProvider.getCredentials(authScope);
        if (creds != null) {
            if ("BASIC".equalsIgnoreCase(authScheme.getSchemeName())) {
                authState.setState(AuthProtocolState.CHALLENGED);
            } else {
                authState.setState(AuthProtocolState.SUCCESS);
            }
            authState.update(authScheme, creds);
        } else if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "No credentials for preemptive authentication");
        }
    }
}
