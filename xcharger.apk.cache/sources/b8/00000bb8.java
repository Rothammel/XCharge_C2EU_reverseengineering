package org.apache.http.client.protocol;

import java.io.IOException;
import java.util.Collection;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

@Immutable
/* loaded from: classes.dex */
public class RequestDefaultHeadersHC4 implements HttpRequestInterceptor {
    private final Collection<? extends Header> defaultHeaders;

    public RequestDefaultHeadersHC4(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public RequestDefaultHeadersHC4() {
        this(null);
    }

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getRequestLine().getMethod();
        if (!method.equalsIgnoreCase(HttpProxyConstants.CONNECT)) {
            Collection<? extends Header> defHeaders = (Collection) request.getParams().getParameter("http.default-headers");
            if (defHeaders == null) {
                defHeaders = this.defaultHeaders;
            }
            if (defHeaders != null) {
                for (Header defHeader : defHeaders) {
                    request.addHeader(defHeader);
                }
            }
        }
    }
}