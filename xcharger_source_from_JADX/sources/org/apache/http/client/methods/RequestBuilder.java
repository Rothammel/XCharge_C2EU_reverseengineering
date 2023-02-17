package org.apache.http.client.methods;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.Args;
import org.eclipse.paho.client.mqttv3.MqttTopic;

@NotThreadSafe
public class RequestBuilder {
    private RequestConfig config;
    private HttpEntity entity;
    private HeaderGroup headergroup;
    private String method;
    private LinkedList<NameValuePair> parameters;
    private URI uri;
    private ProtocolVersion version;

    RequestBuilder(String method2) {
        this.method = method2;
    }

    RequestBuilder() {
        this((String) null);
    }

    public static RequestBuilder create(String method2) {
        Args.notBlank(method2, "HTTP method");
        return new RequestBuilder(method2);
    }

    public static RequestBuilder get() {
        return new RequestBuilder("GET");
    }

    public static RequestBuilder head() {
        return new RequestBuilder(HttpHeadHC4.METHOD_NAME);
    }

    public static RequestBuilder post() {
        return new RequestBuilder(HttpPostHC4.METHOD_NAME);
    }

    public static RequestBuilder put() {
        return new RequestBuilder("PUT");
    }

    public static RequestBuilder delete() {
        return new RequestBuilder(HttpDeleteHC4.METHOD_NAME);
    }

    public static RequestBuilder trace() {
        return new RequestBuilder(HttpTraceHC4.METHOD_NAME);
    }

    public static RequestBuilder options() {
        return new RequestBuilder(HttpOptionsHC4.METHOD_NAME);
    }

    public static RequestBuilder copy(HttpRequest request) {
        Args.notNull(request, "HTTP request");
        return new RequestBuilder().doCopy(request);
    }

    private RequestBuilder doCopy(HttpRequest request) {
        if (request != null) {
            this.method = request.getRequestLine().getMethod();
            this.version = request.getRequestLine().getProtocolVersion();
            if (request instanceof HttpUriRequest) {
                this.uri = ((HttpUriRequest) request).getURI();
            } else {
                this.uri = URI.create(request.getRequestLine().getUri());
            }
            if (this.headergroup == null) {
                this.headergroup = new HeaderGroup();
            }
            this.headergroup.clear();
            this.headergroup.setHeaders(request.getAllHeaders());
            if (request instanceof HttpEntityEnclosingRequest) {
                this.entity = ((HttpEntityEnclosingRequest) request).getEntity();
            } else {
                this.entity = null;
            }
            if (request instanceof Configurable) {
                this.config = ((Configurable) request).getConfig();
            } else {
                this.config = null;
            }
            this.parameters = null;
        }
        return this;
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public RequestBuilder setVersion(ProtocolVersion version2) {
        this.version = version2;
        return this;
    }

    public URI getUri() {
        return this.uri;
    }

    public RequestBuilder setUri(URI uri2) {
        this.uri = uri2;
        return this;
    }

    public RequestBuilder setUri(String uri2) {
        this.uri = uri2 != null ? URI.create(uri2) : null;
        return this;
    }

    public Header getFirstHeader(String name) {
        if (this.headergroup != null) {
            return this.headergroup.getFirstHeader(name);
        }
        return null;
    }

    public Header getLastHeader(String name) {
        if (this.headergroup != null) {
            return this.headergroup.getLastHeader(name);
        }
        return null;
    }

    public Header[] getHeaders(String name) {
        if (this.headergroup != null) {
            return this.headergroup.getHeaders(name);
        }
        return null;
    }

    public RequestBuilder addHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.addHeader(header);
        return this;
    }

    public RequestBuilder addHeader(String name, String value) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public RequestBuilder removeHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.removeHeader(header);
        return this;
    }

    public RequestBuilder removeHeaders(String name) {
        if (!(name == null || this.headergroup == null)) {
            HeaderIterator i = this.headergroup.iterator();
            while (i.hasNext()) {
                if (name.equalsIgnoreCase(i.nextHeader().getName())) {
                    i.remove();
                }
            }
        }
        return this;
    }

    public RequestBuilder setHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(header);
        return this;
    }

    public RequestBuilder setHeader(String name, String value) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(new BasicHeader(name, value));
        return this;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public RequestBuilder setEntity(HttpEntity entity2) {
        this.entity = entity2;
        return this;
    }

    public List<NameValuePair> getParameters() {
        if (this.parameters != null) {
            return new ArrayList(this.parameters);
        }
        return new ArrayList();
    }

    public RequestBuilder addParameter(NameValuePair nvp) {
        Args.notNull(nvp, "Name value pair");
        if (this.parameters == null) {
            this.parameters = new LinkedList<>();
        }
        this.parameters.add(nvp);
        return this;
    }

    public RequestBuilder addParameter(String name, String value) {
        return addParameter(new BasicNameValuePair(name, value));
    }

    public RequestBuilder addParameters(NameValuePair... nvps) {
        for (NameValuePair nvp : nvps) {
            addParameter(nvp);
        }
        return this;
    }

    public RequestConfig getConfig() {
        return this.config;
    }

    public RequestBuilder setConfig(RequestConfig config2) {
        this.config = config2;
        return this;
    }

    public HttpUriRequest build() {
        HttpRequestBaseHC4 result;
        URI uri2 = this.uri != null ? this.uri : URI.create(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        HttpEntity entity2 = this.entity;
        if (this.parameters != null && !this.parameters.isEmpty()) {
            if (entity2 != null || (!HttpPostHC4.METHOD_NAME.equalsIgnoreCase(this.method) && !"PUT".equalsIgnoreCase(this.method))) {
                try {
                    uri2 = new URIBuilder(uri2).addParameters(this.parameters).build();
                } catch (URISyntaxException e) {
                }
            } else {
                entity2 = new UrlEncodedFormEntityHC4((Iterable<? extends NameValuePair>) this.parameters, Charset.forName(CharEncoding.ISO_8859_1));
            }
        }
        if (entity2 == null) {
            result = new InternalRequest(this.method);
        } else {
            InternalEntityEclosingRequest request = new InternalEntityEclosingRequest(this.method);
            request.setEntity(entity2);
            result = request;
        }
        result.setProtocolVersion(this.version);
        result.setURI(uri2);
        if (this.headergroup != null) {
            result.setHeaders(this.headergroup.getAllHeaders());
        }
        result.setConfig(this.config);
        return result;
    }

    static class InternalRequest extends HttpRequestBaseHC4 {
        private final String method;

        InternalRequest(String method2) {
            this.method = method2;
        }

        public String getMethod() {
            return this.method;
        }
    }

    static class InternalEntityEclosingRequest extends HttpEntityEnclosingRequestBaseHC4 {
        private final String method;

        InternalEntityEclosingRequest(String method2) {
            this.method = method2;
        }

        public String getMethod() {
            return this.method;
        }
    }
}
