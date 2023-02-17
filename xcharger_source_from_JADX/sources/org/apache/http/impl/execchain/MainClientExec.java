package org.apache.http.impl.execchain;

import android.util.Log;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.entity.BufferedHttpEntityHC4;
import org.apache.http.impl.auth.HttpAuthenticator;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestTargetHostHC4;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtilsHC4;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

@Immutable
public class MainClientExec implements ClientExecChain {
    private static final String TAG = "HttpClient";
    private final HttpAuthenticator authenticator = new HttpAuthenticator();
    private final HttpClientConnectionManager connManager;
    private final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final HttpProcessor proxyHttpProcessor = new ImmutableHttpProcessor(new RequestTargetHostHC4(), new RequestClientConnControl());
    private final HttpRequestExecutor requestExecutor;
    private final ConnectionReuseStrategy reuseStrategy;
    private final HttpRouteDirector routeDirector = new BasicRouteDirector();
    private final AuthenticationStrategy targetAuthStrategy;
    private final UserTokenHandler userTokenHandler;

    public MainClientExec(HttpRequestExecutor requestExecutor2, HttpClientConnectionManager connManager2, ConnectionReuseStrategy reuseStrategy2, ConnectionKeepAliveStrategy keepAliveStrategy2, AuthenticationStrategy targetAuthStrategy2, AuthenticationStrategy proxyAuthStrategy2, UserTokenHandler userTokenHandler2) {
        Args.notNull(requestExecutor2, "HTTP request executor");
        Args.notNull(connManager2, "Client connection manager");
        Args.notNull(reuseStrategy2, "Connection reuse strategy");
        Args.notNull(keepAliveStrategy2, "Connection keep alive strategy");
        Args.notNull(targetAuthStrategy2, "Target authentication strategy");
        Args.notNull(proxyAuthStrategy2, "Proxy authentication strategy");
        Args.notNull(userTokenHandler2, "User token handler");
        this.requestExecutor = requestExecutor2;
        this.connManager = connManager2;
        this.reuseStrategy = reuseStrategy2;
        this.keepAliveStrategy = keepAliveStrategy2;
        this.targetAuthStrategy = targetAuthStrategy2;
        this.proxyAuthStrategy = proxyAuthStrategy2;
        this.userTokenHandler = userTokenHandler2;
    }

    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context, HttpExecutionAware execAware) throws IOException, HttpException {
        HttpResponse response;
        String s;
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        AuthStateHC4 targetAuthState = context.getTargetAuthState();
        if (targetAuthState == null) {
            targetAuthState = new AuthStateHC4();
            context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, targetAuthState);
        }
        AuthStateHC4 proxyAuthState = context.getProxyAuthState();
        if (proxyAuthState == null) {
            proxyAuthState = new AuthStateHC4();
            context.setAttribute(HttpClientContext.PROXY_AUTH_STATE, proxyAuthState);
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            RequestEntityProxy.enhance((HttpEntityEnclosingRequest) request);
        }
        Object userToken = context.getUserToken();
        ConnectionRequest connRequest = this.connManager.requestConnection(route, userToken);
        if (execAware != null) {
            if (execAware.isAborted()) {
                connRequest.cancel();
                throw new RequestAbortedException("Request aborted");
            }
            execAware.setCancellable(connRequest);
        }
        RequestConfig config = context.getRequestConfig();
        try {
            int timeout = config.getConnectionRequestTimeout();
            if (timeout <= 0) {
                timeout = 0;
            }
            HttpClientConnection managedConn = connRequest.get((long) timeout, TimeUnit.MILLISECONDS);
            context.setAttribute(HttpCoreContext.HTTP_CONNECTION, managedConn);
            if (config.isStaleConnectionCheckEnabled() && managedConn.isOpen()) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Stale connection check");
                }
                if (managedConn.isStale()) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Stale connection detected");
                    }
                    managedConn.close();
                }
            }
            ConnectionHolder connHolder = new ConnectionHolder(this.connManager, managedConn);
            if (execAware != null) {
                try {
                    execAware.setCancellable(connHolder);
                } catch (ConnectionShutdownException ex) {
                    InterruptedIOException interruptedIOException = new InterruptedIOException("Connection has been shut down");
                    interruptedIOException.initCause(ex);
                    throw interruptedIOException;
                } catch (HttpException ex2) {
                    connHolder.abortConnection();
                    throw ex2;
                } catch (IOException ex3) {
                    connHolder.abortConnection();
                    throw ex3;
                } catch (RuntimeException ex4) {
                    connHolder.abortConnection();
                    throw ex4;
                }
            }
            int execCount = 1;
            while (true) {
                if (execCount <= 1 || RequestEntityProxy.isRepeatable(request)) {
                    if (execAware != null) {
                        if (execAware.isAborted()) {
                            throw new RequestAbortedException("Request aborted");
                        }
                    }
                    if (!managedConn.isOpen()) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Opening connection " + route);
                        }
                        try {
                            establishRoute(proxyAuthState, managedConn, route, request, context);
                        } catch (TunnelRefusedException ex5) {
                            if (Log.isLoggable(TAG, 3)) {
                                Log.d(TAG, ex5.getMessage());
                            }
                            response = ex5.getResponse();
                        }
                    }
                    int timeout2 = config.getSocketTimeout();
                    if (timeout2 >= 0) {
                        managedConn.setSocketTimeout(timeout2);
                    }
                    if (execAware == null || !execAware.isAborted()) {
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Executing request " + request.getRequestLine());
                        }
                        if (!request.containsHeader("Authorization")) {
                            if (Log.isLoggable(TAG, 3)) {
                                Log.d(TAG, "Target auth state: " + targetAuthState.getState());
                            }
                            this.authenticator.generateAuthResponse(request, targetAuthState, context);
                        }
                        if (!request.containsHeader(HttpHeaders.PROXY_AUTHORIZATION) && !route.isTunnelled()) {
                            if (Log.isLoggable(TAG, 3)) {
                                Log.d(TAG, "Proxy auth state: " + proxyAuthState.getState());
                            }
                            this.authenticator.generateAuthResponse(request, proxyAuthState, context);
                        }
                        response = this.requestExecutor.execute(request, managedConn, context);
                        if (this.reuseStrategy.keepAlive(response, context)) {
                            long duration = this.keepAliveStrategy.getKeepAliveDuration(response, context);
                            if (Log.isLoggable(TAG, 3)) {
                                if (duration > 0) {
                                    s = "for " + duration + StringUtils.SPACE + TimeUnit.MILLISECONDS;
                                } else {
                                    s = "indefinitely";
                                }
                                Log.d(TAG, "Connection can be kept alive " + s);
                            }
                            connHolder.setValidFor(duration, TimeUnit.MILLISECONDS);
                            connHolder.markReusable();
                        } else {
                            connHolder.markNonReusable();
                        }
                        if (!needAuthentication(targetAuthState, proxyAuthState, route, response, context)) {
                            break;
                        }
                        HttpEntity entity = response.getEntity();
                        if (connHolder.isReusable()) {
                            EntityUtilsHC4.consume(entity);
                        } else {
                            managedConn.close();
                            if (proxyAuthState.getState() == AuthProtocolState.SUCCESS && proxyAuthState.getAuthScheme() != null && proxyAuthState.getAuthScheme().isConnectionBased()) {
                                if (Log.isLoggable(TAG, 3)) {
                                    Log.d(TAG, "Resetting proxy auth state");
                                }
                                proxyAuthState.reset();
                            }
                            if (targetAuthState.getState() == AuthProtocolState.SUCCESS && targetAuthState.getAuthScheme() != null && targetAuthState.getAuthScheme().isConnectionBased()) {
                                if (Log.isLoggable(TAG, 3)) {
                                    Log.d(TAG, "Resetting target auth state");
                                }
                                targetAuthState.reset();
                            }
                        }
                        HttpRequest original = request.getOriginal();
                        if (!original.containsHeader("Authorization")) {
                            request.removeHeaders("Authorization");
                        }
                        if (!original.containsHeader(HttpHeaders.PROXY_AUTHORIZATION)) {
                            request.removeHeaders(HttpHeaders.PROXY_AUTHORIZATION);
                        }
                        execCount++;
                    } else {
                        throw new RequestAbortedException("Request aborted");
                    }
                } else {
                    throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity.");
                }
            }
            if (userToken == null) {
                userToken = this.userTokenHandler.getUserToken(context);
                context.setAttribute(HttpClientContext.USER_TOKEN, userToken);
            }
            if (userToken != null) {
                connHolder.setState(userToken);
            }
            HttpEntity entity2 = response.getEntity();
            if (entity2 != null && entity2.isStreaming()) {
                return new HttpResponseProxy(response, connHolder);
            }
            connHolder.releaseConnection();
            return new HttpResponseProxy(response, (ConnectionHolder) null);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RequestAbortedException("Request aborted", interrupted);
        } catch (ExecutionException ex6) {
            Throwable cause = ex6.getCause();
            if (cause == null) {
                cause = ex6;
            }
            throw new RequestAbortedException("Request execution failed", cause);
        }
    }

    /* access modifiers changed from: package-private */
    public void establishRoute(AuthStateHC4 proxyAuthState, HttpClientConnection managedConn, HttpRoute route, HttpRequest request, HttpClientContext context) throws HttpException, IOException {
        int step;
        int timeout = context.getRequestConfig().getConnectTimeout();
        RouteTracker tracker = new RouteTracker(route);
        do {
            HttpRoute fact = tracker.toRoute();
            step = this.routeDirector.nextStep(route, fact);
            switch (step) {
                case -1:
                    throw new HttpException("Unable to establish route: planned = " + route + "; current = " + fact);
                case 0:
                    this.connManager.routeComplete(managedConn, route, context);
                    continue;
                case 1:
                    this.connManager.connect(managedConn, route, timeout > 0 ? timeout : 0, context);
                    tracker.connectTarget(route.isSecure());
                    continue;
                case 2:
                    this.connManager.connect(managedConn, route, timeout > 0 ? timeout : 0, context);
                    tracker.connectProxy(route.getProxyHost(), false);
                    continue;
                case 3:
                    boolean secure = createTunnelToTarget(proxyAuthState, managedConn, route, request, context);
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Tunnel to target created.");
                    }
                    tracker.tunnelTarget(secure);
                    continue;
                case 4:
                    int hop = fact.getHopCount() - 1;
                    boolean secure2 = createTunnelToProxy(route, hop, context);
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Tunnel to proxy created.");
                    }
                    tracker.tunnelProxy(route.getHopTarget(hop), secure2);
                    continue;
                case 5:
                    this.connManager.upgrade(managedConn, route, context);
                    tracker.layerProtocol(route.isSecure());
                    continue;
                default:
                    throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
            }
        } while (step > 0);
    }

    /* JADX WARNING: type inference failed for: r3v5, types: [java.lang.Throwable, org.apache.http.impl.execchain.TunnelRefusedException] */
    private boolean createTunnelToTarget(AuthStateHC4 proxyAuthState, HttpClientConnection managedConn, HttpRoute route, HttpRequest request, HttpClientContext context) throws HttpException, IOException {
        RequestConfig config = context.getRequestConfig();
        int timeout = config.getConnectTimeout();
        HttpHost target = route.getTargetHost();
        HttpHost proxy = route.getProxyHost();
        HttpResponse response = null;
        HttpRequest connect = new BasicHttpRequest(HttpProxyConstants.CONNECT, target.toHostString(), request.getProtocolVersion());
        this.requestExecutor.preProcess(connect, this.proxyHttpProcessor, context);
        while (response == null) {
            if (!managedConn.isOpen()) {
                this.connManager.connect(managedConn, route, timeout > 0 ? timeout : 0, context);
            }
            connect.removeHeaders(HttpHeaders.PROXY_AUTHORIZATION);
            this.authenticator.generateAuthResponse(connect, proxyAuthState, context);
            response = this.requestExecutor.execute(connect, managedConn, context);
            if (response.getStatusLine().getStatusCode() < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " + response.getStatusLine());
            } else if (config.isAuthenticationEnabled() && this.authenticator.isAuthenticationRequested(proxy, response, this.proxyAuthStrategy, proxyAuthState, context) && this.authenticator.handleAuthChallenge(proxy, response, this.proxyAuthStrategy, proxyAuthState, context)) {
                if (this.reuseStrategy.keepAlive(response, context)) {
                    if (Log.isLoggable(TAG, 3)) {
                        Log.d(TAG, "Connection kept alive");
                    }
                    EntityUtilsHC4.consume(response.getEntity());
                } else {
                    managedConn.close();
                }
                response = null;
            }
        }
        if (response.getStatusLine().getStatusCode() <= 299) {
            return false;
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            response.setEntity(new BufferedHttpEntityHC4(entity));
        }
        managedConn.close();
        throw new TunnelRefusedException("CONNECT refused by proxy: " + response.getStatusLine(), response);
    }

    private boolean createTunnelToProxy(HttpRoute route, int hop, HttpClientContext context) throws HttpException {
        throw new HttpException("Proxy chains are not supported.");
    }

    private boolean needAuthentication(AuthStateHC4 targetAuthState, AuthStateHC4 proxyAuthState, HttpRoute route, HttpResponse response, HttpClientContext context) {
        if (context.getRequestConfig().isAuthenticationEnabled()) {
            HttpHost target = context.getTargetHost();
            if (target == null) {
                target = route.getTargetHost();
            }
            if (target.getPort() < 0) {
                target = new HttpHost(target.getHostName(), route.getTargetHost().getPort(), target.getSchemeName());
            }
            boolean targetAuthRequested = this.authenticator.isAuthenticationRequested(target, response, this.targetAuthStrategy, targetAuthState, context);
            HttpHost proxy = route.getProxyHost();
            if (proxy == null) {
                proxy = route.getTargetHost();
            }
            boolean proxyAuthRequested = this.authenticator.isAuthenticationRequested(proxy, response, this.proxyAuthStrategy, proxyAuthState, context);
            if (targetAuthRequested) {
                return this.authenticator.handleAuthChallenge(target, response, this.targetAuthStrategy, targetAuthState, context);
            } else if (proxyAuthRequested) {
                return this.authenticator.handleAuthChallenge(proxy, response, this.proxyAuthStrategy, proxyAuthState, context);
            }
        }
        return false;
    }
}
