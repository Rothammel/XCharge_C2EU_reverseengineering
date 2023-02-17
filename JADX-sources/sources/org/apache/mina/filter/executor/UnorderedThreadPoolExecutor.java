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

/* loaded from: classes.dex */
public class UnorderedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Runnable EXIT_SIGNAL = new Runnable() { // from class: org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.1
        @Override // java.lang.Runnable
        public void run() {
            throw new Error("This method shouldn't be called. Please file a bug report.");
        }
    };
    private long completedTaskCount;
    private volatile int corePoolSize;
    private final AtomicInteger idleWorkers;
    private volatile int largestPoolSize;
    private volatile int maximumPoolSize;
    private final IoEventQueueHandler queueHandler;
    private volatile boolean shutdown;
    private final Set<Worker> workers;

    public UnorderedThreadPoolExecutor() {
        this(16);
    }

    public UnorderedThreadPoolExecutor(int maximumPoolSize) {
        this(0, maximumPoolSize);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory());
    }

    public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventQueueHandler queueHandler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, null);
    }

    public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler queueHandler) {
        super(0, 1, keepAliveTime, unit, new LinkedBlockingQueue(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        this.workers = new HashSet();
        this.idleWorkers = new AtomicInteger();
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (maximumPoolSize == 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        queueHandler = queueHandler == null ? IoEventQueueHandler.NOOP : queueHandler;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.queueHandler = queueHandler;
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.queueHandler;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
    }

    private void addWorker() {
        synchronized (this.workers) {
            if (this.workers.size() < this.maximumPoolSize) {
                Worker worker = new Worker();
                Thread thread = getThreadFactory().newThread(worker);
                this.idleWorkers.incrementAndGet();
                thread.start();
                this.workers.add(worker);
                if (this.workers.size() > this.largestPoolSize) {
                    this.largestPoolSize = this.workers.size();
                }
            }
        }
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

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < this.corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        synchronized (this.workers) {
            this.maximumPoolSize = maximumPoolSize;
            for (int difference = this.workers.size() - maximumPoolSize; difference > 0; difference--) {
                removeWorker();
            }
        }
    }

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
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

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
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

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
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

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> answer = new ArrayList<>();
        while (true) {
            Runnable task = getQueue().poll();
            if (task != null) {
                if (task == EXIT_SIGNAL) {
                    getQueue().offer(EXIT_SIGNAL);
                    Thread.yield();
                } else {
                    getQueueHandler().polled(this, (IoEvent) task);
                    answer.add(task);
                }
            } else {
                return answer;
            }
        }
    }

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.Executor
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

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getActiveCount() {
        int size;
        synchronized (this.workers) {
            size = this.workers.size() - this.idleWorkers.get();
        }
        return size;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
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

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getLargestPoolSize() {
        return this.largestPoolSize;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getPoolSize() {
        int size;
        synchronized (this.workers) {
            size = this.workers.size();
        }
        return size;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public long getTaskCount() {
        return getCompletedTaskCount();
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public boolean isTerminating() {
        boolean z;
        synchronized (this.workers) {
            z = isShutdown() && !isTerminated();
        }
        return z;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
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

    @Override // java.util.concurrent.ThreadPoolExecutor
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

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void purge() {
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public boolean remove(Runnable task) {
        boolean removed = super.remove(task);
        if (removed) {
            getQueueHandler().polled(this, (IoEvent) task);
        }
        return removed;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (corePoolSize > this.maximumPoolSize) {
            throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
        }
        synchronized (this.workers) {
            if (this.corePoolSize > corePoolSize) {
                for (int i = this.corePoolSize - corePoolSize; i > 0; i--) {
                    removeWorker();
                }
            }
            this.corePoolSize = corePoolSize;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Worker implements Runnable {
        private AtomicLong completedTaskCount;
        private Thread thread;

        private Worker() {
            this.completedTaskCount = new AtomicLong(0L);
        }

        /* JADX WARN: Code restructure failed: missing block: B:9:0x002e, code lost:
            r10.this$0.workers.remove(r10);
         */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r10 = this;
                java.lang.Thread r3 = java.lang.Thread.currentThread()
                r10.thread = r3
            L6:
                java.lang.Runnable r2 = r10.fetchTask()     // Catch: java.lang.Throwable -> L89
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L89
                java.util.concurrent.atomic.AtomicInteger r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$200(r3)     // Catch: java.lang.Throwable -> L89
                r3.decrementAndGet()     // Catch: java.lang.Throwable -> L89
                if (r2 != 0) goto L64
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L89
                java.util.Set r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)     // Catch: java.lang.Throwable -> L89
                monitor-enter(r4)     // Catch: java.lang.Throwable -> L89
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb5
                java.util.Set r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)     // Catch: java.lang.Throwable -> Lb5
                int r3 = r3.size()     // Catch: java.lang.Throwable -> Lb5
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb5
                int r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$400(r5)     // Catch: java.lang.Throwable -> Lb5
                if (r3 <= r5) goto L63
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb5
                java.util.Set r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)     // Catch: java.lang.Throwable -> Lb5
                r3.remove(r10)     // Catch: java.lang.Throwable -> Lb5
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lb5
            L38:
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this
                java.util.Set r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)
                monitor-enter(r4)
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc3
                java.util.Set r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)     // Catch: java.lang.Throwable -> Lc3
                r3.remove(r10)     // Catch: java.lang.Throwable -> Lc3
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc3
                long r6 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$700(r3)     // Catch: java.lang.Throwable -> Lc3
                java.util.concurrent.atomic.AtomicLong r5 = r10.completedTaskCount     // Catch: java.lang.Throwable -> Lc3
                long r8 = r5.get()     // Catch: java.lang.Throwable -> Lc3
                long r6 = r6 + r8
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$702(r3, r6)     // Catch: java.lang.Throwable -> Lc3
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc3
                java.util.Set r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r3)     // Catch: java.lang.Throwable -> Lc3
                r3.notifyAll()     // Catch: java.lang.Throwable -> Lc3
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lc3
                return
            L63:
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lb5
            L64:
                java.lang.Runnable r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$500()     // Catch: java.lang.Throwable -> L89
                if (r2 == r3) goto L38
                if (r2 == 0) goto L7e
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb8
                org.apache.mina.filter.executor.IoEventQueueHandler r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$600(r3)     // Catch: java.lang.Throwable -> Lb8
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb8
                r0 = r2
                org.apache.mina.core.session.IoEvent r0 = (org.apache.mina.core.session.IoEvent) r0     // Catch: java.lang.Throwable -> Lb8
                r3 = r0
                r4.polled(r5, r3)     // Catch: java.lang.Throwable -> Lb8
                r10.runTask(r2)     // Catch: java.lang.Throwable -> Lb8
            L7e:
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L89
                java.util.concurrent.atomic.AtomicInteger r3 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$200(r3)     // Catch: java.lang.Throwable -> L89
                r3.incrementAndGet()     // Catch: java.lang.Throwable -> L89
                goto L6
            L89:
                r3 = move-exception
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this
                java.util.Set r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r4)
                monitor-enter(r4)
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc6
                java.util.Set r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r5)     // Catch: java.lang.Throwable -> Lc6
                r5.remove(r10)     // Catch: java.lang.Throwable -> Lc6
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc6
                long r6 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$700(r5)     // Catch: java.lang.Throwable -> Lc6
                java.util.concurrent.atomic.AtomicLong r8 = r10.completedTaskCount     // Catch: java.lang.Throwable -> Lc6
                long r8 = r8.get()     // Catch: java.lang.Throwable -> Lc6
                long r6 = r6 + r8
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$702(r5, r6)     // Catch: java.lang.Throwable -> Lc6
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lc6
                java.util.Set r5 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$300(r5)     // Catch: java.lang.Throwable -> Lc6
                r5.notifyAll()     // Catch: java.lang.Throwable -> Lc6
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lc6
                throw r3
            Lb5:
                r3 = move-exception
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lb5
                throw r3     // Catch: java.lang.Throwable -> L89
            Lb8:
                r3 = move-exception
                org.apache.mina.filter.executor.UnorderedThreadPoolExecutor r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L89
                java.util.concurrent.atomic.AtomicInteger r4 = org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.access$200(r4)     // Catch: java.lang.Throwable -> L89
                r4.incrementAndGet()     // Catch: java.lang.Throwable -> L89
                throw r3     // Catch: java.lang.Throwable -> L89
            Lc3:
                r3 = move-exception
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lc3
                throw r3
            Lc6:
                r3 = move-exception
                monitor-exit(r4)     // Catch: java.lang.Throwable -> Lc6
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.UnorderedThreadPoolExecutor.Worker.run():void");
        }

        private Runnable fetchTask() {
            Runnable task = null;
            long currentTime = System.currentTimeMillis();
            long deadline = currentTime + UnorderedThreadPoolExecutor.this.getKeepAliveTime(TimeUnit.MILLISECONDS);
            while (true) {
                long waitTime = deadline - currentTime;
                if (waitTime <= 0) {
                    break;
                }
                try {
                    task = UnorderedThreadPoolExecutor.this.getQueue().poll(waitTime, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        break;
                    }
                    currentTime = System.currentTimeMillis();
                    break;
                } catch (InterruptedException e) {
                }
            }
            return task;
        }

        private void runTask(Runnable task) {
            UnorderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
            boolean ran = false;
            try {
                task.run();
                ran = true;
                UnorderedThreadPoolExecutor.this.afterExecute(task, null);
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
