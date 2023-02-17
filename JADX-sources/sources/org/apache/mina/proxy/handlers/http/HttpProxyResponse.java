package org.apache.mina.proxy.handlers.http;

import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class HttpProxyResponse {
    private String body;
    private final Map<String, List<String>> headers;
    private final String httpVersion;
    private final int statusCode;
    private final String statusLine;

    /* JADX INFO: Access modifiers changed from: protected */
    public HttpProxyResponse(String httpVersion, String statusLine, Map<String, List<String>> headers) {
        this.httpVersion = httpVersion;
        this.statusLine = statusLine;
        this.statusCode = statusLine.charAt(0) == ' ' ? Integer.parseInt(statusLine.substring(1, 4)) : Integer.parseInt(statusLine.substring(0, 3));
        this.headers = headers;
    }

    public final String getHttpVersion() {
        return this.httpVersion;
    }

    public final int getStatusCode() {
        return this.statusCode;
    }

    public final String getStatusLine() {
        return this.statusLine;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public final Map<String, List<String>> getHeaders() {
        return this.headers;
    }
}
