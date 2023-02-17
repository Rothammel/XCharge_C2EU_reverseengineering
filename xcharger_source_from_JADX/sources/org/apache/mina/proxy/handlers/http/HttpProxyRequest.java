package org.apache.mina.proxy.handlers.http;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyRequest extends ProxyRequest {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) HttpProxyRequest.class);
    private Map<String, List<String>> headers;
    private String host;
    private final String httpURI;
    private final String httpVerb;
    private String httpVersion;
    private transient Map<String, String> properties;

    public HttpProxyRequest(InetSocketAddress endpointAddress) {
        this(endpointAddress, HttpProxyConstants.HTTP_1_0, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion2) {
        this(endpointAddress, httpVersion2, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion2, Map<String, List<String>> headers2) {
        this.httpVerb = HttpProxyConstants.CONNECT;
        if (!endpointAddress.isUnresolved()) {
            this.httpURI = endpointAddress.getHostName() + ":" + endpointAddress.getPort();
        } else {
            this.httpURI = endpointAddress.getAddress().getHostAddress() + ":" + endpointAddress.getPort();
        }
        this.httpVersion = httpVersion2;
        this.headers = headers2;
    }

    public HttpProxyRequest(String httpURI2) {
        this("GET", httpURI2, HttpProxyConstants.HTTP_1_0, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(String httpURI2, String httpVersion2) {
        this("GET", httpURI2, httpVersion2, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(String httpVerb2, String httpURI2, String httpVersion2) {
        this(httpVerb2, httpURI2, httpVersion2, (Map<String, List<String>>) null);
    }

    public HttpProxyRequest(String httpVerb2, String httpURI2, String httpVersion2, Map<String, List<String>> headers2) {
        this.httpVerb = httpVerb2;
        this.httpURI = httpURI2;
        this.httpVersion = httpVersion2;
        this.headers = headers2;
    }

    public final String getHttpVerb() {
        return this.httpVerb;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public void setHttpVersion(String httpVersion2) {
        this.httpVersion = httpVersion2;
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

    public final void setHeaders(Map<String, List<String>> headers2) {
        this.headers = headers2;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties2) {
        this.properties = properties2;
    }

    /* JADX WARNING: type inference failed for: r2v4, types: [java.lang.Throwable, org.apache.mina.proxy.ProxyAuthException] */
    public void checkRequiredProperties(String... propNames) throws ProxyAuthException {
        StringBuilder sb = new StringBuilder();
        for (String propertyName : propNames) {
            if (this.properties.get(propertyName) == null) {
                sb.append(propertyName).append(TokenParser.f168SP);
            }
        }
        if (sb.length() > 0) {
            sb.append("property(ies) missing in request");
            throw new ProxyAuthException(sb.toString());
        }
    }

    public String toHttpString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHttpVerb()).append(TokenParser.f168SP).append(getHttpURI()).append(TokenParser.f168SP).append(getHttpVersion()).append(HttpProxyConstants.CRLF);
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
