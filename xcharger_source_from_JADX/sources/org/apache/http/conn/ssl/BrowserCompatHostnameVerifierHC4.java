package org.apache.http.conn.ssl;

import javax.net.ssl.SSLException;
import org.apache.http.annotation.Immutable;

@Immutable
public class BrowserCompatHostnameVerifierHC4 extends AbstractVerifierHC4 {
    public final void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, false);
    }

    /* access modifiers changed from: package-private */
    public boolean validCountryWildcard(String cn) {
        return true;
    }

    public final String toString() {
        return "BROWSER_COMPATIBLE";
    }
}
