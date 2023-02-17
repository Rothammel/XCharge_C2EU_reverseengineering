package org.apache.mina.proxy.handlers.http;

import java.util.List;
import java.util.Map;

public class HttpProxyResponse {
    private String body;
    private final Map<String, List<String>> headers;
    private final String httpVersion;
    private final int statusCode;
    private final String statusLine;

    protected HttpProxyResponse(String httpVersion2, String statusLine2, Map<String, List<String>> headers2) {
        int parseInt;
        this.httpVersion = httpVersion2;
        this.statusLine = statusLine2;
        if (statusLine2.charAt(0) == ' ') {
            parseInt = Integer.parseInt(statusLine2.substring(1, 4));
        } else {
            parseInt = Integer.parseInt(statusLine2.substring(0, 3));
        }
        this.statusCode = parseInt;
        this.headers = headers2;
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

    public void setBody(String body2) {
        this.body = body2;
    }

    public final Map<String, List<String>> getHeaders() {
        return this.headers;
    }
}
