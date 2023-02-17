package org.apache.http.impl.auth;

import java.nio.charset.Charset;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class DigestSchemeFactoryHC4 implements AuthSchemeFactory, AuthSchemeProvider {
    private final Charset charset;

    public DigestSchemeFactoryHC4(Charset charset2) {
        this.charset = charset2;
    }

    public DigestSchemeFactoryHC4() {
        this((Charset) null);
    }

    public AuthScheme newInstance(HttpParams params) {
        return new DigestSchemeHC4();
    }

    public AuthScheme create(HttpContext context) {
        return new DigestSchemeHC4(this.charset);
    }
}
