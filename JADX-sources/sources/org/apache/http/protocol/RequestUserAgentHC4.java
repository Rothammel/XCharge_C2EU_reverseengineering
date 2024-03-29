package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class RequestUserAgentHC4 implements HttpRequestInterceptor {
    private final String userAgent;

    public RequestUserAgentHC4(String userAgent) {
        this.userAgent = userAgent;
    }

    public RequestUserAgentHC4() {
        this(null);
    }

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (!request.containsHeader("User-Agent")) {
            String s = null;
            HttpParams params = request.getParams();
            if (params != null) {
                s = (String) params.getParameter("http.useragent");
            }
            if (s == null) {
                s = this.userAgent;
            }
            if (s != null) {
                request.addHeader("User-Agent", s);
            }
        }
    }
}
