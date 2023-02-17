package org.apache.http.impl.execchain;

import android.util.Log;
import java.io.IOException;
import java.io.InterruptedIOException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

@Immutable
public class ServiceUnavailableRetryExec implements ClientExecChain {
    private static final String TAG = "HttpClient";
    private final ClientExecChain requestExecutor;
    private final ServiceUnavailableRetryStrategy retryStrategy;

    public ServiceUnavailableRetryExec(ClientExecChain requestExecutor2, ServiceUnavailableRetryStrategy retryStrategy2) {
        Args.notNull(requestExecutor2, "HTTP request executor");
        Args.notNull(retryStrategy2, "Retry strategy");
        this.requestExecutor = requestExecutor2;
        this.retryStrategy = retryStrategy2;
    }

    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context, HttpExecutionAware execAware) throws IOException, HttpException {
        Header[] origheaders = request.getAllHeaders();
        int c = 1;
        while (true) {
            CloseableHttpResponse response = this.requestExecutor.execute(route, request, context, execAware);
            try {
                if (!this.retryStrategy.retryRequest(response, c, context)) {
                    return response;
                }
                response.close();
                long nextInterval = this.retryStrategy.getRetryInterval();
                if (nextInterval > 0) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Wait for " + nextInterval);
                    }
                    Thread.sleep(nextInterval);
                }
                request.setHeaders(origheaders);
                c++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            } catch (RuntimeException ex) {
                response.close();
                throw ex;
            }
        }
    }
}
