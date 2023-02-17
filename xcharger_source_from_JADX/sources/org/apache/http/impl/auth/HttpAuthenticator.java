package org.apache.http.impl.auth;

import android.util.Log;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;

public class HttpAuthenticator {
    private static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState = null;
    private static final String TAG = "HttpClient";

    static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState() {
        int[] iArr = $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState;
        if (iArr == null) {
            iArr = new int[AuthProtocolState.values().length];
            try {
                iArr[AuthProtocolState.CHALLENGED.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[AuthProtocolState.FAILURE.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[AuthProtocolState.HANDSHAKE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[AuthProtocolState.SUCCESS.ordinal()] = 5;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[AuthProtocolState.UNCHALLENGED.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState = iArr;
        }
        return iArr;
    }

    public boolean isAuthenticationRequested(HttpHost host, HttpResponse response, AuthenticationStrategy authStrategy, AuthStateHC4 authState, HttpContext context) {
        if (authStrategy.isAuthenticationRequested(host, response, context)) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Authentication required");
            }
            if (authState.getState() == AuthProtocolState.SUCCESS) {
                authStrategy.authFailed(host, authState.getAuthScheme(), context);
            }
            return true;
        }
        switch ($SWITCH_TABLE$org$apache$http$auth$AuthProtocolState()[authState.getState().ordinal()]) {
            case 2:
            case 3:
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Authentication succeeded");
                }
                authState.setState(AuthProtocolState.SUCCESS);
                authStrategy.authSucceeded(host, authState.getAuthScheme(), context);
                break;
            case 5:
                break;
            default:
                authState.setState(AuthProtocolState.UNCHALLENGED);
                break;
        }
        return false;
    }

    public boolean handleAuthChallenge(HttpHost host, HttpResponse response, AuthenticationStrategy authStrategy, AuthStateHC4 authState, HttpContext context) {
        try {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, String.valueOf(host.toHostString()) + " requested authentication");
            }
            Map<String, Header> challenges = authStrategy.getChallenges(host, response, context);
            if (challenges.isEmpty()) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Response contains no authentication challenges");
                }
                return false;
            }
            AuthScheme authScheme = authState.getAuthScheme();
            switch ($SWITCH_TABLE$org$apache$http$auth$AuthProtocolState()[authState.getState().ordinal()]) {
                case 1:
                    break;
                case 2:
                case 3:
                    if (authScheme == null) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Auth scheme is null");
                        }
                        authStrategy.authFailed(host, (AuthScheme) null, context);
                        authState.reset();
                        authState.setState(AuthProtocolState.FAILURE);
                        return false;
                    }
                    break;
                case 4:
                    return false;
                case 5:
                    authState.reset();
                    break;
            }
            if (authScheme != null) {
                Header challenge = challenges.get(authScheme.getSchemeName().toLowerCase(Locale.ENGLISH));
                if (challenge != null) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Authorization challenge processed");
                    }
                    authScheme.processChallenge(challenge);
                    if (authScheme.isComplete()) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Authentication failed");
                        }
                        authStrategy.authFailed(host, authState.getAuthScheme(), context);
                        authState.reset();
                        authState.setState(AuthProtocolState.FAILURE);
                        return false;
                    }
                    authState.setState(AuthProtocolState.HANDSHAKE);
                    return true;
                }
                authState.reset();
            }
            Queue<AuthOption> authOptions = authStrategy.select(challenges, host, response, context);
            if (authOptions == null || authOptions.isEmpty()) {
                return false;
            }
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Selected authentication options: " + authOptions);
            }
            authState.setState(AuthProtocolState.CHALLENGED);
            authState.update(authOptions);
            return true;
        } catch (MalformedChallengeException ex) {
            if (Log.isLoggable(TAG, 5)) {
                Log.w(TAG, "Malformed challenge: " + ex.getMessage());
            }
            authState.reset();
            return false;
        }
    }

    public void generateAuthResponse(HttpRequest request, AuthStateHC4 authState, HttpContext context) throws HttpException, IOException {
        AuthScheme authScheme = authState.getAuthScheme();
        Credentials creds = authState.getCredentials();
        switch ($SWITCH_TABLE$org$apache$http$auth$AuthProtocolState()[authState.getState().ordinal()]) {
            case 2:
                Queue<AuthOption> authOptions = authState.getAuthOptions();
                if (authOptions == null) {
                    ensureAuthScheme(authScheme);
                    break;
                } else {
                    while (!authOptions.isEmpty()) {
                        AuthOption authOption = authOptions.remove();
                        AuthScheme authScheme2 = authOption.getAuthScheme();
                        Credentials creds2 = authOption.getCredentials();
                        authState.update(authScheme2, creds2);
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Generating response to an authentication challenge using " + authScheme2.getSchemeName() + " scheme");
                        }
                        try {
                            request.addHeader(doAuth(authScheme2, creds2, request, context));
                            return;
                        } catch (AuthenticationException ex) {
                            if (Log.isLoggable(TAG, 5)) {
                                Log.w(TAG, authScheme2 + " authentication error: " + ex.getMessage());
                            }
                        }
                    }
                    return;
                }
            case 4:
                return;
            case 5:
                ensureAuthScheme(authScheme);
                if (authScheme.isConnectionBased()) {
                    return;
                }
                break;
        }
        if (authScheme != null) {
            try {
                request.addHeader(doAuth(authScheme, creds, request, context));
            } catch (AuthenticationException ex2) {
                if (Log.isLoggable(TAG, 6)) {
                    Log.e(TAG, authScheme + " authentication error: " + ex2.getMessage());
                }
            }
        }
    }

    private void ensureAuthScheme(AuthScheme authScheme) {
        Asserts.notNull(authScheme, "Auth scheme");
    }

    private Header doAuth(AuthScheme authScheme, Credentials creds, HttpRequest request, HttpContext context) throws AuthenticationException {
        if (authScheme instanceof ContextAwareAuthScheme) {
            return ((ContextAwareAuthScheme) authScheme).authenticate(creds, request, context);
        }
        return authScheme.authenticate(creds, request);
    }
}
