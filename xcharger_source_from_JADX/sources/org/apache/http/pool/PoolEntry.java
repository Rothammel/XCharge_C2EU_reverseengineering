package org.apache.http.pool;

import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHeaders;
import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.util.Args;

@ThreadSafe
public abstract class PoolEntry<T, C> {
    private final C conn;
    private final long created;
    @GuardedBy("this")
    private long expiry;

    /* renamed from: id */
    private final String f183id;
    private final T route;
    private volatile Object state;
    @GuardedBy("this")
    private long updated;
    private final long validUnit;

    public abstract void close();

    public abstract boolean isClosed();

    public PoolEntry(String id, T route2, C conn2, long timeToLive, TimeUnit tunit) {
        Args.notNull(route2, "Route");
        Args.notNull(conn2, HttpHeaders.CONNECTION);
        Args.notNull(tunit, "Time unit");
        this.f183id = id;
        this.route = route2;
        this.conn = conn2;
        this.created = System.currentTimeMillis();
        if (timeToLive > 0) {
            this.validUnit = this.created + tunit.toMillis(timeToLive);
        } else {
            this.validUnit = Long.MAX_VALUE;
        }
        this.expiry = this.validUnit;
    }

    public PoolEntry(String id, T route2, C conn2) {
        this(id, route2, conn2, 0, TimeUnit.MILLISECONDS);
    }

    public String getId() {
        return this.f183id;
    }

    public T getRoute() {
        return this.route;
    }

    public C getConnection() {
        return this.conn;
    }

    public long getCreated() {
        return this.created;
    }

    public long getValidUnit() {
        return this.validUnit;
    }

    public Object getState() {
        return this.state;
    }

    public void setState(Object state2) {
        this.state = state2;
    }

    public synchronized long getUpdated() {
        return this.updated;
    }

    public synchronized long getExpiry() {
        return this.expiry;
    }

    public synchronized void updateExpiry(long time, TimeUnit tunit) {
        long newExpiry;
        Args.notNull(tunit, "Time unit");
        this.updated = System.currentTimeMillis();
        if (time > 0) {
            newExpiry = this.updated + tunit.toMillis(time);
        } else {
            newExpiry = Long.MAX_VALUE;
        }
        this.expiry = Math.min(newExpiry, this.validUnit);
    }

    public synchronized boolean isExpired(long now) {
        return now >= this.expiry;
    }

    public String toString() {
        return "[id:" + this.f183id + "][route:" + this.route + "][state:" + this.state + "]";
    }
}
