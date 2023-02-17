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
public class DefaultRedirectStrategy implements RedirectStrategy {
    public static final DefaultRedirectStrategy INSTANCE = new DefaultRedirectStrategy();
    @Deprecated
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    private static final String[] REDIRECT_METHODS = {"GET", HttpHeadHC4.METHOD_NAME};
    private static final String TAG = "HttpClient";

    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader(RequestParameters.SUBRESOURCE_LOCATION);
        switch (statusCode) {
            case 301:
            case StatusLine.HTTP_TEMP_REDIRECT:
                return isRedirectable(method);
            case 302:
                if (!isRedirectable(method) || locationHeader == null) {
                    return false;
                }
                return true;
            case 303:
                return true;
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
                uri = URIUtilsHC4.resolve(URIUtilsHC4.rewriteURI(new URI(request.getRequestLine().getUri()), target, false), uri);
            }
            RedirectLocationsHC4 redirectLocations = (RedirectLocationsHC4) clientContext.getAttribute("http.protocol.redirect-locations");
            if (redirectLocations == null) {
                redirectLocations = new RedirectLocationsHC4();
                context.setAttribute("http.protocol.redirect-locations", redirectLocations);
            }
            if (config.isCircularRedirectsAllowed() || !redirectLocations.contains(uri)) {
                redirectLocations.add(uri);
                return uri;
            }
            throw new CircularRedirectException("Circular redirect to '" + uri + "'");
        } catch (URISyntaxException ex) {
            throw new ProtocolException(ex.getMessage(), ex);
        }
    }

    /* access modifiers changed from: protected */
    public URI createLocationURI(String location) throws ProtocolException {
        try {
            URIBuilder b = new URIBuilder(new URI(location).normalize());
            String host = b.getHost();
            if (host != null) {
                b.setHost(host.toLowerCase(Locale.ENGLISH));
            }
            if (TextUtils.isEmpty(b.getPath())) {
                b.setPath(MqttTopic.TOPIC_LEVEL_SEPARATOR);
            }
            return b.build();
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRedirectable(String method) {
        for (String m : REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        URI uri = getLocationURI(request, response, context);
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHeadHC4.METHOD_NAME)) {
            return new HttpHeadHC4(uri);
        }
        if (method.equalsIgnoreCase("GET")) {
            return new HttpGetHC4(uri);
        }
        if (response.getStatusLine().getStatusCode() == 307) {
            return RequestBuilder.copy(request).setUri(uri).build();
        }
        return new HttpGetHC4(uri);
    }
}
