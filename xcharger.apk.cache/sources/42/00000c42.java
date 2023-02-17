package org.apache.http.impl.client;

import java.security.Principal;
import javax.net.ssl.SSLSession;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.auth.Credentials;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;

@Immutable
/* loaded from: classes.dex */
public class DefaultUserTokenHandlerHC4 implements UserTokenHandler {
    public static final DefaultUserTokenHandlerHC4 INSTANCE = new DefaultUserTokenHandlerHC4();

    public Object getUserToken(HttpContext context) {
        SSLSession sslsession;
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Principal userPrincipal = null;
        AuthStateHC4 targetAuthState = clientContext.getTargetAuthState();
        if (targetAuthState != null && (userPrincipal = getAuthPrincipal(targetAuthState)) == null) {
            AuthStateHC4 proxyAuthState = clientContext.getProxyAuthState();
            userPrincipal = getAuthPrincipal(proxyAuthState);
        }
        if (userPrincipal == null) {
            ManagedHttpClientConnection connection = clientContext.getConnection();
            if (connection.isOpen() && (connection instanceof ManagedHttpClientConnection) && (sslsession = connection.getSSLSession()) != null) {
                Principal userPrincipal2 = sslsession.getLocalPrincipal();
                return userPrincipal2;
            }
            return userPrincipal;
        }
        return userPrincipal;
    }

    private static Principal getAuthPrincipal(AuthStateHC4 authState) {
        Credentials creds;
        AuthScheme scheme = authState.getAuthScheme();
        if (scheme == null || !scheme.isComplete() || !scheme.isConnectionBased() || (creds = authState.getCredentials()) == null) {
            return null;
        }
        return creds.getUserPrincipal();
    }
}