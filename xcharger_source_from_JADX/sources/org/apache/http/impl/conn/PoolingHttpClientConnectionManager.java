package org.apache.http.impl.conn;

import android.util.Log;
import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.pool.ConnFactory;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@ThreadSafe
public class PoolingHttpClientConnectionManager implements HttpClientConnectionManager, ConnPoolControl<HttpRoute>, Closeable {
    private static final String TAG = "HttpClient";
    private final ConfigData configData;
    private final HttpClientConnectionOperator connectionOperator;
    private final AtomicBoolean isShutDown;
    private final CPool pool;

    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.create().register(ConsoleSetting.SCHEMA_HTTP, PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
    }

    public PoolingHttpClientConnectionManager() {
        this(getDefaultRegistry());
    }

    public PoolingHttpClientConnectionManager(long timeToLive, TimeUnit tunit) {
        this(getDefaultRegistry(), (HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>) null, (SchemePortResolver) null, (DnsResolver) null, timeToLive, tunit);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        this(socketFactoryRegistry, (HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>) null, (DnsResolver) null);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, DnsResolver dnsResolver) {
        this(socketFactoryRegistry, (HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>) null, dnsResolver);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
        this(socketFactoryRegistry, connFactory, (DnsResolver) null);
    }

    public PoolingHttpClientConnectionManager(HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
        this(getDefaultRegistry(), connFactory, (DnsResolver) null);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, DnsResolver dnsResolver) {
        this(socketFactoryRegistry, connFactory, (SchemePortResolver) null, dnsResolver, -1, TimeUnit.MILLISECONDS);
    }

