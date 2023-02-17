package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpPostHC4 extends HttpEntityEnclosingRequestBaseHC4 {
    public static final String METHOD_NAME = "POST";

    public HttpPostHC4() {
    }

    public HttpPostHC4(URI uri) {
        setURI(uri);
    }

    public HttpPostHC4(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
