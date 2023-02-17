package org.apache.http.impl.conn;

import android.util.Log;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.pool.PoolEntry;

@ThreadSafe
/* loaded from: classes.dex */
class CPoolEntry extends PoolEntry<HttpRoute, ManagedHttpClientConnection> {
    private static final String TAG = "HttpClient";
    private volatile boolean routeComplete;

    public CPoolEntry(String id, HttpRoute route, ManagedHttpClientConnection conn, long timeToLive, TimeUnit tunit) {
        super(id, route, conn, timeToLive, tunit);
    }

    public void markRouteComplete() {
        this.routeComplete = true;
    }

    public boolean isRouteComplete() {
        return this.routeComplete;
    }

    public void closeConnection() throws IOException {
        HttpClientConnection conn = getConnection();
        conn.close();
    }

    public void shutdownConnection() throws IOException {
        HttpClientConnection conn = getConnection();
        conn.shutdown();
    }

    @Override // org.apache.http.pool.PoolEntry
    public boolean isExpired(long now) {
        boolean expired = super.isExpired(now);
        if (expired && Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Connection " + this + " expired @ " + new Date(getExpiry()));
        }
        return expired;
    }

    @Override // org.apache.http.pool.PoolEntry
    public boolean isClosed() {
        HttpClientConnection conn = getConnection();
        return !conn.isOpen();
    }

    @Override // org.apache.http.pool.PoolEntry
    public void close() {
        try {
            closeConnection();
        } catch (IOException ex) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "I/O error closing connection", ex);
            }
        }
    }
}