    public PoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory, SchemePortResolver schemePortResolver, DnsResolver dnsResolver, long timeToLive, TimeUnit tunit) {
        this.configData = new ConfigData();
        this.pool = new CPool(new InternalConnectionFactory(this.configData, connFactory), 2, 20, timeToLive, tunit);
        this.connectionOperator = new HttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver);
        this.isShutDown = new AtomicBoolean(false);
    }

    PoolingHttpClientConnectionManager(CPool pool2, Lookup<ConnectionSocketFactory> socketFactoryRegistry, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        this.configData = new ConfigData();
        this.pool = pool2;
        this.connectionOperator = new HttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver);
        this.isShutDown = new AtomicBoolean(false);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        shutdown();
    }

    private String format(HttpRoute route, Object state) {
        StringBuilder buf = new StringBuilder();
        buf.append("[route: ").append(route).append("]");
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    private String formatStats(HttpRoute route) {
        StringBuilder buf = new StringBuilder();
        PoolStats totals = this.pool.getTotalStats();
        PoolStats stats = this.pool.getStats(route);
        buf.append("[total kept alive: ").append(totals.getAvailable()).append("; ");
        buf.append("route allocated: ").append(stats.getLeased() + stats.getAvailable());
        buf.append(" of ").append(stats.getMax()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        return buf.toString();
    }

    private String format(CPoolEntry entry) {
        StringBuilder buf = new StringBuilder();
        buf.append("[id: ").append(entry.getId()).append("]");
        buf.append("[route: ").append(entry.getRoute()).append("]");
        Object state = entry.getState();
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    public ConnectionRequest requestConnection(HttpRoute route, Object state) {
        Args.notNull(route, "HTTP route");
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Connection request: " + format(route, state) + formatStats(route));
        }
        final Future<CPoolEntry> future = this.pool.lease(route, state, (FutureCallback) null);
        return new ConnectionRequest() {
            public boolean cancel() {
                return future.cancel(true);
            }

            public HttpClientConnection get(long timeout, TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                return PoolingHttpClientConnectionManager.this.leaseConnection(future, timeout, tunit);
            }
        };
    }

    /* access modifiers changed from: protected */
    public HttpClientConnection leaseConnection(Future<CPoolEntry> future, long timeout, TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        try {
            CPoolEntry entry = future.get(timeout, tunit);
            if (entry == null || future.isCancelled()) {
                throw new InterruptedException();
            }
            Asserts.check(entry.getConnection() != null, "Pool entry with no connection");
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Connection leased: " + format(entry) + formatStats((HttpRoute) entry.getRoute()));
            }
            return CPoolProxy.newProxy(entry);
        } catch (TimeoutException e) {
            throw new ConnectionPoolTimeoutException("Timeout waiting for connection from pool");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        return;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void releaseConnection(org.apache.http.HttpClientConnection r10, java.lang.Object r11, long r12, java.util.concurrent.TimeUnit r14) {
        /*
            r9 = this;
            java.lang.String r3 = "Managed connection"
            org.apache.http.util.Args.notNull(r10, r3)
            monitor-enter(r10)
            org.apache.http.impl.conn.CPoolEntry r1 = org.apache.http.impl.conn.CPoolProxy.detach(r10)     // Catch:{ all -> 0x00b3 }
            if (r1 != 0) goto L_0x000e
            monitor-exit(r10)     // Catch:{ all -> 0x00b3 }
        L_0x000d:
            return
        L_0x000e:
            java.lang.Object r0 = r1.getConnection()     // Catch:{ all -> 0x00b3 }
            org.apache.http.conn.ManagedHttpClientConnection r0 = (org.apache.http.conn.ManagedHttpClientConnection) r0     // Catch:{ all -> 0x00b3 }
            boolean r3 = r0.isOpen()     // Catch:{ all -> 0x00bd }
            if (r3 == 0) goto L_0x006f
            r1.setState(r11)     // Catch:{ all -> 0x00bd }
            if (r14 == 0) goto L_0x00b6
        L_0x001f:
            r1.updateExpiry(r12, r14)     // Catch:{ all -> 0x00bd }
            java.lang.String r3 = "HttpClient"
            r4 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r4)     // Catch:{ all -> 0x00bd }
            if (r3 == 0) goto L_0x006f
            r4 = 0
            int r3 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1))
            if (r3 <= 0) goto L_0x00ba
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bd }
            java.lang.String r4 = "for "
            r3.<init>(r4)     // Catch:{ all -> 0x00bd }
            double r4 = (double) r12     // Catch:{ all -> 0x00bd }
            r6 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r4 = r4 / r6
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x00bd }
            java.lang.String r4 = " seconds"
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x00bd }
            java.lang.String r2 = r3.toString()     // Catch:{ all -> 0x00bd }
        L_0x004d:
            java.lang.String r3 = "HttpClient"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bd }
            java.lang.String r5 = "Connection "
            r4.<init>(r5)     // Catch:{ all -> 0x00bd }
            java.lang.String r5 = r9.format(r1)     // Catch:{ all -> 0x00bd }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x00bd }
            java.lang.String r5 = " can be kept alive "
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x00bd }
            java.lang.StringBuilder r4 = r4.append(r2)     // Catch:{ all -> 0x00bd }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00bd }
            android.util.Log.d(r3, r4)     // Catch:{ all -> 0x00bd }
        L_0x006f:
            org.apache.http.impl.conn.CPool r4 = r9.pool     // Catch:{ all -> 0x00b3 }
            boolean r3 = r0.isOpen()     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0103
            boolean r3 = r1.isRouteComplete()     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0103
            r3 = 1
        L_0x007e:
            r4.release(r1, (boolean) r3)     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = "HttpClient"
            r4 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r4)     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x00b0
            java.lang.String r4 = "HttpClient"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = "Connection released: "
            r3.<init>(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = r9.format(r1)     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r5 = r3.append(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.Object r3 = r1.getRoute()     // Catch:{ all -> 0x00b3 }
            org.apache.http.conn.routing.HttpRoute r3 = (org.apache.http.conn.routing.HttpRoute) r3     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = r9.formatStats(r3)     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r3 = r5.append(r3)     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x00b3 }
            android.util.Log.d(r4, r3)     // Catch:{ all -> 0x00b3 }
        L_0x00b0:
            monitor-exit(r10)     // Catch:{ all -> 0x00b3 }
            goto L_0x000d
        L_0x00b3:
            r3 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x00b3 }
            throw r3
        L_0x00b6:
            java.util.concurrent.TimeUnit r14 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x00bd }
            goto L_0x001f
        L_0x00ba:
            java.lang.String r2 = "indefinitely"
            goto L_0x004d
        L_0x00bd:
            r3 = move-exception
            r4 = r3
            org.apache.http.impl.conn.CPool r5 = r9.pool     // Catch:{ all -> 0x00b3 }
            boolean r3 = r0.isOpen()     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0101
            boolean r3 = r1.isRouteComplete()     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0101
            r3 = 1
        L_0x00ce:
            r5.release(r1, (boolean) r3)     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = "HttpClient"
            r5 = 3
            boolean r3 = android.util.Log.isLoggable(r3, r5)     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0100
            java.lang.String r5 = "HttpClient"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            java.lang.String r6 = "Connection released: "
            r3.<init>(r6)     // Catch:{ all -> 0x00b3 }
            java.lang.String r6 = r9.format(r1)     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r6 = r3.append(r6)     // Catch:{ all -> 0x00b3 }
            java.lang.Object r3 = r1.getRoute()     // Catch:{ all -> 0x00b3 }
            org.apache.http.conn.routing.HttpRoute r3 = (org.apache.http.conn.routing.HttpRoute) r3     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = r9.formatStats(r3)     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r3 = r6.append(r3)     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x00b3 }
            android.util.Log.d(r5, r3)     // Catch:{ all -> 0x00b3 }
        L_0x0100:
            throw r4     // Catch:{ all -> 0x00b3 }
        L_0x0101:
            r3 = 0
            goto L_0x00ce
        L_0x0103:
            r3 = 0
            goto L_0x007e
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.conn.PoolingHttpClientConnectionManager.releaseConnection(org.apache.http.HttpClientConnection, java.lang.Object, long, java.util.concurrent.TimeUnit):void");
    }

    public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {
        ManagedHttpClientConnection conn;
        HttpHost host;
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            conn = (ManagedHttpClientConnection) CPoolProxy.getPoolEntry(managedConn).getConnection();
        }
        if (route.getProxyHost() != null) {
            host = route.getProxyHost();
        } else {
            host = route.getTargetHost();
        }
        InetSocketAddress localAddress = route.getLocalAddress() != null ? new InetSocketAddress(route.getLocalAddress(), 0) : null;
        SocketConfig socketConfig = this.configData.getSocketConfig(host);
        if (socketConfig == null) {
            socketConfig = this.configData.getDefaultSocketConfig();
        }
        if (socketConfig == null) {
            socketConfig = SocketConfig.DEFAULT;
        }
        this.connectionOperator.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
    }

    public void upgrade(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        ManagedHttpClientConnection conn;
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            conn = (ManagedHttpClientConnection) CPoolProxy.getPoolEntry(managedConn).getConnection();
        }
        this.connectionOperator.upgrade(conn, route.getTargetHost(), context);
    }

    public void routeComplete(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        Args.notNull(managedConn, "Managed Connection");
        Args.notNull(route, "HTTP route");
        synchronized (managedConn) {
            CPoolProxy.getPoolEntry(managedConn).markRouteComplete();
        }
    }

    public void shutdown() {
        if (this.isShutDown.compareAndSet(false, true)) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Connection manager is shutting down");
            }
            try {
                this.pool.shutdown();
            } catch (IOException ex) {
                Log.d(TAG, "I/O exception shutting down connection manager", ex);
            }
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Connection manager shut down");
            }
        }
    }

    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Closing connections idle longer than " + idleTimeout + StringUtils.SPACE + tunit);
        }
        this.pool.closeIdle(idleTimeout, tunit);
    }

    public void closeExpiredConnections() {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Closing expired connections");
        }
        this.pool.closeExpired();
    }

    public int getMaxTotal() {
        return this.pool.getMaxTotal();
    }

    public void setMaxTotal(int max) {
        this.pool.setMaxTotal(max);
    }

    public int getDefaultMaxPerRoute() {
        return this.pool.getDefaultMaxPerRoute();
    }

    public void setDefaultMaxPerRoute(int max) {
        this.pool.setDefaultMaxPerRoute(max);
    }

    public int getMaxPerRoute(HttpRoute route) {
        return this.pool.getMaxPerRoute(route);
    }

    public void setMaxPerRoute(HttpRoute route, int max) {
        this.pool.setMaxPerRoute(route, max);
    }

    public PoolStats getTotalStats() {
        return this.pool.getTotalStats();
    }

    public PoolStats getStats(HttpRoute route) {
        return this.pool.getStats(route);
    }

    public SocketConfig getDefaultSocketConfig() {
        return this.configData.getDefaultSocketConfig();
    }

    public void setDefaultSocketConfig(SocketConfig defaultSocketConfig) {
        this.configData.setDefaultSocketConfig(defaultSocketConfig);
    }

    public ConnectionConfig getDefaultConnectionConfig() {
        return this.configData.getDefaultConnectionConfig();
    }

    public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig) {
        this.configData.setDefaultConnectionConfig(defaultConnectionConfig);
    }

    public SocketConfig getSocketConfig(HttpHost host) {
        return this.configData.getSocketConfig(host);
    }

    public void setSocketConfig(HttpHost host, SocketConfig socketConfig) {
        this.configData.setSocketConfig(host, socketConfig);
    }

    public ConnectionConfig getConnectionConfig(HttpHost host) {
        return this.configData.getConnectionConfig(host);
    }

    public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
        this.configData.setConnectionConfig(host, connectionConfig);
    }

    static class ConfigData {
        private final Map<HttpHost, ConnectionConfig> connectionConfigMap = new ConcurrentHashMap();
        private volatile ConnectionConfig defaultConnectionConfig;
        private volatile SocketConfig defaultSocketConfig;
        private final Map<HttpHost, SocketConfig> socketConfigMap = new ConcurrentHashMap();

        ConfigData() {
        }

        public SocketConfig getDefaultSocketConfig() {
            return this.defaultSocketConfig;
        }

        public void setDefaultSocketConfig(SocketConfig defaultSocketConfig2) {
            this.defaultSocketConfig = defaultSocketConfig2;
        }

        public ConnectionConfig getDefaultConnectionConfig() {
            return this.defaultConnectionConfig;
        }

        public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig2) {
            this.defaultConnectionConfig = defaultConnectionConfig2;
        }

        public SocketConfig getSocketConfig(HttpHost host) {
            return this.socketConfigMap.get(host);
        }

        public void setSocketConfig(HttpHost host, SocketConfig socketConfig) {
            this.socketConfigMap.put(host, socketConfig);
        }

        public ConnectionConfig getConnectionConfig(HttpHost host) {
            return this.connectionConfigMap.get(host);
        }

        public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
            this.connectionConfigMap.put(host, connectionConfig);
        }
    }

    static class InternalConnectionFactory implements ConnFactory<HttpRoute, ManagedHttpClientConnection> {
        private final ConfigData configData;
        private final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;

        InternalConnectionFactory(ConfigData configData2, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory2) {
            this.configData = configData2 == null ? new ConfigData() : configData2;
            this.connFactory = connFactory2 == null ? ManagedHttpClientConnectionFactory.INSTANCE : connFactory2;
        }

        public ManagedHttpClientConnection create(HttpRoute route) throws IOException {
            ConnectionConfig config = null;
            if (route.getProxyHost() != null) {
                config = this.configData.getConnectionConfig(route.getProxyHost());
            }
            if (config == null) {
                config = this.configData.getConnectionConfig(route.getTargetHost());
            }
            if (config == null) {
                config = this.configData.getDefaultConnectionConfig();
            }
            if (config == null) {
                config = ConnectionConfig.DEFAULT;
            }
            return this.connFactory.create(route, config);
        }
    }
}
