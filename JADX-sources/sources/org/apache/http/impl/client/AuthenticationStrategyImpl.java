package org.apache.http.impl.client;

import android.util.Log;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthCache;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@Immutable
/* loaded from: classes.dex */
abstract class AuthenticationStrategyImpl implements AuthenticationStrategy {
    private static final List<String> DEFAULT_SCHEME_PRIORITY = Collections.unmodifiableList(Arrays.asList(AuthSchemes.SPNEGO, AuthSchemes.KERBEROS, AuthSchemes.NTLM, AuthSchemes.DIGEST, AuthSchemes.BASIC));
    private static final String TAG = "HttpClient";
    private final int challengeCode;
    private final String headerName;

    abstract Collection<String> getPreferredAuthSchemes(RequestConfig requestConfig);

    /* JADX INFO: Access modifiers changed from: package-private */
    public AuthenticationStrategyImpl(int challengeCode, String headerName) {
        this.challengeCode = challengeCode;
        this.headerName = headerName;
    }

    @Override // org.apache.http.client.AuthenticationStrategy
    public boolean isAuthenticationRequested(HttpHost authhost, HttpResponse response, HttpContext context) {
        Args.notNull(response, "HTTP response");
        int status = response.getStatusLine().getStatusCode();
        return status == this.challengeCode;
    }

    @Override // org.apache.http.client.AuthenticationStrategy
    public Map<String, Header> getChallenges(HttpHost authhost, HttpResponse response, HttpContext context) throws MalformedChallengeException {
        CharArrayBuffer buffer;
        int pos;
        Args.notNull(response, "HTTP response");
        FormattedHeader[] headers = response.getHeaders(this.headerName);
        Map<String, Header> map = new HashMap<>(headers.length);
        for (FormattedHeader formattedHeader : headers) {
            if (formattedHeader instanceof FormattedHeader) {
                buffer = formattedHeader.getBuffer();
                pos = formattedHeader.getValuePos();
            } else {
                String s = formattedHeader.getValue();
                if (s == null) {
                    throw new MalformedChallengeException("Header value is null");
                }
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                pos = 0;
            }
            while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            int beginIndex = pos;
            while (pos < buffer.length() && !HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            int endIndex = pos;
            map.put(buffer.substring(beginIndex, endIndex).toLowerCase(Locale.ENGLISH), formattedHeader);
        }
        return map;
    }

    @Override // org.apache.http.client.AuthenticationStrategy
    public Queue<AuthOption> select(Map<String, Header> challenges, HttpHost authhost, HttpResponse response, HttpContext context) throws MalformedChallengeException {
        Args.notNull(challenges, "Map of auth challenges");
        Args.notNull(authhost, "Host");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Queue<AuthOption> options = new LinkedList<>();
        Lookup<AuthSchemeProvider> registry = clientContext.getAuthSchemeRegistry();
        if (registry == null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Auth scheme registry not set in the context");
            }
        } else {
            CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
            if (credsProvider == null) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Credentials provider not set in the context");
                }
            } else {
                RequestConfig config = clientContext.getRequestConfig();
                Collection<String> authPrefs = getPreferredAuthSchemes(config);
                if (authPrefs == null) {
                    authPrefs = DEFAULT_SCHEME_PRIORITY;
                }
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Authentication schemes in the order of preference: " + authPrefs);
                }
                for (String id : authPrefs) {
                    Header challenge = challenges.get(id.toLowerCase(Locale.ENGLISH));
                    if (challenge != null) {
                        AuthSchemeProvider authSchemeProvider = registry.lookup(id);
                        if (authSchemeProvider == null) {
                            if (Log.isLoggable(TAG, 5)) {
                                Log.w(TAG, "Authentication scheme " + id + " not supported");
                            }
                        } else {
                            AuthScheme authScheme = authSchemeProvider.create(context);
                            authScheme.processChallenge(challenge);
                            AuthScope authScope = new AuthScope(authhost.getHostName(), authhost.getPort(), authScheme.getRealm(), authScheme.getSchemeName());
                            Credentials credentials = credsProvider.getCredentials(authScope);
                            if (credentials != null) {
                                options.add(new AuthOption(authScheme, credentials));
                            }
                        }
                    } else if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Challenge for " + id + " authentication scheme not available");
                    }
                }
            }
        }
        return options;
    }

    @Override // org.apache.http.client.AuthenticationStrategy
    public void authSucceeded(HttpHost authhost, AuthScheme authScheme, HttpContext context) {
        Args.notNull(authhost, "Host");
        Args.notNull(authScheme, "Auth scheme");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        if (isCachable(authScheme)) {
            AuthCache authCache = clientContext.getAuthCache();
            if (authCache == null) {
                authCache = new BasicAuthCache();
                clientContext.setAuthCache(authCache);
            }
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Caching '" + authScheme.getSchemeName() + "' auth scheme for " + authhost);
            }
            authCache.put(authhost, authScheme);
        }
    }

    protected boolean isCachable(AuthScheme authScheme) {
        if (authScheme == null || !authScheme.isComplete()) {
            return false;
        }
        String schemeName = authScheme.getSchemeName();
        return schemeName.equalsIgnoreCase(AuthSchemes.BASIC) || schemeName.equalsIgnoreCase(AuthSchemes.DIGEST);
    }

    @Override // org.apache.http.client.AuthenticationStrategy
    public void authFailed(HttpHost authhost, AuthScheme authScheme, HttpContext context) {
        Args.notNull(authhost, "Host");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        AuthCache authCache = clientContext.getAuthCache();
        if (authCache != null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Clearing cached auth scheme for " + authhost);
            }
            authCache.remove(authhost);
        }
    }
}
