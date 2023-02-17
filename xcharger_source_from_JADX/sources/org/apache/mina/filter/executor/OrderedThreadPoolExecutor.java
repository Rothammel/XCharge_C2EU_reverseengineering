package org.apache.mina.filter.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final int DEFAULT_INITIAL_THREAD_POOL_SIZE = 0;
    private static final int DEFAULT_KEEP_ALIVE = 30;
    private static final int DEFAULT_MAX_THREAD_POOL = 16;
    /* access modifiers changed from: private */
    public static final IoSession EXIT_SIGNAL = new DummySession();
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) OrderedThreadPoolExecutor.class);
    private final AttributeKey TASKS_QUEUE;
    /* access modifiers changed from: private */
    public long completedTaskCount;
    /* access modifiers changed from: private */
    public final IoEventQueueHandler eventQueueHandler;
    /* access modifiers changed from: private */
    public final AtomicInteger idleWorkers;
    private volatile int largestPoolSize;
    private volatile boolean shutdown;
    /* access modifiers changed from: private */
    public final BlockingQueue<IoSession> waitingSessions;
    /* access modifiers changed from: private */
    public final Set<Worker> workers;

    public OrderedThreadPoolExecutor() {
        this(0, 16, 30, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
    }

    public OrderedThreadPoolExecutor(int maximumPoolSize) {
        this(0, maximumPoolSize, 30, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, 30, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventQueueHandler eventQueueHandler2) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), eventQueueHandler2);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler eventQueueHandler2) {
        super(0, 1, keepAliveTime, unit, new SynchronousQueue(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        this.TASKS_QUEUE = new AttributeKey(getClass(), "tasksQueue");
        this.waitingSessions = new LinkedBlockingQueue();
        this.workers = new HashSet();
        this.idleWorkers = new AtomicInteger();
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        } else if (maximumPoolSize == 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        } else {
            super.setCorePoolSize(corePoolSize);
            super.setMaximumPoolSize(maximumPoolSize);
            if (eventQueueHandler2 == null) {
                this.eventQueueHandler = IoEventQueueHandler.NOOP;
            } else {
                this.eventQueueHandler = eventQueueHandler2;
            }
        }
    }

    /* access modifiers changed from: private */
    public SessionTasksQueue getSessionTasksQueue(IoSession session) {
        SessionTasksQueue queue = (SessionTasksQueue) session.getAttribute(this.TASKS_QUEUE);
        if (queue != null) {
            return queue;
        }
        SessionTasksQueue queue2 = new SessionTasksQueue();
        SessionTasksQueue oldQueue = (SessionTasksQueue) session.setAttributeIfAbsent(this.TASKS_QUEUE, queue2);
        if (oldQueue != null) {
            return oldQueue;
        }
        return queue2;
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.eventQueueHandler;
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
            java.util.Set<org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker> r3 = r5.workers
            monitor-enter(r3)
            java.util.Set<org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x0040 }
            int r2 = r2.size()     // Catch:{ all -> 0x0040 }
            int r4 = super.getMaximumPoolSize()     // Catch:{ all -> 0x0040 }
            if (r2 < r4) goto L_0x0011
            monitor-exit(r3)     // Catch:{ all -> 0x0040 }
        L_0x0010:
            return
        L_0x0011:
            org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker r1 = new org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker     // Catch:{ all -> 0x0040 }
            r2 = 0
            r1.<init>()     // Catch:{ all -> 0x0040 }
            java.util.concurrent.ThreadFactory r2 = r5.getThreadFactory()     // Catch:{ all -> 0x0040 }
            java.lang.Thread r0 = r2.newThread(r1)     // Catch:{ all -> 0x0040 }
            java.util.concurrent.atomic.AtomicInteger r2 = r5.idleWorkers     // Catch:{ all -> 0x0040 }
            r2.incrementAndGet()     // Catch:{ all -> 0x0040 }
            r0.start()     // Catch:{ all -> 0x0040 }
            java.util.Set<org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x0040 }
            r2.add(r1)     // Catch:{ all -> 0x0040 }
            java.util.Set<org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x0040 }
            int r2 = r2.size()     // Catch:{ all -> 0x0040 }
            int r4 = r5.largestPoolSize     // Catch:{ all -> 0x0040 }
            if (r2 <= r4) goto L_0x003e
            java.util.Set<org.apache.mina.filter.executor.OrderedThreadPoolExecutor$Worker> r2 = r5.workers     // Catch:{ all -> 0x0040 }
            int r2 = r2.size()     // Catch:{ all -> 0x0040 }
            r5.largestPoolSize = r2     // Catch:{ all -> 0x0040 }
        L_0x003e:
            monitor-exit(r3)     // Catch:{ all -> 0x0040 }
            goto L_0x0010
        L_0x0040:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0040 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.OrderedThreadPoolExecutor.addWorker():void");
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
            if (this.workers.size() > super.getCorePoolSize()) {
                this.waitingSessions.offer(EXIT_SIGNAL);
            }
        }
    }

    public int getMaximumPoolSize() {
        return super.getMaximumPoolSize();
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < super.getCorePoolSize()) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        synchronized (this.workers) {
            super.setMaximumPoolSize(maximumPoolSize);
            for (int difference = this.workers.size() - maximumPoolSize; difference > 0; difference--) {
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
                    this.waitingSessions.offer(EXIT_SIGNAL);
                }
            }
        }
    }

    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> answer = new ArrayList<>();
        while (true) {
            IoSession session = (IoSession) this.waitingSessions.poll();
            if (session == null) {
                return answer;
            }
            if (session == EXIT_SIGNAL) {
                this.waitingSessions.offer(EXIT_SIGNAL);
                Thread.yield();
            } else {
                SessionTasksQueue sessionTasksQueue = (SessionTasksQueue) session.getAttribute(this.TASKS_QUEUE);
                synchronized (sessionTasksQueue.tasksQueue) {
                    for (Runnable task : sessionTasksQueue.tasksQueue) {
                        getQueueHandler().polled(this, (IoEvent) task);
                        answer.add(task);
                    }
                    sessionTasksQueue.tasksQueue.clear();
                }
            }
        }
    }

    private void print(Queue<Runnable> queue, IoEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Adding event ").append(event.getType()).append(" to session ").append(event.getSession().getId());
        boolean first = true;
        sb.append("\nQueue : [");
        for (Runnable elem : queue) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(((IoEvent) elem).getType()).append(", ");
        }
        sb.append("]\n");
        LOGGER.debug(sb.toString());
    }

    public void execute(Runnable task) {
        boolean offerSession;
        if (this.shutdown) {
            rejectTask(task);
        }
        checkTaskType(task);
        IoEvent event = (IoEvent) task;
        IoSession session = event.getSession();
        SessionTasksQueue sessionTasksQueue = getSessionTasksQueue(session);
        Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
        boolean offerEvent = this.eventQueueHandler.accept(this, event);
        if (offerEvent) {
            synchronized (tasksQueue) {
                tasksQueue.offer(event);
                if (sessionTasksQueue.processingCompleted) {
                    boolean unused = sessionTasksQueue.processingCompleted = false;
                    offerSession = true;
                } else {
                    offerSession = false;
                }
                if (LOGGER.isDebugEnabled()) {
                    print(tasksQueue, event);
                }
            }
        } else {
            offerSession = false;
        }
        if (offerSession) {
            this.waitingSessions.offer(session);
        }
        addWorkerIfNecessary();
        if (offerEvent) {
            this.eventQueueHandler.offered(this, event);
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
            for (int i = super.getCorePoolSize() - this.workers.size(); i > 0; i--) {
                addWorker();
                answer++;
            }
        }
        return answer;
    }

    public boolean prestartCoreThread() {
        boolean z;
        synchronized (this.workers) {
            if (this.workers.size() < super.getCorePoolSize()) {
                addWorker();
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public BlockingQueue<Runnable> getQueue() {
        throw new UnsupportedOperationException();
    }

    public void purge() {
    }

    public boolean remove(Runnable task) {
        boolean removed;
        checkTaskType(task);
        IoEvent event = (IoEvent) task;
        SessionTasksQueue sessionTasksQueue = (SessionTasksQueue) event.getSession().getAttribute(this.TASKS_QUEUE);
        if (sessionTasksQueue == null) {
            return false;
        }
        Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
        synchronized (tasksQueue) {
            removed = tasksQueue.remove(task);
        }
        if (!removed) {
            return removed;
        }
        getQueueHandler().polled(this, event);
        return removed;
    }

    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        } else if (corePoolSize > super.getMaximumPoolSize()) {
            throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
        } else {
            synchronized (this.workers) {
                if (super.getCorePoolSize() > corePoolSize) {
                    for (int i = super.getCorePoolSize() - corePoolSize; i > 0; i--) {
                        removeWorker();
                    }
                }
                super.setCorePoolSize(corePoolSize);
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
                IoSession session = fetchSession();
                OrderedThreadPoolExecutor.this.idleWorkers.decrementAndGet();
                if (session == null) {
                    synchronized (OrderedThreadPoolExecutor.this.workers) {
                        if (OrderedThreadPoolExecutor.this.workers.size() > OrderedThreadPoolExecutor.this.getCorePoolSize()) {
                            OrderedThreadPoolExecutor.this.workers.remove(this);
                            break;
                        }
                    }
                }
                try {
                    if (session == OrderedThreadPoolExecutor.EXIT_SIGNAL) {
                        break;
                    }
                    if (session != null) {
                        runTasks(OrderedThreadPoolExecutor.this.getSessionTasksQueue(session));
                    }
                    OrderedThreadPoolExecutor.this.idleWorkers.incrementAndGet();
                } catch (Throwable th) {
                    synchronized (OrderedThreadPoolExecutor.this.workers) {
                        OrderedThreadPoolExecutor.this.workers.remove(this);
                        OrderedThreadPoolExecutor orderedThreadPoolExecutor = OrderedThreadPoolExecutor.this;
                        long unused = orderedThreadPoolExecutor.completedTaskCount = orderedThreadPoolExecutor.completedTaskCount + this.completedTaskCount.get();
                        OrderedThreadPoolExecutor.this.workers.notifyAll();
                        throw th;
                    }
                }
            }
            synchronized (OrderedThreadPoolExecutor.this.workers) {
                OrderedThreadPoolExecutor.this.workers.remove(this);
                OrderedThreadPoolExecutor orderedThreadPoolExecutor2 = OrderedThreadPoolExecutor.this;
                long unused2 = orderedThreadPoolExecutor2.completedTaskCount = orderedThreadPoolExecutor2.completedTaskCount + this.completedTaskCount.get();
                OrderedThreadPoolExecutor.this.workers.notifyAll();
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: org.apache.mina.core.session.IoSession} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private org.apache.mina.core.session.IoSession fetchSession() {
            /*
                r12 = this;
                r7 = 0
                long r2 = java.lang.System.currentTimeMillis()
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r10 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this
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
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r10 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch:{ all -> 0x002f }
                java.util.concurrent.BlockingQueue r10 = r10.waitingSessions     // Catch:{ all -> 0x002f }
                java.util.concurrent.TimeUnit r11 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x002f }
                java.lang.Object r10 = r10.poll(r8, r11)     // Catch:{ all -> 0x002f }
                r0 = r10
                org.apache.mina.core.session.IoSession r0 = (org.apache.mina.core.session.IoSession) r0     // Catch:{ all -> 0x002f }
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
            throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.OrderedThreadPoolExecutor.Worker.fetchSession():org.apache.mina.core.session.IoSession");
        }

        private void runTasks(SessionTasksQueue sessionTasksQueue) {
            Runnable task;
            while (true) {
                Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
                synchronized (tasksQueue) {
                    task = tasksQueue.poll();
                    if (task == null) {
                        boolean unused = sessionTasksQueue.processingCompleted = true;
                        return;
                    }
                }
                OrderedThreadPoolExecutor.this.eventQueueHandler.polled(OrderedThreadPoolExecutor.this, (IoEvent) task);
                runTask(task);
            }
            while (true) {
            }
        }

        private void runTask(Runnable task) {
            OrderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
            boolean ran = false;
            try {
                task.run();
                ran = true;
                OrderedThreadPoolExecutor.this.afterExecute(task, (Throwable) null);
                this.completedTaskCount.incrementAndGet();
            } catch (RuntimeException e) {
                if (!ran) {
                    OrderedThreadPoolExecutor.this.afterExecute(task, e);
                }
                throw e;
            }
        }
    }

    private class SessionTasksQueue {
        /* access modifiers changed from: private */
        public boolean processingCompleted;
        /* access modifiers changed from: private */
        public final Queue<Runnable> tasksQueue;

        private SessionTasksQueue() {
            this.tasksQueue = new ConcurrentLinkedQueue();
            this.processingCompleted = true;
        }
    }
}
