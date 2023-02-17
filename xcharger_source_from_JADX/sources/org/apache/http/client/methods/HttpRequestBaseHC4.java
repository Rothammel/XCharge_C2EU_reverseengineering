package org.apache.http.client.methods;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpProtocolParams;
import org.eclipse.paho.client.mqttv3.MqttTopic;

@NotThreadSafe
public abstract class HttpRequestBaseHC4 extends AbstractExecutionAwareRequest implements HttpUriRequest, Configurable {
    private RequestConfig config;
    private URI uri;
    private ProtocolVersion version;

    public abstract String getMethod();

    public void setProtocolVersion(ProtocolVersion version2) {
        this.version = version2;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.version != null ? this.version : HttpProtocolParams.getVersion(getParams());
    }

    public URI getURI() {
        return this.uri;
    }

    public RequestLine getRequestLine() {
        String method = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        URI uri2 = getURI();
        String uritext = null;
        if (uri2 != null) {
            uritext = uri2.toASCIIString();
        }
        if (uritext == null || uritext.length() == 0) {
            uritext = MqttTopic.TOPIC_LEVEL_SEPARATOR;
        }
        return new BasicRequestLine(method, uritext, ver);
    }

    public RequestConfig getConfig() {
        return this.config;
    }

    public void setConfig(RequestConfig config2) {
        this.config = config2;
    }

    public void setURI(URI uri2) {
        this.uri = uri2;
    }

    public void started() {
    }

    public void releaseConnection() {
        reset();
    }

    public String toString() {
        return String.valueOf(getMethod()) + StringUtils.SPACE + getURI() + StringUtils.SPACE + getProtocolVersion();
    }
}
