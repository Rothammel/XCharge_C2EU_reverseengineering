package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpHeadHC4 extends HttpRequestBaseHC4 {
    public static final String METHOD_NAME = "HEAD";

    public HttpHeadHC4() {
    }

    public HttpHeadHC4(URI uri) {
        setURI(uri);
    }

    public HttpHeadHC4(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
