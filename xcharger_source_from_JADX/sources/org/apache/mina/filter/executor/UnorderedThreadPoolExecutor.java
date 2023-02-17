package org.apache.mina.filter.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.session.IoEvent;

public class UnorderedThreadPoolExecutor extends ThreadPoolExecutor {
    /* access modifiers changed from: private */
    public static final Runnable EXIT_SIGNAL = new Runnable() {
        public void run() {
            throw new Error("This method shouldn't be called. Please file a bug report.");
        }
    };
    /* access modifiers changed from: private */
    public long completedTaskCount;
    /* access modifiers changed from: private */
    public volatile int corePoolSize;
    /* access modifiers changed from: private */
    public final AtomicInteger idleWorkers;
    private volatile int largestPoolSize;
    private volatile int maximumPoolSize;
    /* access modifiers changed from: private */
    public final IoEventQueueHandler queueHandler;
    private volatile boolean shutdown;
    /* access modifiers changed from: private */
    public final Set<Worker> workers;

    public UnorderedThreadPoolExecutor() {
        this(16);
    }

    public UnorderedThreadPoolExecutor(int maximumPoolSize2) {
        this(0, maximumPoolSize2);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2) {
        this(corePoolSize2, maximumPoolSize2, 30, TimeUnit.SECONDS);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime, unit, Executors.defaultThreadFactory());
    }

    public UnorderedThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime, TimeUnit unit, IoEventQueueHandler queueHandler2) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler2);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler queueHandler2) {
        super(0, 1, keepAliveTime, unit, new LinkedBlockingQueue(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        this.workers = new HashSet();
        this.idleWorkers = new AtomicInteger();
        if (corePoolSize2 < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize2);
        } else if (maximumPoolSize2 == 0 || maximumPoolSize2 < corePoolSize2) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize2);
        } else {
            queueHandler2 = queueHandler2 == null ? IoEventQueueHandler.NOOP : queueHandler2;
            this.corePoolSize = corePoolSize2;
            this.maximumPoolSize = maximumPoolSize2;
            this.queueHandler = queueHandler2;
        }
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.queueHandler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addWorker() {
        /*
            r5 = this;
            java.util.Set<org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker> r3 = r5.workers
            monitor-enter(r3)
            java.util.Set<org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x003e }
            int r2 = r2.size()     // Catch:{ all -> 0x003e }
            int r4 = r5.maximumPoolSize     // Catch:{ all -> 0x003e }
            if (r2 < r4) goto L_0x000f
            monitor-exit(r3)     // Catch:{ all -> 0x003e }
        L_0x000e:
            return
        L_0x000f:
            org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker r1 = new org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker     // Catch:{ all -> 0x003e }
            r2 = 0
            r1.<init>()     // Catch:{ all -> 0x003e }
            java.util.concurrent.ThreadFactory r2 = r5.getThreadFactory()     // Catch:{ all -> 0x003e }
            java.lang.Thread r0 = r2.newThread(r1)     // Catch:{ all -> 0x003e }
            java.util.concurrent.atomic.AtomicInteger r2 = r5.idleWorkers     // Catch:{ all -> 0x003e }
            r2.incrementAndGet()     // Catch:{ all -> 0x003e }
            r0.start()     // Catch:{ all -> 0x003e }
            java.util.Set<org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x003e }
            r2.add(r1)     // Catch:{ all -> 0x003e }
            java.util.Set<org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x003e }
            int r2 = r2.size()     // Catch:{ all -> 0x003e }
            int r4 = r5.largestPoolSize     // Catch:{ all -> 0x003e }
            if (r2 <= r4) goto L_0x003c
            java.util.Set<org.apache.mina.filter.executor.UnorderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x003e }
            int r2 = r2.size()     // Catch:{ all -> 0x003e }
            r5.largestPoolSize = r2     // Catch:{ all -> 0x003e }
        L_0x003c:
            monitor-exit(r3)     // Catch:{ all -> 0x003e }
            goto L_0x000e
        L_0x003e:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x003e }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.addWorker():void");
    }

    private void addWorkerIfNecessary() {
        if (this.idleWorkers.get() == 0) {
            synchronized (this.workers) {
                if (this.workers.isEmpty() || this.idleWorkers.get() == 0) {
                    addWorker();
                }
            }
        }
    }

    private void removeWorker() {
        synchronized (this.workers) {
            if (this.workers.size() > this.corePoolSize) {
                getQueue().offer(EXIT_SIGNAL);
            }
        }
    }

    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize2) {
        if (maximumPoolSize2 <= 0 || maximumPoolSize2 < this.corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize2);
        }
        synchronized (this.workers) {
            this.maximumPoolSize = maximumPoolSize2;
            for (int difference = this.workers.size() - maximumPoolSize2; difference > 0; difference--) {
                removeWorker();
            }
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        synchronized (this.workers) {
            while (!isTerminated()) {
                long waitTime = deadline - System.currentTimeMillis();
                if (waitTime <= 0) {
                    break;
                }
                this.workers.wait(waitTime);
            }
        }
        return isTerminated();
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public boolean isTerminated() {
        boolean isEmpty;
        if (!this.shutdown) {
            return false;
        }
        synchronized (this.workers) {
            isEmpty = this.workers.isEmpty();
        }
        return isEmpty;
    }

    public void shutdown() {
        if (!this.shutdown) {
            this.shutdown = true;
            synchronized (this.workers) {
                for (int i = this.workers.size(); i > 0; i--) {
                    getQueue().offer(EXIT_SIGNAL);
                }
            }
        }
    }

    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> answer = new ArrayList<>();
        while (true) {
            Runnable task = (Runnable) getQueue().poll();
            if (task == null) {
                return answer;
            }
            if (task == EXIT_SIGNAL) {
                getQueue().offer(EXIT_SIGNAL);
                Thread.yield();
            } else {
                getQueueHandler().polled(this, (IoEvent) task);
                answer.add(task);
            }
        }
    }

    public void execute(Runnable task) {
        if (this.shutdown) {
            rejectTask(task);
        }
        checkTaskType(task);
        IoEvent e = (IoEvent) task;
        boolean offeredEvent = this.queueHandler.accept(this, e);
        if (offeredEvent) {
            getQueue().offer(e);
        }
        addWorkerIfNecessary();
        if (offeredEvent) {
            this.queueHandler.offered(this, e);
        }
    }

    private void rejectTask(Runnable task) {
        getRejectedExecutionHandler().rejectedExecution(task, this);
    }

    private void checkTaskType(Runnable task) {
        if (!(task instanceof IoEvent)) {
            throw new IllegalArgumentException("task must be an IoEvent or its subclass.");
        }
    }

    public int getActiveCount() {
        int size;
        synchronized (this.workers) {
            size = this.workers.size() - this.idleWorkers.get();
        }
        return size;
    }

    public long getCompletedTaskCount() {
        long answer;
        synchronized (this.workers) {
            answer = this.completedTaskCount;
            for (Worker w : this.workers) {
                answer += w.completedTaskCount.get();
            }
        }
        return answer;
    }

    public int getLargestPoolSize() {
        return this.largestPoolSize;
    }

    public int getPoolSize() {
        int size;
        synchronized (this.workers) {
            size = this.workers.size();
        }
        return size;
    }

    public long getTaskCount() {
        return getCompletedTaskCount();
    }

    public boolean isTerminating() {
        boolean z;
        synchronized (this.workers) {
            z = isShutdown() && !isTerminated();
        }
        return z;
    }

    public int prestartAllCoreThreads() {
        int answer = 0;
        synchronized (this.workers) {
            for (int i = this.corePoolSize - this.workers.size(); i > 0; i--) {
                addWorker();
                answer++;
            }
        }
        return answer;
    }

    public boolean prestartCoreThread() {
        boolean z;
        synchronized (this.workers) {
            if (this.workers.size() < this.corePoolSize) {
                addWorker();
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public void purge() {
    }

    public boolean remove(Runnable task) {
        boolean removed = super.remove(task);
        if (removed) {
            getQueueHandler().polled(this, (IoEvent) task);
        }
        return removed;
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize2) {
        if (corePoolSize2 < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize2);
        } else if (corePoolSize2 > this.maximumPoolSize) {
            throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
        } else {
            synchronized (this.workers) {
                if (this.corePoolSize > corePoolSize2) {
                    for (int i = this.corePoolSize - corePoolSize2; i > 0; i--) {
                        removeWorker();
                    }
                }
                this.corePoolSize = corePoolSize2;
            }
        }
    }

    private class Worker implements Runnable {
        /* access modifiers changed from: private */
        public AtomicLong completedTaskCount;
        private Thread thread;

        private Worker() {
            this.completedTaskCount = new AtomicLong(0);
        }

        public void run() {
            this.thread = Thread.currentThread();
            while (true) {
                Runnable task = fetchTask();
                UnorderedThreadPoolExecutor.this.idleWorkers.decrementAndGet();
                if (task == null) {
                    synchronized (UnorderedThreadPoolExecutor.this.workers) {
                        if (UnorderedThreadPoolExecutor.this.workers.size() > UnorderedThreadPoolExecutor.this.corePoolSize) {
                            UnorderedThreadPoolExecutor.this.workers.remove(this);
                            break;
                        }
                    }
                }
                try {
                    if (task == UnorderedThreadPoolExecutor.EXIT_SIGNAL) {
                        break;
                    }
                    if (task != null) {
                        UnorderedThreadPoolExecutor.this.queueHandler.polled(UnorderedThreadPoolExecutor.this, (IoEvent) task);
                        runTask(task);
                    }
                    UnorderedThreadPoolExecutor.this.idleWorkers.incrementAndGet();
                } catch (Throwable th) {
                    synchronized (UnorderedThreadPoolExecutor.this.workers) {
                        UnorderedThreadPoolExecutor.this.workers.remove(this);
                        UnorderedThreadPoolExecutor unorderedThreadPoolExecutor = UnorderedThreadPoolExecutor.this;
                        long unused = unorderedThreadPoolExecutor.completedTaskCount = unorderedThreadPoolExecutor.completedTaskCount + this.completedTaskCount.get();
                        UnorderedThreadPoolExecutor.this.workers.notifyAll();
                        throw th;
                    }
                }
            }
            synchronized (UnorderedThreadPoolExecutor.this.workers) {
                UnorderedThreadPoolExecutor.this.workers.remove(this);
                UnorderedThreadPoolExecutor unorderedThreadPoolExecutor2 = UnorderedThreadPoolExecutor.this;
                long unused2 = unorderedThreadPoolExecutor2.completedTaskCount = unorderedThreadPoolExecutor2.completedTaskCount + this.completedTaskCount.get();
                UnorderedThreadPoolExecutor.this.workers.notifyAll();
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.Runnable} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private java.lang.Runnable fetchTask() {
            /*
                r12 = this;
                r7 = 0
                long r2 = java.lang.System.currentTimeMillis()
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r10 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this
                java.util.concurrent.TimeUnit r11 = java.util.concurrent.TimeUnit.MILLISECONDS
                long r10 = r10.getKeepAliveTime(r11)
                long r4 = r2 + r10
            L_0x000f:
                long r8 = r4 - r2
                r10 = 0
                int r10 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
                if (r10 > 0) goto L_0x0018
            L_0x0017:
                return r7
            L_0x0018:
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r10 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch:{ all -> 0x002f }
                java.util.concurrent.BlockingQueue r10 = r10.getQueue()     // Catch:{ all -> 0x002f }
                java.util.concurrent.TimeUnit r11 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x002f }
                java.lang.Object r10 = r10.poll(r8, r11)     // Catch:{ all -> 0x002f }
                r0 = r10
                java.lang.Runnable r0 = (java.lang.Runnable) r0     // Catch:{ all -> 0x002f }
                r7 = r0
                if (r7 != 0) goto L_0x0017
                long r2 = java.lang.System.currentTimeMillis()     // Catch:{ InterruptedException -> 0x0037 }
                goto L_0x0017
            L_0x002f:
                r10 = move-exception
                if (r7 != 0) goto L_0x0036
                long r2 = java.lang.System.currentTimeMillis()     // Catch:{ InterruptedException -> 0x0037 }
            L_0x0036:
                throw r10     // Catch:{ InterruptedException -> 0x0037 }
            L_0x0037:
                r6 = move-exception
                goto L_0x000f
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.Worker.fetchTask():java.lang.Runnable");
        }

        private void runTask(Runnable task) {
            UnorderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
            boolean ran = false;
            try {
                task.run();
                ran = true;
                UnorderedThreadPoolExecutor.this.afterExecute(task, (Throwable) null);
                this.completedTaskCount.incrementAndGet();
            } catch (RuntimeException e) {
                if (!ran) {
                    UnorderedThreadPoolExecutor.this.afterExecute(task, e);
                }
                throw e;
            }
        }
    }
}
