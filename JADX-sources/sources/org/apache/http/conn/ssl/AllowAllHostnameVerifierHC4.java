package org.apache.http.conn.ssl;

import org.apache.http.annotation.Immutable;

@Immutable
/* loaded from: classes.dex */
public class AllowAllHostnameVerifierHC4 extends AbstractVerifierHC4 {
    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, String[] cns, String[] subjectAlts) {
    }

    public final String toString() {
        return "ALLOW_ALL";
    }
}
