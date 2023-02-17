package org.apache.http.client.methods;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.util.Args;

@NotThreadSafe
/* loaded from: classes.dex */
public class HttpOptionsHC4 extends HttpRequestBaseHC4 {
    public static final String METHOD_NAME = "OPTIONS";

    public HttpOptionsHC4() {
    }

    public HttpOptionsHC4(URI uri) {
        setURI(uri);
    }

    public HttpOptionsHC4(String uri) {
        setURI(URI.create(uri));
    }

    @Override // org.apache.http.client.methods.HttpRequestBaseHC4
    public String getMethod() {
        return METHOD_NAME;
    }

    public Set<String> getAllowedMethods(HttpResponse response) {
        Args.notNull(response, "HTTP response");
        HeaderIterator it2 = response.headerIterator(HttpHeaders.ALLOW);
        Set<String> methods = new HashSet<>();
        while (it2.hasNext()) {
            Header header = it2.nextHeader();
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                methods.add(element.getName());
            }
        }
        return methods;
    }
}