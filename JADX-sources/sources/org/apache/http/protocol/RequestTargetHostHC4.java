package org.apache.http.protocol;

import java.io.IOException;
import java.net.InetAddress;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.Args;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

@Immutable
/* loaded from: classes.dex */
public class RequestTargetHostHC4 implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        HttpCoreContext corecontext = HttpCoreContext.adapt(context);
        ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
        String method = request.getRequestLine().getMethod();
        if ((!method.equalsIgnoreCase(HttpProxyConstants.CONNECT) || !ver.lessEquals(HttpVersion.HTTP_1_0)) && !request.containsHeader("Host")) {
            HttpHost targethost = corecontext.getTargetHost();
            if (targethost == null) {
                HttpInetConnection connection = corecontext.getConnection();
                if (connection instanceof HttpInetConnection) {
                    InetAddress address = connection.getRemoteAddress();
                    int port = connection.getRemotePort();
                    if (address != null) {
                        targethost = new HttpHost(address.getHostName(), port);
                    }
                }
                if (targethost == null) {
                    if (!ver.lessEquals(HttpVersion.HTTP_1_0)) {
                        throw new ProtocolException("Target host missing");
                    }
                    return;
                }
            }
            request.addHeader("Host", targethost.toHostString());
        }
    }
}
