package com.alibaba.sdk.android.oss.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
abstract class HttpMessage {
    private InputStream content;
    private long contentLength;
    private Map<String, String> headers = new HashMap();
    private String stringBody;

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (this.headers == null) {
            this.headers = new HashMap();
        }
        if (this.headers != null && this.headers.size() > 0) {
            this.headers.clear();
        }
        this.headers.putAll(headers);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public InputStream getContent() {
        return this.content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getStringBody() {
        return this.stringBody;
    }

    public void setStringBody(String stringBody) {
        this.stringBody = stringBody;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void close() throws IOException {
        if (this.content != null) {
            this.content.close();
            this.content = null;
        }
    }
}