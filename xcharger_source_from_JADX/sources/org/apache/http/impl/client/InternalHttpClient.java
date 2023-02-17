package org.apache.http.impl.client;

import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.params.HttpClientParamConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContextHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@ThreadSafe
class InternalHttpClient extends CloseableHttpClient {
    private static final String TAG = "HttpClient";
    private final Lookup<AuthSchemeProvider> authSchemeRegistry;
    private final List<Closeable> closeables;
    /* access modifiers changed from: private */
    public final HttpClientConnectionManager connManager;
    private final Lookup<CookieSpecProvider> cookieSpecRegistry;
    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;
    private final RequestConfig defaultConfig;
    private final ClientExecChain execChain;
    private final HttpRoutePlanner routePlanner;

    public InternalHttpClient(ClientExecChain execChain2, HttpClientConnectionManager connManager2, HttpRoutePlanner routePlanner2, Lookup<CookieSpecProvider> cookieSpecRegistry2, Lookup<AuthSchemeProvider> authSchemeRegistry2, CookieStore cookieStore2, CredentialsProvider credentialsProvider2, RequestConfig defaultConfig2, List<Closeable> closeables2) {
        Args.notNull(execChain2, "HTTP client exec chain");
        Args.notNull(connManager2, "HTTP connection manager");
        Args.notNull(routePlanner2, "HTTP route planner");
        this.execChain = execChain2;
        this.connManager = connManager2;
        this.routePlanner = routePlanner2;
        this.cookieSpecRegistry = cookieSpecRegistry2;
        this.authSchemeRegistry = authSchemeRegistry2;
        this.cookieStore = cookieStore2;
        this.credentialsProvider = credentialsProvider2;
        this.defaultConfig = defaultConfig2;
        this.closeables = closeables2;
    }

    private HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        HttpHost host = target;
        if (host == null) {
            host = (HttpHost) request.getParams().getParameter("http.default-host");
        }
        return this.routePlanner.determineRoute(host, request, context);
    }

    private void setupContext(HttpClientContext context) {
        if (context.getAttribute(HttpClientContext.TARGET_AUTH_STATE) == null) {
            context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, new AuthStateHC4());
        }
        if (context.getAttribute(HttpClientContext.PROXY_AUTH_STATE) == null) {
            context.setAttribute(HttpClientContext.PROXY_AUTH_STATE, new AuthStateHC4());
        }
        if (context.getAttribute(HttpClientContext.AUTHSCHEME_REGISTRY) == null) {
            context.setAttribute(HttpClientContext.AUTHSCHEME_REGISTRY, this.authSchemeRegistry);
        }
        if (context.getAttribute(HttpClientContext.COOKIESPEC_REGISTRY) == null) {
            context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);
        }
        if (context.getAttribute(HttpClientContext.COOKIE_STORE) == null) {
            context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        }
        if (context.getAttribute(HttpClientContext.CREDS_PROVIDER) == null) {
            context.setAttribute(HttpClientContext.CREDS_PROVIDER, this.credentialsProvider);
        }
        if (context.getAttribute(HttpClientContext.REQUEST_CONFIG) == null) {
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, this.defaultConfig);
        }
    }

    /* access modifiers changed from: protected */
    public CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        Args.notNull(request, "HTTP request");
        HttpExecutionAware execAware = null;
        if (request instanceof HttpExecutionAware) {
            execAware = (HttpExecutionAware) request;
        }
        try {
            HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(request);
            if (context == null) {
                context = new BasicHttpContextHC4();
            }
            HttpClientContext localcontext = HttpClientContext.adapt(context);
            RequestConfig config = null;
            if (request instanceof Configurable) {
                config = ((Configurable) request).getConfig();
            }
            if (config == null) {
                config = HttpClientParamConfig.getRequestConfig(request.getParams());
            }
            if (config != null) {
                localcontext.setRequestConfig(config);
            }
            setupContext(localcontext);
            return this.execChain.execute(determineRoute(target, wrapper, localcontext), wrapper, localcontext, execAware);
        } catch (HttpException httpException) {
            throw new ClientProtocolException(httpException);
        }
    }

    public void close() {
        this.connManager.shutdown();
        if (this.closeables != null) {
            for (Closeable closeable : this.closeables) {
                try {
                    closeable.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }
            }
        }
    }

    public HttpParams getParams() {
        throw new UnsupportedOperationException();
    }

    public ClientConnectionManager getConnectionManager() {
        return new ClientConnectionManager() {
            public void shutdown() {
                InternalHttpClient.this.connManager.shutdown();
            }

            public ClientConnectionRequest requestConnection(HttpRoute route, Object state) {
                throw new UnsupportedOperationException();
            }

            public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {
                throw new UnsupportedOperationException();
            }

            public SchemeRegistry getSchemeRegistry() {
                throw new UnsupportedOperationException();
            }

            public void closeIdleConnections(long idletime, TimeUnit tunit) {
                InternalHttpClient.this.connManager.closeIdleConnections(idletime, tunit);
            }

            public void closeExpiredConnections() {
                InternalHttpClient.this.connManager.closeExpiredConnections();
            }
        };
    }
}