package org.apache.http.client.methods;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpParams;
import org.eclipse.paho.client.mqttv3.MqttTopic;

@NotThreadSafe
/* loaded from: classes.dex */
public class HttpRequestWrapper extends AbstractHttpMessage implements HttpUriRequest {
    private final String method;
    private final HttpRequest original;
    private URI uri;
    private ProtocolVersion version;

    /* synthetic */ HttpRequestWrapper(HttpRequest httpRequest, HttpRequestWrapper httpRequestWrapper) {
        this(httpRequest);
    }

    private HttpRequestWrapper(HttpRequest request) {
        this.original = request;
        this.version = this.original.getRequestLine().getProtocolVersion();
        this.method = this.original.getRequestLine().getMethod();
        if (request instanceof HttpUriRequest) {
            this.uri = ((HttpUriRequest) request).getURI();
        } else {
            this.uri = null;
        }
        setHeaders(request.getAllHeaders());
    }

    public ProtocolVersion getProtocolVersion() {
        return this.version != null ? this.version : this.original.getProtocolVersion();
    }

    public void setProtocolVersion(ProtocolVersion version) {
        this.version = version;
    }

    public URI getURI() {
        return this.uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return this.method;
    }

    public void abort() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean isAborted() {
        return false;
    }

    public RequestLine getRequestLine() {
        String requestUri;
        if (this.uri != null) {
            requestUri = this.uri.toASCIIString();
        } else {
            requestUri = this.original.getRequestLine().getUri();
        }
        if (requestUri == null || requestUri.length() == 0) {
            requestUri = MqttTopic.TOPIC_LEVEL_SEPARATOR;
        }
        return new BasicRequestLine(this.method, requestUri, getProtocolVersion());
    }

    public HttpRequest getOriginal() {
        return this.original;
    }

    public String toString() {
        return getRequestLine() + StringUtils.SPACE + this.headergroup;
    }

    /* loaded from: classes.dex */
    static class HttpEntityEnclosingRequestWrapper extends HttpRequestWrapper implements HttpEntityEnclosingRequest {
        private HttpEntity entity;

        public HttpEntityEnclosingRequestWrapper(HttpEntityEnclosingRequest request) {
            super(request, null);
            this.entity = request.getEntity();
        }

        public HttpEntity getEntity() {
            return this.entity;
        }

        public void setEntity(HttpEntity entity) {
            this.entity = entity;
        }

        public boolean expectContinue() {
            Header expect = getFirstHeader(HttpHeaders.EXPECT);
            return expect != null && "100-continue".equalsIgnoreCase(expect.getValue());
        }
    }

    public static HttpRequestWrapper wrap(HttpRequest request) {
        if (request == null) {
            return null;
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            return new HttpEntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) request);
        }
        return new HttpRequestWrapper(request);
    }

    @Deprecated
    public HttpParams getParams() {
        if (this.params == null) {
            this.params = this.original.getParams().copy();
        }
        return this.params;
    }
}
