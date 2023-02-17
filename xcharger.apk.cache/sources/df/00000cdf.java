package org.apache.http.pool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.pool.PoolEntry;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@NotThreadSafe
/* loaded from: classes.dex */
abstract class RouteSpecificPool<T, C, E extends PoolEntry<T, C>> {
    private final T route;
    private final Set<E> leased = new HashSet();
    private final LinkedList<E> available = new LinkedList<>();
    private final LinkedList<PoolEntryFuture<E>> pending = new LinkedList<>();

    protected abstract E createEntry(C c);

    /* JADX INFO: Access modifiers changed from: package-private */
    public RouteSpecificPool(T route) {
        this.route = route;
    }

    public final T getRoute() {
        return this.route;
    }

    public int getLeasedCount() {
        return this.leased.size();
    }

    public int getPendingCount() {
        return this.pending.size();
    }

    public int getAvailableCount() {
        return this.available.size();
    }

    public int getAllocatedCount() {
        return this.available.size() + this.leased.size();
    }

    public E getFree(Object state) {
        if (!this.available.isEmpty()) {
            if (state != null) {
                Iterator<E> it2 = this.available.iterator();
                while (it2.hasNext()) {
                    E entry = it2.next();
                    if (state.equals(entry.getState())) {
                        it2.remove();
                        this.leased.add(entry);
                        return entry;
                    }
                }
            }
            Iterator<E> it3 = this.available.iterator();
            while (it3.hasNext()) {
                E entry2 = it3.next();
                if (entry2.getState() == null) {
                    it3.remove();
                    this.leased.add(entry2);
                    return entry2;
                }
            }
        }
        return null;
    }

    public E getLastUsed() {
        if (this.available.isEmpty()) {
            return null;
        }
        return this.available.getLast();
    }

    public boolean remove(E entry) {
        Args.notNull(entry, "Pool entry");
        return this.available.remove(entry) || this.leased.remove(entry);
    }

    public void free(E entry, boolean reusable) {
        Args.notNull(entry, "Pool entry");
        boolean found = this.leased.remove(entry);
        Asserts.check(found, "Entry %s has not been leased from this pool", entry);
        if (reusable) {
            this.available.addFirst(entry);
        }
    }

    public E add(C conn) {
        E entry = createEntry(conn);
        this.leased.add(entry);
        return entry;
    }

    public void queue(PoolEntryFuture<E> future) {
        if (future != null) {
            this.pending.add(future);
        }
    }

    public PoolEntryFuture<E> nextPending() {
        return this.pending.poll();
    }

    public void unqueue(PoolEntryFuture<E> future) {
        if (future != null) {
            this.pending.remove(future);
        }
    }

    public void shutdown() {
        Iterator<PoolEntryFuture<E>> it2 = this.pending.iterator();
        while (it2.hasNext()) {
            PoolEntryFuture<E> future = it2.next();
            future.cancel(true);
        }
        this.pending.clear();
        Iterator<E> it3 = this.available.iterator();
        while (it3.hasNext()) {
            E entry = it3.next();
            entry.close();
        }
        this.available.clear();
        for (E entry2 : this.leased) {
            entry2.close();
        }
        this.leased.clear();
    }

    public String toString() {
        return "[route: " + this.route + "][leased: " + this.leased.size() + "][available: " + this.available.size() + "][pending: " + this.pending.size() + "]";
    }
}