package org.apache.http.impl.execchain;

import android.util.Log;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.client.RedirectException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtilsHC4;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtilsHC4;

@ThreadSafe
public class RedirectExec implements ClientExecChain {
    private static final String TAG = "HttpClient";
    private final RedirectStrategy redirectStrategy;
    private final ClientExecChain requestExecutor;
    private final HttpRoutePlanner routePlanner;

    public RedirectExec(ClientExecChain requestExecutor2, HttpRoutePlanner routePlanner2, RedirectStrategy redirectStrategy2) {
        Args.notNull(requestExecutor2, "HTTP client request executor");
        Args.notNull(routePlanner2, "HTTP route planner");
        Args.notNull(redirectStrategy2, "HTTP redirect strategy");
        this.requestExecutor = requestExecutor2;
        this.routePlanner = routePlanner2;
        this.redirectStrategy = redirectStrategy2;
    }

    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context, HttpExecutionAware execAware) throws IOException, HttpException {
        CloseableHttpResponse response;
        AuthScheme authScheme;
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        List<URI> redirectLocations = context.getRedirectLocations();
        if (redirectLocations != null) {
            redirectLocations.clear();
        }
        RequestConfig config = context.getRequestConfig();
        int maxRedirects = config.getMaxRedirects() > 0 ? config.getMaxRedirects() : 50;
        HttpRoute currentRoute = route;
        HttpRequestWrapper currentRequest = request;
        int redirectCount = 0;
        while (true) {
            response = this.requestExecutor.execute(currentRoute, currentRequest, context, execAware);
            try {
                if (!config.isRedirectsEnabled() || !this.redirectStrategy.isRedirected(currentRequest, response, context)) {
                    return response;
                }
                if (redirectCount >= maxRedirects) {
                    throw new RedirectException("Maximum redirects (" + maxRedirects + ") exceeded");
                }
                redirectCount++;
                HttpRequest redirect = this.redirectStrategy.getRedirect(currentRequest, response, context);
                if (!redirect.headerIterator().hasNext()) {
                    redirect.setHeaders(request.getOriginal().getAllHeaders());
                }
                currentRequest = HttpRequestWrapper.wrap(redirect);
                if (currentRequest instanceof HttpEntityEnclosingRequest) {
                    RequestEntityProxy.enhance((HttpEntityEnclosingRequest) currentRequest);
                }
                URI uri = currentRequest.getURI();
                HttpHost newTarget = URIUtilsHC4.extractHost(uri);
                if (newTarget == null) {
                    throw new ProtocolException("Redirect URI does not specify a valid host name: " + uri);
                }
                if (!currentRoute.getTargetHost().equals(newTarget)) {
                    AuthStateHC4 targetAuthState = context.getTargetAuthState();
                    if (targetAuthState != null) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Resetting target auth state");
                        }
                        targetAuthState.reset();
                    }
                    AuthStateHC4 proxyAuthState = context.getProxyAuthState();
                    if (!(proxyAuthState == null || (authScheme = proxyAuthState.getAuthScheme()) == null || !authScheme.isConnectionBased())) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Resetting proxy auth state");
                        }
                        proxyAuthState.reset();
                    }
                }
                currentRoute = this.routePlanner.determineRoute(newTarget, currentRequest, context);
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Redirecting to '" + uri + "' via " + currentRoute);
                }
                EntityUtilsHC4.consume(response.getEntity());
                response.close();
            } catch (RuntimeException ex) {
                response.close();
                throw ex;
            } catch (IOException ex2) {
                response.close();
                throw ex2;
            } catch (HttpException ex3) {
                try {
                    EntityUtilsHC4.consume(response.getEntity());
                } catch (IOException ioex) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "I/O error while releasing connection", ioex);
                    }
                } finally {
                    response.close();
                }
                throw ex3;
            }
        }
        return response;
    }
}
