package org.apache.http.impl.execchain;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import org.apache.http.HttpException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.BackoffManager;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class BackoffStrategyExec implements ClientExecChain {
    private final BackoffManager backoffManager;
    private final ConnectionBackoffStrategy connectionBackoffStrategy;
    private final ClientExecChain requestExecutor;

    public BackoffStrategyExec(ClientExecChain requestExecutor, ConnectionBackoffStrategy connectionBackoffStrategy, BackoffManager backoffManager) {
        Args.notNull(requestExecutor, "HTTP client request executor");
        Args.notNull(connectionBackoffStrategy, "Connection backoff strategy");
        Args.notNull(backoffManager, "Backoff manager");
        this.requestExecutor = requestExecutor;
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        this.backoffManager = backoffManager;
    }

    @Override // org.apache.http.impl.execchain.ClientExecChain
    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context, HttpExecutionAware execAware) throws IOException, HttpException {
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        CloseableHttpResponse out = null;
        try {
            CloseableHttpResponse out2 = this.requestExecutor.execute(route, request, context, execAware);
            if (this.connectionBackoffStrategy.shouldBackoff(out2)) {
                this.backoffManager.backOff(route);
            } else {
                this.backoffManager.probe(route);
            }
            return out2;
        } catch (Exception e) {
            if (0 != 0) {
                out.close();
            }
            if (this.connectionBackoffStrategy.shouldBackoff((Throwable) e)) {
                this.backoffManager.backOff(route);
            }
            if (e instanceof RuntimeException) {
                Exception ex = (RuntimeException) e;
                throw ex;
            } else if (e instanceof HttpException) {
                throw e;
            } else {
                if (e instanceof IOException) {
                    Exception ex2 = (IOException) e;
                    throw ex2;
                }
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}