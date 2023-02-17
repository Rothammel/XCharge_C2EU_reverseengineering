package org.apache.http.impl.conn;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.pool.AbstractConnPool;
import org.apache.http.pool.ConnFactory;

@ThreadSafe
class CPool extends AbstractConnPool<HttpRoute, ManagedHttpClientConnection, CPoolEntry> {
    private static final AtomicLong COUNTER = new AtomicLong();
    private final long timeToLive;
    private final TimeUnit tunit;

    public CPool(ConnFactory<HttpRoute, ManagedHttpClientConnection> connFactory, int defaultMaxPerRoute, int maxTotal, long timeToLive2, TimeUnit tunit2) {
        super(connFactory, defaultMaxPerRoute, maxTotal);
        this.timeToLive = timeToLive2;
        this.tunit = tunit2;
    }

    /* access modifiers changed from: protected */
    public CPoolEntry createEntry(HttpRoute route, ManagedHttpClientConnection conn) {
        return new CPoolEntry(Long.toString(COUNTER.getAndIncrement()), route, conn, this.timeToLive, this.tunit);
    }
}
