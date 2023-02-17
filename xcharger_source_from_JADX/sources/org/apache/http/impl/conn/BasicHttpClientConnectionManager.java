package org.apache.http.impl.conn;

import android.util.Log;
import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.LangUtils;

@ThreadSafe
public class BasicHttpClientConnectionManager implements HttpClientConnectionManager, Closeable {
    private static final String TAG = "HttpClient";
    @GuardedBy("this")
    private ManagedHttpClientConnection conn;
    @GuardedBy("this")
    private ConnectionConfig connConfig;
    private final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;
    private final HttpClientConnectionOperator connectionOperator;
    @GuardedBy("this")
    private long expiry;
    private final AtomicBoolean isShutdown;
    @GuardedBy("this")
    private boolean leased;
    @GuardedBy("this")
    private HttpRoute route;
    @GuardedBy("this")
    private SocketConfig socketConfig;
    @GuardedBy("this")
    private Object state;
    @GuardedBy("this")
    private long updated;

    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.create().register(ConsoleSetting.SCHEMA_HTTP, PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory2, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        this.connectionOperator = new HttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver);
        this.connFactory = connFactory2 == null ? ManagedHttpClientConnectionFactory.INSTANCE : connFactory2;
        this.expiry = Long.MAX_VALUE;
        this.socketConfig = SocketConfig.DEFAULT;
        this.connConfig = ConnectionConfig.DEFAULT;
        this.isShutdown = new AtomicBoolean(false);
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory2) {
        this(socketFactoryRegistry, connFactory2, (SchemePortResolver) null, (DnsResolver) null);
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry) {
        this(socketFactoryRegistry, (HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>) null, (SchemePortResolver) null, (DnsResolver) null);
    }

    public BasicHttpClientConnectionManager() {
        this(getDefaultRegistry(), (HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>) null, (SchemePortResolver) null, (DnsResolver) null);
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

    /* access modifiers changed from: package-private */
    public HttpRoute getRoute() {
        return this.route;
    }

    /* access modifiers changed from: package-private */
    public Object getState() {
        return this.state;
    }

    public synchronized SocketConfig getSocketConfig() {
        return this.socketConfig;
    }

    public synchronized void setSocketConfig(SocketConfig socketConfig2) {
        if (socketConfig2 == null) {
            socketConfig2 = SocketConfig.DEFAULT;
        }
        this.socketConfig = socketConfig2;
    }

    public synchronized ConnectionConfig getConnectionConfig() {
        return this.connConfig;
    }

    public synchronized void setConnectionConfig(ConnectionConfig connConfig2) {
        if (connConfig2 == null) {
            connConfig2 = ConnectionConfig.DEFAULT;
        }
        this.connConfig = connConfig2;
    }

    public final ConnectionRequest requestConnection(final HttpRoute route2, final Object state2) {
        Args.notNull(route2, "Route");
        return new ConnectionRequest() {
            public boolean cancel() {
                return false;
            }

            public HttpClientConnection get(long timeout, TimeUnit tunit) {
                return BasicHttpClientConnectionManager.this.getConnection(route2, state2);
            }
        };
    }

    private void closeConnection() {
        if (this.conn != null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Closing connection");
            }
            try {
                this.conn.close();
            } catch (IOException iox) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "I/O exception closing connection", iox);
                }
            }
            this.conn = null;
        }
    }

    private void shutdownConnection() {
        if (this.conn != null) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Shutting down connection");
            }
            try {
                this.conn.shutdown();
            } catch (IOException iox) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "I/O exception shutting down connection", iox);
                }
            }
            this.conn = null;
        }
    }

    private void checkExpiry() {
        if (this.conn != null && System.currentTimeMillis() >= this.expiry) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Connection expired @ " + new Date(this.expiry));
            }
            closeConnection();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized HttpClientConnection getConnection(HttpRoute route2, Object state2) {
        ManagedHttpClientConnection managedHttpClientConnection;
        boolean z = false;
        synchronized (this) {
            Asserts.check(!this.isShutdown.get(), "Connection manager has been shut down");
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Get connection for route " + route2);
            }
            if (!this.leased) {
                z = true;
            }
            Asserts.check(z, "Connection is still allocated");
            if (!LangUtils.equals(this.route, route2) || !LangUtils.equals(this.state, state2)) {
                closeConnection();
            }
            this.route = route2;
            this.state = state2;
            checkExpiry();
            if (this.conn == null) {
                this.conn = this.connFactory.create(route2, this.connConfig);
            }
            this.leased = true;
            managedHttpClientConnection = this.conn;
        }
        return managedHttpClientConnection;
    }

    /* JADX INFO: finally extract failed */
    public synchronized void releaseConnection(HttpClientConnection conn2, Object state2, long keepalive, TimeUnit tunit) {
        String s;
        boolean z = false;
        synchronized (this) {
            Args.notNull(conn2, HttpHeaders.CONNECTION);
            if (conn2 == this.conn) {
                z = true;
            }
            Asserts.check(z, "Connection not obtained from this manager");
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Releasing connection " + conn2);
            }
            if (!this.isShutdown.get()) {
                try {
                    this.updated = System.currentTimeMillis();
                    if (!this.conn.isOpen()) {
                        this.conn = null;
                        this.route = null;
                        this.conn = null;
                        this.expiry = Long.MAX_VALUE;
                    } else {
                        this.state = state2;
                        if (Log.isLoggable(TAG, 3)) {
                            if (keepalive > 0) {
                                s = "for " + keepalive + StringUtils.SPACE + tunit;
                            } else {
                                s = "indefinitely";
                            }
                            Log.d(TAG, "Connection can be kept alive " + s);
                        }
                        if (keepalive > 0) {
                            this.expiry = this.updated + tunit.toMillis(keepalive);
                        } else {
                            this.expiry = Long.MAX_VALUE;
                        }
                    }
                    this.leased = false;
                } catch (Throwable th) {
                    this.leased = false;
                    throw th;
                }
            }
        }
    }

    public void connect(HttpClientConnection conn2, HttpRoute route2, int connectTimeout, HttpContext context) throws IOException {
        boolean z;
        HttpHost host;
        Args.notNull(conn2, HttpHeaders.CONNECTION);
        Args.notNull(route2, "HTTP route");
        if (conn2 == this.conn) {
            z = true;
        } else {
            z = false;
        }
        Asserts.check(z, "Connection not obtained from this manager");
        if (route2.getProxyHost() != null) {
            host = route2.getProxyHost();
        } else {
            host = route2.getTargetHost();
        }
        this.connectionOperator.connect(this.conn, host, route2.getLocalAddress() != null ? new InetSocketAddress(route2.getLocalAddress(), 0) : null, connectTimeout, this.socketConfig, context);
    }

    public void upgrade(HttpClientConnection conn2, HttpRoute route2, HttpContext context) throws IOException {
        Args.notNull(conn2, HttpHeaders.CONNECTION);
        Args.notNull(route2, "HTTP route");
        Asserts.check(conn2 == this.conn, "Connection not obtained from this manager");
        this.connectionOperator.upgrade(this.conn, route2.getTargetHost(), context);
    }

    public void routeComplete(HttpClientConnection conn2, HttpRoute route2, HttpContext context) throws IOException {
    }

    public synchronized void closeExpiredConnections() {
        if (!this.isShutdown.get()) {
            if (!this.leased) {
                checkExpiry();
            }
        }
    }

    public synchronized void closeIdleConnections(long idletime, TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        if (!this.isShutdown.get()) {
            if (!this.leased) {
                long time = tunit.toMillis(idletime);
                if (time < 0) {
                    time = 0;
                }
                if (this.updated <= System.currentTimeMillis() - time) {
                    closeConnection();
                }
            }
        }
    }

    public synchronized void shutdown() {
        if (this.isShutdown.compareAndSet(false, true)) {
            shutdownConnection();
        }
    }
}
