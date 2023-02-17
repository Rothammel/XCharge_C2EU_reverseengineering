package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpGetHC4 extends HttpRequestBaseHC4 {
    public static final String METHOD_NAME = "GET";

    public HttpGetHC4() {
    }

    public HttpGetHC4(URI uri) {
        setURI(uri);
    }

    public HttpGetHC4(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return "GET";
    }
}
