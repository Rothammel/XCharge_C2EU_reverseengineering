package org.apache.http.impl.client;

import android.util.Log;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import okhttp3.internal.http.StatusLine;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtilsHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.TextUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

@Immutable
/* loaded from: classes.dex */
public class DefaultRedirectStrategy implements RedirectStrategy {
    @Deprecated
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    private static final String TAG = "HttpClient";
    public static final DefaultRedirectStrategy INSTANCE = new DefaultRedirectStrategy();
    private static final String[] REDIRECT_METHODS = {"GET", HttpHeadHC4.METHOD_NAME};

    @Override // org.apache.http.client.RedirectStrategy
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader(RequestParameters.SUBRESOURCE_LOCATION);
        switch (statusCode) {
            case 301:
            case StatusLine.HTTP_TEMP_REDIRECT /* 307 */:
                return isRedirectable(method);
            case 302:
                return isRedirectable(method) && locationHeader != null;
            case 303:
                return true;
            case 304:
            case 305:
            case 306:
            default:
                return false;
        }
    }

    public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Header locationHeader = response.getFirstHeader(RequestParameters.SUBRESOURCE_LOCATION);
        if (locationHeader == null) {
            throw new ProtocolException("Received redirect response " + response.getStatusLine() + " but no location header");
        }
        String location = locationHeader.getValue();
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Redirect requested to location '" + location + "'");
        }
        RequestConfig config = clientContext.getRequestConfig();
        URI uri = createLocationURI(location);
        try {
            if (!uri.isAbsolute()) {
                if (!config.isRelativeRedirectsAllowed()) {
                    throw new ProtocolException("Relative redirect location '" + uri + "' not allowed");
                }
                HttpHost target = clientContext.getTargetHost();
                Asserts.notNull(target, "Target host");
                URI requestURI = new URI(request.getRequestLine().getUri());
                URI absoluteRequestURI = URIUtilsHC4.rewriteURI(requestURI, target, false);
                uri = URIUtilsHC4.resolve(absoluteRequestURI, uri);
            }
            RedirectLocationsHC4 redirectLocations = (RedirectLocationsHC4) clientContext.getAttribute("http.protocol.redirect-locations");
            if (redirectLocations == null) {
                redirectLocations = new RedirectLocationsHC4();
                context.setAttribute("http.protocol.redirect-locations", redirectLocations);
            }
            if (!config.isCircularRedirectsAllowed() && redirectLocations.contains(uri)) {
                throw new CircularRedirectException("Circular redirect to '" + uri + "'");
            }
            redirectLocations.add(uri);
            return uri;
        } catch (URISyntaxException ex) {
            throw new ProtocolException(ex.getMessage(), ex);
        }
    }

    protected URI createLocationURI(String location) throws ProtocolException {
        try {
            URIBuilder b = new URIBuilder(new URI(location).normalize());
            String host = b.getHost();
            if (host != null) {
                b.setHost(host.toLowerCase(Locale.ENGLISH));
            }
            String path = b.getPath();
            if (TextUtils.isEmpty(path)) {
                b.setPath(MqttTopic.TOPIC_LEVEL_SEPARATOR);
            }
            return b.build();
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
    }

    protected boolean isRedirectable(String method) {
        String[] strArr;
        for (String m : REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    @Override // org.apache.http.client.RedirectStrategy
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        URI uri = getLocationURI(request, response, context);
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHeadHC4.METHOD_NAME)) {
            return new HttpHeadHC4(uri);
        }
        if (method.equalsIgnoreCase("GET")) {
            return new HttpGetHC4(uri);
        }
        int status = response.getStatusLine().getStatusCode();
        if (status == 307) {
            return RequestBuilder.copy(request).setUri(uri).build();
        }
        return new HttpGetHC4(uri);
    }
}
