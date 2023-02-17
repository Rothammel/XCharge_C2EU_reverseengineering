package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
/* loaded from: classes.dex */
public class HttpDeleteHC4 extends HttpRequestBaseHC4 {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteHC4() {
    }

    public HttpDeleteHC4(URI uri) {
        setURI(uri);
    }

    public HttpDeleteHC4(String uri) {
        setURI(URI.create(uri));
    }

    @Override // org.apache.http.client.methods.HttpRequestBaseHC4
    public String getMethod() {
        return METHOD_NAME;
    }
}
