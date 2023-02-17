package org.apache.http.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.http.util.Args;

public class BasicFuture<T> implements Future<T>, Cancellable {
    private final FutureCallback<T> callback;
    private volatile boolean cancelled;
    private volatile boolean completed;

    /* renamed from: ex */
    private volatile Exception f164ex;
    private volatile T result;

    public BasicFuture(FutureCallback<T> callback2) {
        this.callback = callback2;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if (this.f164ex == null) {
            return this.result;
        }
        throw new ExecutionException(this.f164ex);
    }

    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            wait();
        }
        return getResult();
    }

    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        T result2;
        Args.notNull(unit, "Time unit");
        long msecs = unit.toMillis(timeout);
        long startTime = msecs <= 0 ? 0 : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            result2 = getResult();
        } else if (waitTime <= 0) {
            throw new TimeoutException();
        } else {
            do {
                wait(waitTime);
                if (this.completed) {
                    result2 = getResult();
                } else {
                    waitTime = msecs - (System.currentTimeMillis() - startTime);
                }
            } while (waitTime > 0);
            throw new TimeoutException();
        }
        return result2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r2.callback == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r2.callback.completed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean completed(T r3) {
        /*
            r2 = this;
            r0 = 1
            monitor-enter(r2)
            boolean r1 = r2.completed     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            r0 = 0
        L_0x0008:
            return r0
        L_0x0009:
            r1 = 1
            r2.completed = r1     // Catch:{ all -> 0x001c }
            r2.result = r3     // Catch:{ all -> 0x001c }
            r2.notifyAll()     // Catch:{ all -> 0x001c }
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            if (r1 == 0) goto L_0x0008
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            r1.completed(r3)
            goto L_0x0008
        L_0x001c:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.concurrent.BasicFuture.completed(java.lang.Object):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r2.callback == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r2.callback.failed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean failed(java.lang.Exception r3) {
        /*
            r2 = this;
            r0 = 1
            monitor-enter(r2)
            boolean r1 = r2.completed     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            r0 = 0
        L_0x0008:
            return r0
        L_0x0009:
            r1 = 1
            r2.completed = r1     // Catch:{ all -> 0x001c }
            r2.f164ex = r3     // Catch:{ all -> 0x001c }
            r2.notifyAll()     // Catch:{ all -> 0x001c }
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            if (r1 == 0) goto L_0x0008
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            r1.failed(r3)
            goto L_0x0008
        L_0x001c:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.concurrent.BasicFuture.failed(java.lang.Exception):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        if (r2.callback == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        r2.callback.cancelled();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean cancel(boolean r3) {
        /*
            r2 = this;
            r0 = 1
            monitor-enter(r2)
            boolean r1 = r2.completed     // Catch:{ all -> 0x001d }
            if (r1 == 0) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x001d }
            r0 = 0
        L_0x0008:
            return r0
        L_0x0009:
            r1 = 1
            r2.completed = r1     // Catch:{ all -> 0x001d }
            r1 = 1
            r2.cancelled = r1     // Catch:{ all -> 0x001d }
            r2.notifyAll()     // Catch:{ all -> 0x001d }
            monitor-exit(r2)     // Catch:{ all -> 0x001d }
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            if (r1 == 0) goto L_0x0008
            org.apache.http.concurrent.FutureCallback<T> r1 = r2.callback
            r1.cancelled()
            goto L_0x0008
        L_0x001d:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x001d }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.concurrent.BasicFuture.cancel(boolean):boolean");
    }

    public boolean cancel() {
        return cancel(true);
    }
}
