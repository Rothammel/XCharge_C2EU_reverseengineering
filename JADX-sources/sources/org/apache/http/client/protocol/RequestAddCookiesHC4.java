package org.apache.http.client.protocol;

import android.util.Log;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.SetCookie2;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.TextUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.eclipse.paho.client.mqttv3.MqttTopic;

@Immutable
/* loaded from: classes.dex */
public class RequestAddCookiesHC4 implements HttpRequestInterceptor {
    private static final String TAG = "HttpClient";

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Header header;
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        String method = request.getRequestLine().getMethod();
        if (!method.equalsIgnoreCase(HttpProxyConstants.CONNECT)) {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            CookieStore cookieStore = clientContext.getCookieStore();
            if (cookieStore == null) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Cookie store not specified in HTTP context");
                    return;
                }
                return;
            }
            Lookup<CookieSpecProvider> registry = clientContext.getCookieSpecRegistry();
            if (registry == null) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "CookieSpec registry not specified in HTTP context");
                    return;
                }
                return;
            }
            HttpHost targetHost = clientContext.getTargetHost();
            if (targetHost == null) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Target host not set in the context");
                    return;
                }
                return;
            }
            RouteInfo route = clientContext.getHttpRoute();
            if (route == null) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Connection route not set in the context");
                    return;
                }
                return;
            }
            RequestConfig config = clientContext.getRequestConfig();
            String policy = config.getCookieSpec();
            if (policy == null) {
                policy = CookieSpecs.BEST_MATCH;
            }
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "CookieSpec selected: " + policy);
            }
            URI requestURI = null;
            if (request instanceof HttpUriRequest) {
                requestURI = ((HttpUriRequest) request).getURI();
            } else {
                try {
                    URI requestURI2 = new URI(request.getRequestLine().getUri());
                    requestURI = requestURI2;
                } catch (URISyntaxException e) {
                }
            }
            String path = requestURI != null ? requestURI.getPath() : null;
            String hostName = targetHost.getHostName();
            int port = targetHost.getPort();
            if (port < 0) {
                port = route.getTargetHost().getPort();
            }
            if (port < 0) {
                port = 0;
            }
            if (TextUtils.isEmpty(path)) {
                path = MqttTopic.TOPIC_LEVEL_SEPARATOR;
            }
            CookieOrigin cookieOrigin = new CookieOrigin(hostName, port, path, route.isSecure());
            CookieSpecProvider provider = registry.lookup(policy);
            if (provider == null) {
                throw new HttpException("Unsupported cookie policy: " + policy);
            }
            CookieSpec cookieSpec = provider.create(clientContext);
            List<Cookie> cookies = new ArrayList<>(cookieStore.getCookies());
            List<Cookie> matchedCookies = new ArrayList<>();
            Date now = new Date();
            for (Cookie cookie : cookies) {
                if (!cookie.isExpired(now)) {
                    if (cookieSpec.match(cookie, cookieOrigin)) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Cookie " + cookie + " match " + cookieOrigin);
                        }
                        matchedCookies.add(cookie);
                    }
                } else if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Cookie " + cookie + " expired");
                }
            }
            if (!matchedCookies.isEmpty()) {
                List<Header> headers = cookieSpec.formatCookies(matchedCookies);
                for (Header header2 : headers) {
                    request.addHeader(header2);
                }
            }
            int ver = cookieSpec.getVersion();
            if (ver > 0) {
                boolean needVersionHeader = false;
                for (Cookie cookie2 : matchedCookies) {
                    if (ver != cookie2.getVersion() || !(cookie2 instanceof SetCookie2)) {
                        needVersionHeader = true;
                    }
                }
                if (needVersionHeader && (header = cookieSpec.getVersionHeader()) != null) {
                    request.addHeader(header);
                }
            }
            context.setAttribute(HttpClientContext.COOKIE_SPEC, cookieSpec);
            context.setAttribute(HttpClientContext.COOKIE_ORIGIN, cookieOrigin);
        }
    }
}
