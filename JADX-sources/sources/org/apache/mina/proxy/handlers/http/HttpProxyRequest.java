package org.apache.mina.proxy.handlers.http;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.http.conn.ssl.TokenParser;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class HttpProxyRequest extends ProxyRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyRequest.class);
    private Map<String, List<String>> headers;
    private String host;
    private final String httpURI;
    private final String httpVerb;
    private String httpVersion;
    private transient Map<String, String> properties;

    public HttpProxyRequest(InetSocketAddress endpointAddress) {
        this(endpointAddress, HttpProxyConstants.HTTP_1_0, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion) {
        this(endpointAddress, httpVersion, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion, Map<String, List<String>> headers) {
        this.httpVerb = HttpProxyConstants.CONNECT;
        if (!endpointAddress.isUnresolved()) {
            this.httpURI = endpointAddress.getHostName() + ":" + endpointAddress.getPort();
        } else {
            this.httpURI = endpointAddress.getAddress().getHostAddress() + ":" + endpointAddress.getPort();
        }
        this.httpVersion = httpVersion;
        this.headers = headers;
    }

    public HttpProxyRequest(String httpURI) {
        this("GET", httpURI, HttpProxyConstants.HTTP_1_0, null);
    }

    public HttpProxyRequest(String httpURI, String httpVersion) {
        this("GET", httpURI, httpVersion, null);
    }

    public HttpProxyRequest(String httpVerb, String httpURI, String httpVersion) {
        this(httpVerb, httpURI, httpVersion, null);
    }

    public HttpProxyRequest(String httpVerb, String httpURI, String httpVersion, Map<String, List<String>> headers) {
        this.httpVerb = httpVerb;
        this.httpURI = httpURI;
        this.httpVersion = httpVersion;
        this.headers = headers;
    }

    public final String getHttpVerb() {
        return this.httpVerb;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public final synchronized String getHost() {
        if (this.host == null) {
            if (getEndpointAddress() != null && !getEndpointAddress().isUnresolved()) {
                this.host = getEndpointAddress().getHostName();
            }
            if (this.host == null && this.httpURI != null) {
                try {
                    this.host = new URL(this.httpURI).getHost();
                } catch (MalformedURLException e) {
                    logger.debug("Malformed URL", (Throwable) e);
                }
            }
        }
        return this.host;
    }

    public final String getHttpURI() {
        return this.httpURI;
    }

    public final Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public final void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void checkRequiredProperties(String... propNames) throws ProxyAuthException {
        StringBuilder sb = new StringBuilder();
        for (String propertyName : propNames) {
            if (this.properties.get(propertyName) == null) {
                sb.append(propertyName).append(TokenParser.SP);
            }
        }
        if (sb.length() > 0) {
            sb.append("property(ies) missing in request");
            throw new ProxyAuthException(sb.toString());
        }
    }

    public String toHttpString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHttpVerb()).append(TokenParser.SP).append(getHttpURI()).append(TokenParser.SP).append(getHttpVersion()).append(HttpProxyConstants.CRLF);
        boolean hostHeaderFound = false;
        if (getHeaders() != null) {
            for (Map.Entry<String, List<String>> header : getHeaders().entrySet()) {
                if (!hostHeaderFound) {
                    hostHeaderFound = header.getKey().equalsIgnoreCase("host");
                }
                for (String value : header.getValue()) {
                    sb.append(header.getKey()).append(": ").append(value).append(HttpProxyConstants.CRLF);
                }
            }
            if (!hostHeaderFound && getHttpVersion() == HttpProxyConstants.HTTP_1_1) {
                sb.append("Host: ").append(getHost()).append(HttpProxyConstants.CRLF);
            }
        }
        sb.append(HttpProxyConstants.CRLF);
        return sb.toString();
    }
}
