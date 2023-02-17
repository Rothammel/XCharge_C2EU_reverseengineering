package org.apache.http.conn.ssl;

import javax.net.ssl.SSLException;
import org.apache.http.annotation.Immutable;

@Immutable
/* loaded from: classes.dex */
public class BrowserCompatHostnameVerifierHC4 extends AbstractVerifierHC4 {
    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, false);
    }

    @Override // org.apache.http.conn.ssl.AbstractVerifierHC4
    boolean validCountryWildcard(String cn) {
        return true;
    }

    public final String toString() {
        return "BROWSER_COMPATIBLE";
    }
}