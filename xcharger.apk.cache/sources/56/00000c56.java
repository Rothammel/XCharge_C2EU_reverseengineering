package org.apache.http.impl.client;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpOptionsHC4;
import org.apache.http.client.methods.HttpTraceHC4;

@Immutable
/* loaded from: classes.dex */
public class StandardHttpRequestRetryHandler extends DefaultHttpRequestRetryHandlerHC4 {
    private final Map<String, Boolean> idempotentMethods;

    public StandardHttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled);
        this.idempotentMethods = new ConcurrentHashMap();
        this.idempotentMethods.put("GET", Boolean.TRUE);
        this.idempotentMethods.put(HttpHeadHC4.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put("PUT", Boolean.TRUE);
        this.idempotentMethods.put(HttpDeleteHC4.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpOptionsHC4.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpTraceHC4.METHOD_NAME, Boolean.TRUE);
    }

    public StandardHttpRequestRetryHandler() {
        this(3, false);
    }

    @Override // org.apache.http.impl.client.DefaultHttpRequestRetryHandlerHC4
    protected boolean handleAsIdempotent(HttpRequest request) {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        Boolean b = this.idempotentMethods.get(method);
        return b != null && b.booleanValue();
    }
}