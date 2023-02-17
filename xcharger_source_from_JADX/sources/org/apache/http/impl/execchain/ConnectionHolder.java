package org.apache.http.impl.execchain;

import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.conn.HttpClientConnectionManager;

@ThreadSafe
class ConnectionHolder implements ConnectionReleaseTrigger, Cancellable, Closeable {
    private static final String TAG = "HttpClient";
    private final HttpClientConnection managedConn;
    private final HttpClientConnectionManager manager;
    private volatile boolean released;
    private volatile boolean reusable;
    private volatile Object state;
    private volatile TimeUnit tunit;
    private volatile long validDuration;

    public ConnectionHolder(HttpClientConnectionManager manager2, HttpClientConnection managedConn2) {
        this.manager = manager2;
        this.managedConn = managedConn2;
    }

    public boolean isReusable() {
        return this.reusable;
    }

    public void markReusable() {
        this.reusable = true;
    }

    public void markNonReusable() {
        this.reusable = false;
    }

    public void setState(Object state2) {
        this.state = state2;
    }

    public void setValidFor(long duration, TimeUnit tunit2) {
        synchronized (this.managedConn) {
            this.validDuration = duration;
            this.tunit = tunit2;
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:18:0x0037=Splitter:B:18:0x0037, B:26:0x0057=Splitter:B:26:0x0057} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void releaseConnection() {
        /*
            r9 = this;
            org.apache.http.HttpClientConnection r8 = r9.managedConn
            monitor-enter(r8)
            boolean r1 = r9.released     // Catch:{ all -> 0x001f }
            if (r1 == 0) goto L_0x0009
            monitor-exit(r8)     // Catch:{ all -> 0x001f }
        L_0x0008:
            return
        L_0x0009:
            r1 = 1
            r9.released = r1     // Catch:{ all -> 0x001f }
            boolean r1 = r9.reusable     // Catch:{ all -> 0x001f }
            if (r1 == 0) goto L_0x0022
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x001f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x001f }
            java.lang.Object r3 = r9.state     // Catch:{ all -> 0x001f }
            long r4 = r9.validDuration     // Catch:{ all -> 0x001f }
            java.util.concurrent.TimeUnit r6 = r9.tunit     // Catch:{ all -> 0x001f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x001f }
        L_0x001d:
            monitor-exit(r8)     // Catch:{ all -> 0x001f }
            goto L_0x0008
        L_0x001f:
            r1 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x001f }
            throw r1
        L_0x0022:
            org.apache.http.HttpClientConnection r1 = r9.managedConn     // Catch:{ IOException -> 0x0044 }
            r1.close()     // Catch:{ IOException -> 0x0044 }
            java.lang.String r1 = "HttpClient"
            r2 = 3
            boolean r1 = android.util.Log.isLoggable(r1, r2)     // Catch:{ IOException -> 0x0044 }
            if (r1 == 0) goto L_0x0037
            java.lang.String r1 = "HttpClient"
            java.lang.String r2 = "Connection discarded"
            android.util.Log.d(r1, r2)     // Catch:{ IOException -> 0x0044 }
        L_0x0037:
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x001f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x001f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x001f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x001f }
            goto L_0x001d
        L_0x0044:
            r0 = move-exception
            java.lang.String r1 = "HttpClient"
            r2 = 3
            boolean r1 = android.util.Log.isLoggable(r1, r2)     // Catch:{ all -> 0x0064 }
            if (r1 == 0) goto L_0x0057
            java.lang.String r1 = "HttpClient"
            java.lang.String r2 = r0.getMessage()     // Catch:{ all -> 0x0064 }
            android.util.Log.d(r1, r2, r0)     // Catch:{ all -> 0x0064 }
        L_0x0057:
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x001f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x001f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x001f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x001f }
            goto L_0x001d
        L_0x0064:
            r1 = move-exception
            r7 = r1
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x001f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x001f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x001f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x001f }
            throw r7     // Catch:{ all -> 0x001f }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.execchain.ConnectionHolder.releaseConnection():void");
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:11:0x0021=Splitter:B:11:0x0021, B:24:0x0045=Splitter:B:24:0x0045} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void abortConnection() {
        /*
            r9 = this;
            org.apache.http.HttpClientConnection r8 = r9.managedConn
            monitor-enter(r8)
            boolean r1 = r9.released     // Catch:{ all -> 0x002f }
            if (r1 == 0) goto L_0x0009
            monitor-exit(r8)     // Catch:{ all -> 0x002f }
        L_0x0008:
            return
        L_0x0009:
            r1 = 1
            r9.released = r1     // Catch:{ all -> 0x002f }
            org.apache.http.HttpClientConnection r1 = r9.managedConn     // Catch:{ IOException -> 0x0032 }
            r1.shutdown()     // Catch:{ IOException -> 0x0032 }
            java.lang.String r1 = "HttpClient"
            r2 = 3
            boolean r1 = android.util.Log.isLoggable(r1, r2)     // Catch:{ IOException -> 0x0032 }
            if (r1 == 0) goto L_0x0021
            java.lang.String r1 = "HttpClient"
            java.lang.String r2 = "Connection discarded"
            android.util.Log.d(r1, r2)     // Catch:{ IOException -> 0x0032 }
        L_0x0021:
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x002f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x002f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x002f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x002f }
        L_0x002d:
            monitor-exit(r8)     // Catch:{ all -> 0x002f }
            goto L_0x0008
        L_0x002f:
            r1 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x002f }
            throw r1
        L_0x0032:
            r0 = move-exception
            java.lang.String r1 = "HttpClient"
            r2 = 3
            boolean r1 = android.util.Log.isLoggable(r1, r2)     // Catch:{ all -> 0x0052 }
            if (r1 == 0) goto L_0x0045
            java.lang.String r1 = "HttpClient"
            java.lang.String r2 = r0.getMessage()     // Catch:{ all -> 0x0052 }
            android.util.Log.d(r1, r2, r0)     // Catch:{ all -> 0x0052 }
        L_0x0045:
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x002f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x002f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x002f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x002f }
            goto L_0x002d
        L_0x0052:
            r1 = move-exception
            r7 = r1
            org.apache.http.conn.HttpClientConnectionManager r1 = r9.manager     // Catch:{ all -> 0x002f }
            org.apache.http.HttpClientConnection r2 = r9.managedConn     // Catch:{ all -> 0x002f }
            r3 = 0
            r4 = 0
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x002f }
            r1.releaseConnection(r2, r3, r4, r6)     // Catch:{ all -> 0x002f }
            throw r7     // Catch:{ all -> 0x002f }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.execchain.ConnectionHolder.abortConnection():void");
    }

    public boolean cancel() {
        boolean alreadyReleased = this.released;
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Cancelling request execution");
        }
        abortConnection();
        return !alreadyReleased;
    }

    public boolean isReleased() {
        return this.released;
    }

    public void close() throws IOException {
        abortConnection();
    }
}
