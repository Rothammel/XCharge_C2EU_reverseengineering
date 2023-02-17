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

/* loaded from: classes.dex */
public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final int DEFAULT_INITIAL_THREAD_POOL_SIZE = 0;
    private static final int DEFAULT_KEEP_ALIVE = 30;
    private static final int DEFAULT_MAX_THREAD_POOL = 16;
    private final AttributeKey TASKS_QUEUE;
    private long completedTaskCount;
    private final IoEventQueueHandler eventQueueHandler;
    private final AtomicInteger idleWorkers;
    private volatile int largestPoolSize;
    private volatile boolean shutdown;
    private final BlockingQueue<IoSession> waitingSessions;
    private final Set<Worker> workers;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderedThreadPoolExecutor.class);
    private static final IoSession EXIT_SIGNAL = new DummySession();

    public OrderedThreadPoolExecutor() {
        this(0, 16, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }

    public OrderedThreadPoolExecutor(int maximumPoolSize) {
        this(0, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventQueueHandler eventQueueHandler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), eventQueueHandler);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, null);
    }

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler eventQueueHandler) {
        super(0, 1, keepAliveTime, unit, new SynchronousQueue(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        this.TASKS_QUEUE = new AttributeKey(getClass(), "tasksQueue");
        this.waitingSessions = new LinkedBlockingQueue();
        this.workers = new HashSet();
        this.idleWorkers = new AtomicInteger();
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (maximumPoolSize == 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }
        super.setCorePoolSize(corePoolSize);
        super.setMaximumPoolSize(maximumPoolSize);
        if (eventQueueHandler == null) {
            this.eventQueueHandler = IoEventQueueHandler.NOOP;
        } else {
            this.eventQueueHandler = eventQueueHandler;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SessionTasksQueue getSessionTasksQueue(IoSession session) {
        SessionTasksQueue queue = (SessionTasksQueue) session.getAttribute(this.TASKS_QUEUE);
        if (queue == null) {
            SessionTasksQueue queue2 = new SessionTasksQueue();
            SessionTasksQueue oldQueue = (SessionTasksQueue) session.setAttributeIfAbsent(this.TASKS_QUEUE, queue2);
            if (oldQueue != null) {
                return oldQueue;
            }
            return queue2;
        }
        return queue;
    }

    public IoEventQueueHandler getQueueHandler() {
        return this.eventQueueHandler;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
    }

    private void addWorker() {
        synchronized (this.workers) {
            if (this.workers.size() < super.getMaximumPoolSize()) {
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
            if (this.workers.size() > super.getCorePoolSize()) {
                this.waitingSessions.offer(EXIT_SIGNAL);
            }
        }
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getMaximumPoolSize() {
        return super.getMaximumPoolSize();
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
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
                    this.waitingSessions.offer(EXIT_SIGNAL);
                }
            }
        }
    }

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.ExecutorService
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> answer = new ArrayList<>();
        while (true) {
            IoSession session = this.waitingSessions.poll();
            if (session != null) {
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
            } else {
                return answer;
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

    @Override // java.util.concurrent.ThreadPoolExecutor, java.util.concurrent.Executor
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
                if (!sessionTasksQueue.processingCompleted) {
                    offerSession = false;
                } else {
                    sessionTasksQueue.processingCompleted = false;
                    offerSession = true;
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
            for (int i = super.getCorePoolSize() - this.workers.size(); i > 0; i--) {
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
            if (this.workers.size() < super.getCorePoolSize()) {
                addWorker();
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public BlockingQueue<Runnable> getQueue() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void purge() {
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public boolean remove(Runnable task) {
        boolean removed;
        checkTaskType(task);
        IoEvent event = (IoEvent) task;
        IoSession session = event.getSession();
        SessionTasksQueue sessionTasksQueue = (SessionTasksQueue) session.getAttribute(this.TASKS_QUEUE);
        if (sessionTasksQueue == null) {
            return false;
        }
        Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
        synchronized (tasksQueue) {
            removed = tasksQueue.remove(task);
        }
        if (removed) {
            getQueueHandler().polled(this, event);
            return removed;
        }
        return removed;
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }
        if (corePoolSize > super.getMaximumPoolSize()) {
            throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
        }
        synchronized (this.workers) {
            if (super.getCorePoolSize() > corePoolSize) {
                for (int i = super.getCorePoolSize() - corePoolSize; i > 0; i--) {
                    removeWorker();
                }
            }
            super.setCorePoolSize(corePoolSize);
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
            r8.this$0.workers.remove(r8);
         */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r8 = this;
                java.lang.Thread r1 = java.lang.Thread.currentThread()
                r8.thread = r1
            L6:
                org.apache.mina.core.session.IoSession r0 = r8.fetchSession()     // Catch: java.lang.Throwable -> L7f
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L7f
                java.util.concurrent.atomic.AtomicInteger r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$500(r1)     // Catch: java.lang.Throwable -> L7f
                r1.decrementAndGet()     // Catch: java.lang.Throwable -> L7f
                if (r0 != 0) goto L64
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L7f
                java.util.Set r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)     // Catch: java.lang.Throwable -> L7f
                monitor-enter(r2)     // Catch: java.lang.Throwable -> L7f
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lab
                java.util.Set r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)     // Catch: java.lang.Throwable -> Lab
                int r1 = r1.size()     // Catch: java.lang.Throwable -> Lab
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lab
                int r3 = r3.getCorePoolSize()     // Catch: java.lang.Throwable -> Lab
                if (r1 <= r3) goto L63
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lab
                java.util.Set r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)     // Catch: java.lang.Throwable -> Lab
                r1.remove(r8)     // Catch: java.lang.Throwable -> Lab
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lab
            L38:
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this
                java.util.Set r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)
                monitor-enter(r2)
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb9
                java.util.Set r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)     // Catch: java.lang.Throwable -> Lb9
                r1.remove(r8)     // Catch: java.lang.Throwable -> Lb9
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb9
                long r4 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$900(r1)     // Catch: java.lang.Throwable -> Lb9
                java.util.concurrent.atomic.AtomicLong r3 = r8.completedTaskCount     // Catch: java.lang.Throwable -> Lb9
                long r6 = r3.get()     // Catch: java.lang.Throwable -> Lb9
                long r4 = r4 + r6
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$902(r1, r4)     // Catch: java.lang.Throwable -> Lb9
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lb9
                java.util.Set r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r1)     // Catch: java.lang.Throwable -> Lb9
                r1.notifyAll()     // Catch: java.lang.Throwable -> Lb9
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lb9
                return
            L63:
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lab
            L64:
                org.apache.mina.core.session.IoSession r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$700()     // Catch: java.lang.Throwable -> L7f
                if (r0 == r1) goto L38
                if (r0 == 0) goto L75
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lae
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor$SessionTasksQueue r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$800(r1, r0)     // Catch: java.lang.Throwable -> Lae
                r8.runTasks(r1)     // Catch: java.lang.Throwable -> Lae
            L75:
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L7f
                java.util.concurrent.atomic.AtomicInteger r1 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$500(r1)     // Catch: java.lang.Throwable -> L7f
                r1.incrementAndGet()     // Catch: java.lang.Throwable -> L7f
                goto L6
            L7f:
                r1 = move-exception
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this
                java.util.Set r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r2)
                monitor-enter(r2)
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lbc
                java.util.Set r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r3)     // Catch: java.lang.Throwable -> Lbc
                r3.remove(r8)     // Catch: java.lang.Throwable -> Lbc
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lbc
                long r4 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$900(r3)     // Catch: java.lang.Throwable -> Lbc
                java.util.concurrent.atomic.AtomicLong r6 = r8.completedTaskCount     // Catch: java.lang.Throwable -> Lbc
                long r6 = r6.get()     // Catch: java.lang.Throwable -> Lbc
                long r4 = r4 + r6
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$902(r3, r4)     // Catch: java.lang.Throwable -> Lbc
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> Lbc
                java.util.Set r3 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$600(r3)     // Catch: java.lang.Throwable -> Lbc
                r3.notifyAll()     // Catch: java.lang.Throwable -> Lbc
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lbc
                throw r1
            Lab:
                r1 = move-exception
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lab
                throw r1     // Catch: java.lang.Throwable -> L7f
            Lae:
                r1 = move-exception
                org.apache.mina.filter.executor.OrderedThreadPoolExecutor r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.this     // Catch: java.lang.Throwable -> L7f
                java.util.concurrent.atomic.AtomicInteger r2 = org.apache.mina.filter.executor.OrderedThreadPoolExecutor.access$500(r2)     // Catch: java.lang.Throwable -> L7f
                r2.incrementAndGet()     // Catch: java.lang.Throwable -> L7f
                throw r1     // Catch: java.lang.Throwable -> L7f
            Lb9:
                r1 = move-exception
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lb9
                throw r1
            Lbc:
                r1 = move-exception
                monitor-exit(r2)     // Catch: java.lang.Throwable -> Lbc
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.executor.OrderedThreadPoolExecutor.Worker.run():void");
        }

        private IoSession fetchSession() {
            IoSession session = null;
            long currentTime = System.currentTimeMillis();
            long deadline = currentTime + OrderedThreadPoolExecutor.this.getKeepAliveTime(TimeUnit.MILLISECONDS);
            while (true) {
                long waitTime = deadline - currentTime;
                if (waitTime <= 0) {
                    break;
                }
                try {
                    session = (IoSession) OrderedThreadPoolExecutor.this.waitingSessions.poll(waitTime, TimeUnit.MILLISECONDS);
                    if (session != null) {
                        break;
                    }
                    currentTime = System.currentTimeMillis();
                    break;
                } catch (InterruptedException e) {
                }
            }
            return session;
        }

        private void runTasks(SessionTasksQueue sessionTasksQueue) {
            Runnable task;
            while (true) {
                Queue<Runnable> tasksQueue = sessionTasksQueue.tasksQueue;
                synchronized (tasksQueue) {
                    task = tasksQueue.poll();
                    if (task == null) {
                        sessionTasksQueue.processingCompleted = true;
                        return;
                    }
                }
                OrderedThreadPoolExecutor.this.eventQueueHandler.polled(OrderedThreadPoolExecutor.this, (IoEvent) task);
                runTask(task);
            }
        }

        private void runTask(Runnable task) {
            OrderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
            boolean ran = false;
            try {
                task.run();
                ran = true;
                OrderedThreadPoolExecutor.this.afterExecute(task, null);
                this.completedTaskCount.incrementAndGet();
            } catch (RuntimeException e) {
                if (!ran) {
                    OrderedThreadPoolExecutor.this.afterExecute(task, e);
                }
                throw e;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SessionTasksQueue {
        private boolean processingCompleted;
        private final Queue<Runnable> tasksQueue;

        private SessionTasksQueue() {
            this.tasksQueue = new ConcurrentLinkedQueue();
            this.processingCompleted = true;
        }
    }
}
