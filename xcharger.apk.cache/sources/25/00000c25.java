package org.apache.http.impl.auth;

import android.util.Log;
import java.io.IOException;
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
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;

/* loaded from: classes.dex */
public class HttpAuthenticator {
    private static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState = null;
    private static final String TAG = "HttpClient";

    static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState() {
        int[] iArr = $SWITCH_TABLE$org$apache$http$auth$AuthProtocolState;
        if (iArr == null) {
            iArr = new int[AuthProtocolState.valuesCustom().length];
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
            case 4:
            default:
                authState.setState(AuthProtocolState.UNCHALLENGED);
                break;
            case 5:
                break;
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x005c A[Catch: MalformedChallengeException -> 0x008f, TryCatch #0 {MalformedChallengeException -> 0x008f, blocks: (B:2:0x0000, B:4:0x0009, B:5:0x0025, B:7:0x002f, B:9:0x0038, B:12:0x0041, B:13:0x0053, B:14:0x0056, B:16:0x005c, B:18:0x0062, B:20:0x006b, B:21:0x007f, B:23:0x008b, B:31:0x00b8, B:33:0x00c1, B:34:0x00c8, B:36:0x00d9, B:38:0x00eb, B:40:0x00f4, B:41:0x00fb, B:43:0x0104, B:45:0x010d, B:46:0x0114, B:47:0x0126, B:48:0x012e), top: B:51:0x0000 }] */
    /* JADX WARN: Removed duplicated region for block: B:36:0x00d9 A[Catch: MalformedChallengeException -> 0x008f, TryCatch #0 {MalformedChallengeException -> 0x008f, blocks: (B:2:0x0000, B:4:0x0009, B:5:0x0025, B:7:0x002f, B:9:0x0038, B:12:0x0041, B:13:0x0053, B:14:0x0056, B:16:0x005c, B:18:0x0062, B:20:0x006b, B:21:0x007f, B:23:0x008b, B:31:0x00b8, B:33:0x00c1, B:34:0x00c8, B:36:0x00d9, B:38:0x00eb, B:40:0x00f4, B:41:0x00fb, B:43:0x0104, B:45:0x010d, B:46:0x0114, B:47:0x0126, B:48:0x012e), top: B:51:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean handleAuthChallenge(org.apache.http.HttpHost r10, org.apache.http.HttpResponse r11, org.apache.http.client.AuthenticationStrategy r12, org.apache.http.auth.AuthStateHC4 r13, org.apache.http.protocol.HttpContext r14) {
        /*
            Method dump skipped, instructions count: 324
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.auth.HttpAuthenticator.handleAuthChallenge(org.apache.http.HttpHost, org.apache.http.HttpResponse, org.apache.http.client.AuthenticationStrategy, org.apache.http.auth.AuthStateHC4, org.apache.http.protocol.HttpContext):boolean");
    }

    public void generateAuthResponse(HttpRequest request, AuthStateHC4 authState, HttpContext context) throws HttpException, IOException {
        AuthScheme authScheme = authState.getAuthScheme();
        Credentials creds = authState.getCredentials();
        switch ($SWITCH_TABLE$org$apache$http$auth$AuthProtocolState()[authState.getState().ordinal()]) {
            case 2:
                Queue<AuthOption> authOptions = authState.getAuthOptions();
                if (authOptions != null) {
                    while (!authOptions.isEmpty()) {
                        AuthOption authOption = authOptions.remove();
                        AuthScheme authScheme2 = authOption.getAuthScheme();
                        Credentials creds2 = authOption.getCredentials();
                        authState.update(authScheme2, creds2);
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Generating response to an authentication challenge using " + authScheme2.getSchemeName() + " scheme");
                        }
                        try {
                            Header header = doAuth(authScheme2, creds2, request, context);
                            request.addHeader(header);
                            return;
                        } catch (AuthenticationException ex) {
                            if (Log.isLoggable(TAG, 5)) {
                                Log.w(TAG, authScheme2 + " authentication error: " + ex.getMessage());
                            }
                        }
                    }
                    return;
                }
                ensureAuthScheme(authScheme);
                break;
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
                Header header2 = doAuth(authScheme, creds, request, context);
                request.addHeader(header2);
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
        return authScheme instanceof ContextAwareAuthScheme ? ((ContextAwareAuthScheme) authScheme).authenticate(creds, request, context) : authScheme.authenticate(creds, request);
    }
}