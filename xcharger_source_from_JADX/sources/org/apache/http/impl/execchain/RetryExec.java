package org.apache.http.impl.execchain;

import android.util.Log;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

@Immutable
public class RetryExec implements ClientExecChain {
    private static final String TAG = "HttpClient";
    private final ClientExecChain requestExecutor;
    private final HttpRequestRetryHandler retryHandler;

    public RetryExec(ClientExecChain requestExecutor2, HttpRequestRetryHandler retryHandler2) {
        Args.notNull(requestExecutor2, "HTTP request executor");
        Args.notNull(retryHandler2, "HTTP request retry handler");
        this.requestExecutor = requestExecutor2;
        this.retryHandler = retryHandler2;
    }

    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context, HttpExecutionAware execAware) throws IOException, HttpException {
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        Header[] origheaders = request.getAllHeaders();
        int execCount = 1;
        while (true) {
            try {
                return this.requestExecutor.execute(route, request, context, execAware);
            } catch (IOException ex) {
                if (execAware != null && execAware.isAborted()) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Request has been aborted");
                    }
                    throw ex;
                } else if (this.retryHandler.retryRequest(ex, execCount, context)) {
                    if (Log.isLoggable(TAG, 4)) {
                        Log.i(TAG, "I/O exception (" + ex.getClass().getName() + ") caught when processing request to " + route + ": " + ex.getMessage());
                    }
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, ex.getMessage(), ex);
                    }
                    if (!RequestEntityProxy.isRepeatable(request)) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Cannot retry non-repeatable request");
                        }
                        new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity").initCause(ex);
                    }
                    request.setHeaders(origheaders);
                    if (Log.isLoggable(TAG, 4)) {
                        Log.i(TAG, "Retrying request to " + route);
                    }
                    execCount++;
                } else if (ex instanceof NoHttpResponseException) {
                    NoHttpResponseException updatedex = new NoHttpResponseException(String.valueOf(route.getTargetHost().toHostString()) + " failed to respond");
                    updatedex.setStackTrace(ex.getStackTrace());
                    throw updatedex;
                } else {
                    throw ex;
                }
            }
        }
    }
}
