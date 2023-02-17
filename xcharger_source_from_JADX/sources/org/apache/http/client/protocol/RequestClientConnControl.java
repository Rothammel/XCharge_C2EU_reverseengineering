package org.apache.http.client.protocol;

import android.util.Log;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

@Immutable
public class RequestClientConnControl implements HttpRequestInterceptor {
    private static final String PROXY_CONN_DIRECTIVE = "Proxy-Connection";
    private static final String TAG = "HttpClient";

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (request.getRequestLine().getMethod().equalsIgnoreCase(HttpProxyConstants.CONNECT)) {
            request.setHeader(PROXY_CONN_DIRECTIVE, "Keep-Alive");
            return;
        }
        RouteInfo route = HttpClientContext.adapt(context).getHttpRoute();
        if (route != null) {
            if ((route.getHopCount() == 1 || route.isTunnelled()) && !request.containsHeader(HttpHeaders.CONNECTION)) {
                request.addHeader(HttpHeaders.CONNECTION, "Keep-Alive");
            }
            if (route.getHopCount() == 2 && !route.isTunnelled() && !request.containsHeader(PROXY_CONN_DIRECTIVE)) {
                request.addHeader(PROXY_CONN_DIRECTIVE, "Keep-Alive");
            }
        } else if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Connection route not set in the context");
        }
    }
}
