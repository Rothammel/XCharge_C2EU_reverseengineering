package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.Args;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

@Immutable
/* loaded from: classes.dex */
public class RequestConnControlHC4 implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getRequestLine().getMethod();
        if (!method.equalsIgnoreCase(HttpProxyConstants.CONNECT) && !request.containsHeader(HttpHeaders.CONNECTION)) {
            request.addHeader(HttpHeaders.CONNECTION, "Keep-Alive");
        }
    }
}
