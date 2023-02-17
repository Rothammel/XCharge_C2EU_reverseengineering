package org.apache.http.impl.client;

import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.util.Args;

@ThreadSafe
/* loaded from: classes.dex */
public class SystemDefaultCredentialsProvider implements CredentialsProvider {
    private static final Map<String, String> SCHEME_MAP = new ConcurrentHashMap();
    private final BasicCredentialsProviderHC4 internal = new BasicCredentialsProviderHC4();

    static {
        SCHEME_MAP.put(AuthSchemes.BASIC.toUpperCase(Locale.ENGLISH), AuthSchemes.BASIC);
        SCHEME_MAP.put(AuthSchemes.DIGEST.toUpperCase(Locale.ENGLISH), AuthSchemes.DIGEST);
        SCHEME_MAP.put(AuthSchemes.NTLM.toUpperCase(Locale.ENGLISH), AuthSchemes.NTLM);
        SCHEME_MAP.put(AuthSchemes.SPNEGO.toUpperCase(Locale.ENGLISH), "SPNEGO");
        SCHEME_MAP.put(AuthSchemes.KERBEROS.toUpperCase(Locale.ENGLISH), AuthSchemes.KERBEROS);
    }

    private static String translateScheme(String key) {
        if (key == null) {
            return null;
        }
        String s = SCHEME_MAP.get(key);
        return s == null ? key : s;
    }

    public void setCredentials(AuthScope authscope, Credentials credentials) {
        this.internal.setCredentials(authscope, credentials);
    }

    private static PasswordAuthentication getSystemCreds(AuthScope authscope, Authenticator.RequestorType requestorType) {
        String hostname = authscope.getHost();
        int port = authscope.getPort();
        String protocol = port == 443 ? "https" : ConsoleSetting.SCHEMA_HTTP;
        return Authenticator.requestPasswordAuthentication(hostname, null, port, protocol, null, translateScheme(authscope.getScheme()), null, requestorType);
    }

    public Credentials getCredentials(AuthScope authscope) {
        Args.notNull(authscope, "Auth scope");
        Credentials localcreds = this.internal.getCredentials(authscope);
        if (localcreds == null) {
            if (authscope.getHost() != null) {
                PasswordAuthentication systemcreds = getSystemCreds(authscope, Authenticator.RequestorType.SERVER);
                if (systemcreds == null) {
                    systemcreds = getSystemCreds(authscope, Authenticator.RequestorType.PROXY);
                }
                if (systemcreds != null) {
                    String domain = System.getProperty("http.auth.ntlm.domain");
                    if (domain != null) {
                        return new NTCredentials(systemcreds.getUserName(), new String(systemcreds.getPassword()), (String) null, domain);
                    }
                    if (AuthSchemes.NTLM.equalsIgnoreCase(authscope.getScheme())) {
                        return new NTCredentials(systemcreds.getUserName(), new String(systemcreds.getPassword()), (String) null, (String) null);
                    }
                    return new UsernamePasswordCredentials(systemcreds.getUserName(), new String(systemcreds.getPassword()));
                }
            }
            return null;
        }
        return localcreds;
    }

    public void clear() {
        this.internal.clear();
    }
}
