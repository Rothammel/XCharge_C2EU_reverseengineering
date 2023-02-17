package org.apache.http.impl.auth;

import java.nio.charset.Charset;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class BasicSchemeFactoryHC4 implements AuthSchemeFactory, AuthSchemeProvider {
    private final Charset charset;

    public BasicSchemeFactoryHC4(Charset charset2) {
        this.charset = charset2;
    }

    public BasicSchemeFactoryHC4() {
        this((Charset) null);
    }

    public AuthScheme newInstance(HttpParams params) {
        return new BasicSchemeHC4();
    }

    public AuthScheme create(HttpContext context) {
        return new BasicSchemeHC4(this.charset);
    }
}
