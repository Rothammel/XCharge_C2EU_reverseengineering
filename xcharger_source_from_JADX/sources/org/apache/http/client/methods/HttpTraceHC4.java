package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpTraceHC4 extends HttpRequestBaseHC4 {
    public static final String METHOD_NAME = "TRACE";

    public HttpTraceHC4() {
    }

    public HttpTraceHC4(URI uri) {
        setURI(uri);
    }

    public HttpTraceHC4(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
