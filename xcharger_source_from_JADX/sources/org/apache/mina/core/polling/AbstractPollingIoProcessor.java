package org.apache.mina.core.polling;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.channels.ClosedSelectorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.SessionState;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.util.NamePreservingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPollingIoProcessor<S extends AbstractIoSession> implements IoProcessor<S> {
    /* access modifiers changed from: private */
    public static final Logger LOG = LoggerFactory.getLogger((Class<?>) IoProcessor.class);
    private static final long SELECT_TIMEOUT = 1000;
    private static final ConcurrentHashMap<Class<?>, AtomicInteger> threadIds = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public final DefaultIoFuture disposalFuture = new DefaultIoFuture((IoSession) null);
    /* access modifiers changed from: private */
    public final Object disposalLock = new Object();
    private volatile boolean disposed;
    /* access modifiers changed from: private */
    public volatile boolean disposing;
    private final Executor executor;
    private final Queue<S> flushingSessions = new ConcurrentLinkedQueue();
    /* access modifiers changed from: private */
    public long lastIdleCheckTime;
    /* access modifiers changed from: private */
    public final Queue<S> newSessions = new ConcurrentLinkedQueue();
    /* access modifiers changed from: private */
    public final AtomicReference<AbstractPollingIoProcessor<S>.Processor> processorRef = new AtomicReference<>();
    private final Queue<S> removingSessions = new ConcurrentLinkedQueue();
    private final String threadName;
    private final Queue<S> trafficControllingSessions = new ConcurrentLinkedQueue();
    protected AtomicBoolean wakeupCalled = new AtomicBoolean(false);

    /* access modifiers changed from: protected */
    public abstract Iterator<S> allSessions();

    /* access modifiers changed from: protected */
    public abstract void destroy(S s) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void doDispose() throws Exception;

    /* access modifiers changed from: protected */
    public abstract SessionState getState(S s);

    /* access modifiers changed from: protected */
    public abstract void init(S s) throws Exception;

    /* access modifiers changed from: protected */
    public abstract boolean isBrokenConnection() throws IOException;

    /* access modifiers changed from: protected */
    public abstract boolean isInterestedInRead(S s);

    /* access modifiers changed from: protected */
    public abstract boolean isInterestedInWrite(S s);

    /* access modifiers changed from: protected */
    public abstract boolean isReadable(S s);

    /* access modifiers changed from: protected */
    public abstract boolean isSelectorEmpty();

    /* access modifiers changed from: protected */
    public abstract boolean isWritable(S s);

    /* access modifiers changed from: protected */
    public abstract int read(S s, IoBuffer ioBuffer) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void registerNewSelector() throws IOException;

    /* access modifiers changed from: protected */
    public abstract int select() throws Exception;

    /* access modifiers changed from: protected */
    public abstract int select(long j) throws Exception;

    /* access modifiers changed from: protected */
    public abstract Iterator<S> selectedSessions();

    /* access modifiers changed from: protected */
    public abstract void setInterestedInRead(S s, boolean z) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void setInterestedInWrite(S s, boolean z) throws Exception;

    /* access modifiers changed from: protected */
    public abstract int transferFile(S s, FileRegion fileRegion, int i) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void wakeup();

    /* access modifiers changed from: protected */
    public abstract int write(S s, IoBuffer ioBuffer, int i) throws IOException;

    protected AbstractPollingIoProcessor(Executor executor2) {
        if (executor2 == null) {
            throw new IllegalArgumentException("executor");
        }
        this.threadName = nextThreadName();
        this.executor = executor2;
    }

    private String nextThreadName() {
        int newThreadId;
        Class<?> cls = getClass();
        AtomicInteger threadId = threadIds.putIfAbsent(cls, new AtomicInteger(1));
        if (threadId == null) {
            newThreadId = 1;
        } else {
            newThreadId = threadId.incrementAndGet();
        }
        return cls.getSimpleName() + '-' + newThreadId;
    }

    public final boolean isDisposing() {
        return this.disposing;
    }

    public final boolean isDisposed() {
        return this.disposed;
    }

    public final void dispose() {
        if (!this.disposed && !this.disposing) {
            synchronized (this.disposalLock) {
                this.disposing = true;
                startupProcessor();
            }
            this.disposalFuture.awaitUninterruptibly();
            this.disposed = true;
        }
    }

    public final void add(S session) {
        if (this.disposed || this.disposing) {
            throw new IllegalStateException("Already disposed.");
        }
        this.newSessions.add(session);
        startupProcessor();
    }

    public final void remove(S session) {
        scheduleRemove(session);
        startupProcessor();
    }

    /* access modifiers changed from: private */
    public void scheduleRemove(S session) {
        if (!this.removingSessions.contains(session)) {
            this.removingSessions.add(session);
        }
    }

    public void write(S session, WriteRequest writeRequest) {
        session.getWriteRequestQueue().offer(session, writeRequest);
        if (!session.isWriteSuspended()) {
            flush(session);
        }
    }

    public final void flush(S session) {
        if (session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
            wakeup();
        }
    }

    private void scheduleFlush(S session) {
        if (session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
        }
    }

    public final void updateTrafficMask(S session) {
        this.trafficControllingSessions.add(session);
        wakeup();
    }

    private void startupProcessor() {
        if (this.processorRef.get() == null) {
            AbstractPollingIoProcessor<S>.Processor processor = new Processor();
            if (this.processorRef.compareAndSet((Object) null, processor)) {
                this.executor.execute(new NamePreservingRunnable(processor, this.threadName));
            }
        }
        wakeup();
    }

    /* access modifiers changed from: private */
    public int handleNewSessions() {
        int addedSessions = 0;
        S session = (AbstractIoSession) this.newSessions.poll();
        while (session != null) {
            if (addNow(session)) {
                addedSessions++;
            }
            session = (AbstractIoSession) this.newSessions.poll();
        }
        return addedSessions;
    }

    private boolean addNow(S session) {
        try {
            init(session);
            session.getService().getFilterChainBuilder().buildFilterChain(session.getFilterChain());
            ((AbstractIoService) session.getService()).getListeners().fireSessionCreated(session);
            return true;
        } catch (Exception e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
            try {
                destroy(session);
                return false;
            } catch (Exception e1) {
                ExceptionMonitor.getInstance().exceptionCaught(e1);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public int removeSessions() {
        int removedSessions = 0;
        S session = (AbstractIoSession) this.removingSessions.poll();
        while (session != null) {
            SessionState state = getState(session);
            switch (state) {
                case OPENED:
                    if (!removeNow(session)) {
                        break;
                    } else {
                        removedSessions++;
                        break;
                    }
                case CLOSING:
                    removedSessions++;
                    break;
                case OPENING:
                    this.newSessions.remove(session);
                    if (!removeNow(session)) {
                        break;
                    } else {
                        removedSessions++;
                        break;
                    }
                default:
                    throw new IllegalStateException(String.valueOf(state));
            }
            session = (AbstractIoSession) this.removingSessions.poll();
        }
        return removedSessions;
    }

    private boolean removeNow(S session) {
        clearWriteRequestQueue(session);
        try {
            destroy(session);
            try {
                clearWriteRequestQueue(session);
                ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
            } catch (Exception e) {
                session.getFilterChain().fireExceptionCaught(e);
            }
            return true;
        } catch (Exception e2) {
            session.getFilterChain().fireExceptionCaught(e2);
            try {
                clearWriteRequestQueue(session);
                ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
            } catch (Exception e3) {
                session.getFilterChain().fireExceptionCaught(e3);
            }
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            try {
                clearWriteRequestQueue(session);
                ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
            } catch (Exception e4) {
                session.getFilterChain().fireExceptionCaught(e4);
            }
            throw th2;
        }
    }

    private void clearWriteRequestQueue(S session) {
        WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        List<WriteRequest> failedRequests = new ArrayList<>();
        WriteRequest req = writeRequestQueue.poll(session);
        if (req != null) {
            Object message = req.getMessage();
            if (message instanceof IoBuffer) {
                IoBuffer buf = (IoBuffer) message;
                if (buf.hasRemaining()) {
                    buf.reset();
                    failedRequests.add(req);
                } else {
                    session.getFilterChain().fireMessageSent(req);
                }
            } else {
                failedRequests.add(req);
            }
            while (true) {
                WriteRequest req2 = writeRequestQueue.poll(session);
                if (req2 == null) {
                    break;
                }
                failedRequests.add(req2);
            }
        }
        if (!failedRequests.isEmpty()) {
            WriteToClosedSessionException cause = new WriteToClosedSessionException((Collection<WriteRequest>) failedRequests);
            for (WriteRequest r : failedRequests) {
                session.decreaseScheduledBytesAndMessages(r);
                r.getFuture().setException(cause);
            }
            session.getFilterChain().fireExceptionCaught(cause);
        }
    }

    /* access modifiers changed from: private */
    public void process() throws Exception {
        Iterator<S> i = selectedSessions();
        while (i.hasNext()) {
            process((AbstractIoSession) i.next());
            i.remove();
        }
    }

    private void process(S session) {
        if (isReadable(session) && !session.isReadSuspended()) {
            read(session);
        }
        if (isWritable(session) && !session.isWriteSuspended() && session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
        }
    }

    private void read(S session) {
        int ret;
        IoSessionConfig config = session.getConfig();
        IoBuffer buf = IoBuffer.allocate(config.getReadBufferSize());
        boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
        int readBytes = 0;
        if (hasFragmentation) {
            do {
                try {
                    ret = read(session, buf);
                    if (ret > 0) {
                        readBytes += ret;
                    }
                } catch (Exception e) {
                    if ((e instanceof IOException) && (!(e instanceof PortUnreachableException) || !AbstractDatagramSessionConfig.class.isAssignableFrom(config.getClass()) || ((AbstractDatagramSessionConfig) config).isCloseOnPortUnreachable())) {
                        scheduleRemove(session);
                    }
                    session.getFilterChain().fireExceptionCaught(e);
                    return;
                } catch (Throwable th) {
                    buf.flip();
                    throw th;
                }
            } while (buf.hasRemaining());
        } else {
            ret = read(session, buf);
            if (ret > 0) {
                readBytes = ret;
            }
        }
        buf.flip();
        if (readBytes > 0) {
            session.getFilterChain().fireMessageReceived(buf);
            if (hasFragmentation) {
                if ((readBytes << 1) < config.getReadBufferSize()) {
                    session.decreaseReadBufferSize();
                } else if (readBytes == config.getReadBufferSize()) {
                    session.increaseReadBufferSize();
                }
            }
        }
        if (ret < 0) {
            session.getFilterChain().fireInputClosed();
        }
    }

    /* access modifiers changed from: private */
    public void notifyIdleSessions(long currentTime) throws Exception {
        if (currentTime - this.lastIdleCheckTime >= 1000) {
            this.lastIdleCheckTime = currentTime;
            AbstractIoSession.notifyIdleness(allSessions(), currentTime);
        }
    }

    /* access modifiers changed from: private */
    public void flush(long currentTime) {
        if (!this.flushingSessions.isEmpty()) {
            do {
                S session = (AbstractIoSession) this.flushingSessions.poll();
                if (session != null) {
                    session.unscheduledForFlush();
                    SessionState state = getState(session);
                    switch (state) {
                        case OPENED:
                            try {
                                if (flushNow(session, currentTime) && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
                                    scheduleFlush(session);
                                    break;
                                }
                            } catch (Exception e) {
                                scheduleRemove(session);
                                session.closeNow();
                                session.getFilterChain().fireExceptionCaught(e);
                                break;
                            }
                        case CLOSING:
                            break;
                        case OPENING:
                            scheduleFlush(session);
                            return;
                        default:
                            throw new IllegalStateException(String.valueOf(state));
                    }
                } else {
                    return;
                }
            } while (!this.flushingSessions.isEmpty());
        }
    }

    private boolean flushNow(S session, long currentTime) {
        int localWrittenBytes;
        if (!session.isConnected()) {
            scheduleRemove(session);
            return false;
        }
        boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
        WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        int maxWrittenBytes = session.getConfig().getMaxReadBufferSize() + (session.getConfig().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        WriteRequest req = null;
        try {
            setInterestedInWrite(session, false);
            do {
                req = session.getCurrentWriteRequest();
                if (req == null) {
                    req = writeRequestQueue.poll(session);
                    if (req == null) {
                        break;
                    }
                    session.setCurrentWriteRequest(req);
                }
                Object message = req.getMessage();
                if (message instanceof IoBuffer) {
                    localWrittenBytes = writeBuffer(session, req, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((IoBuffer) message).hasRemaining()) {
                        int writtenBytes2 = writtenBytes + localWrittenBytes;
                        setInterestedInWrite(session, true);
                        return false;
                    }
                } else if (message instanceof FileRegion) {
                    localWrittenBytes = writeFile(session, req, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((FileRegion) message).getRemainingBytes() > 0) {
                        int writtenBytes3 = writtenBytes + localWrittenBytes;
                        setInterestedInWrite(session, true);
                        return false;
                    }
                } else {
                    throw new IllegalStateException("Don't know how to handle message of type '" + message.getClass().getName() + "'.  Are you missing a protocol encoder?");
                }
                if (localWrittenBytes != 0) {
                    writtenBytes += localWrittenBytes;
                    if (writtenBytes >= maxWrittenBytes) {
                        scheduleFlush(session);
                        return false;
                    }
                } else if (!req.equals(AbstractIoSession.MESSAGE_SENT_REQUEST)) {
                    setInterestedInWrite(session, true);
                    return false;
                }
                if (message instanceof IoBuffer) {
                    ((IoBuffer) message).free();
                    continue;
                }
            } while (writtenBytes < maxWrittenBytes);
            return true;
        } catch (Exception e) {
            if (req != null) {
                req.getFuture().setException(e);
            }
            session.getFilterChain().fireExceptionCaught(e);
            return false;
        }
    }

    private int writeBuffer(S session, WriteRequest req, boolean hasFragmentation, int maxLength, long currentTime) throws Exception {
        int length;
        IoBuffer buf = (IoBuffer) req.getMessage();
        int localWrittenBytes = 0;
        if (buf.hasRemaining()) {
            if (hasFragmentation) {
                length = Math.min(buf.remaining(), maxLength);
            } else {
                length = buf.remaining();
            }
            try {
                localWrittenBytes = write(session, buf, length);
            } catch (IOException e) {
                buf.free();
                session.closeNow();
                removeNow(session);
                return 0;
            }
        }
        session.increaseWrittenBytes(localWrittenBytes, currentTime);
        if (!buf.hasRemaining() || (!hasFragmentation && localWrittenBytes != 0)) {
            if (req.getOriginalRequest().getMessage() instanceof IoBuffer) {
                IoBuffer buf2 = (IoBuffer) req.getOriginalRequest().getMessage();
                int pos = buf2.position();
                buf2.reset();
                fireMessageSent(session, req);
                buf2.position(pos);
            } else {
                fireMessageSent(session, req);
            }
        }
        return localWrittenBytes;
    }

    private int writeFile(S session, WriteRequest req, boolean hasFragmentation, int maxLength, long currentTime) throws Exception {
        int localWrittenBytes;
        int length;
        FileRegion region = (FileRegion) req.getMessage();
        if (region.getRemainingBytes() > 0) {
            if (hasFragmentation) {
                length = (int) Math.min(region.getRemainingBytes(), (long) maxLength);
            } else {
                length = (int) Math.min(2147483647L, region.getRemainingBytes());
            }
            localWrittenBytes = transferFile(session, region, length);
            region.update((long) localWrittenBytes);
        } else {
            localWrittenBytes = 0;
        }
        session.increaseWrittenBytes(localWrittenBytes, currentTime);
        if (region.getRemainingBytes() <= 0 || (!hasFragmentation && localWrittenBytes != 0)) {
            fireMessageSent(session, req);
        }
        return localWrittenBytes;
    }

    private void fireMessageSent(S session, WriteRequest req) {
        session.setCurrentWriteRequest((WriteRequest) null);
        session.getFilterChain().fireMessageSent(req);
    }

    /* access modifiers changed from: private */
    public void updateTrafficMask() {
        int queueSize = this.trafficControllingSessions.size();
        while (queueSize > 0) {
            S session = (AbstractIoSession) this.trafficControllingSessions.poll();
            if (session != null) {
                SessionState state = getState(session);
                switch (state) {
                    case OPENED:
                        updateTrafficControl(session);
                        break;
                    case CLOSING:
                        break;
                    case OPENING:
                        this.trafficControllingSessions.add(session);
                        break;
                    default:
                        throw new IllegalStateException(String.valueOf(state));
                }
                queueSize--;
            } else {
                return;
            }
        }
    }

    public void updateTrafficControl(S session) {
        boolean z = true;
        try {
            setInterestedInRead(session, !session.isReadSuspended());
        } catch (Exception e) {
            session.getFilterChain().fireExceptionCaught(e);
        }
        try {
            if (session.getWriteRequestQueue().isEmpty(session) || session.isWriteSuspended()) {
                z = false;
            }
            setInterestedInWrite(session, z);
        } catch (Exception e2) {
            session.getFilterChain().fireExceptionCaught(e2);
        }
    }

    private class Processor implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled = (!AbstractPollingIoProcessor.class.desiredAssertionStatus());

        private Processor() {
        }

        public void run() {
            if ($assertionsDisabled || AbstractPollingIoProcessor.this.processorRef.get() == this) {
                int nSessions = 0;
                long unused = AbstractPollingIoProcessor.this.lastIdleCheckTime = System.currentTimeMillis();
                int nbTries = 10;
                while (true) {
                    try {
                        long t0 = System.currentTimeMillis();
                        int selected = AbstractPollingIoProcessor.this.select(1000);
                        long delta = System.currentTimeMillis() - t0;
                        if (AbstractPollingIoProcessor.this.wakeupCalled.getAndSet(false) || selected != 0 || delta >= 100) {
                            nbTries = 10;
                        } else if (AbstractPollingIoProcessor.this.isBrokenConnection()) {
                            AbstractPollingIoProcessor.LOG.warn("Broken connection");
                        } else if (nbTries == 0) {
                            AbstractPollingIoProcessor.LOG.warn("Create a new selector. Selected is 0, delta = " + delta);
                            AbstractPollingIoProcessor.this.registerNewSelector();
                            nbTries = 10;
                        } else {
                            nbTries--;
                        }
                        int nSessions2 = nSessions + AbstractPollingIoProcessor.this.handleNewSessions();
                        AbstractPollingIoProcessor.this.updateTrafficMask();
                        if (selected > 0) {
                            AbstractPollingIoProcessor.this.process();
                        }
                        long currentTime = System.currentTimeMillis();
                        AbstractPollingIoProcessor.this.flush(currentTime);
                        nSessions = nSessions2 - AbstractPollingIoProcessor.this.removeSessions();
                        AbstractPollingIoProcessor.this.notifyIdleSessions(currentTime);
                        if (nSessions == 0) {
                            AbstractPollingIoProcessor.this.processorRef.set((Object) null);
                            if (!AbstractPollingIoProcessor.this.newSessions.isEmpty() || !AbstractPollingIoProcessor.this.isSelectorEmpty()) {
                                if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                                    throw new AssertionError();
                                } else if (!AbstractPollingIoProcessor.this.processorRef.compareAndSet((Object) null, this)) {
                                    if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                                        throw new AssertionError();
                                    }
                                } else if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() != this) {
                                    throw new AssertionError();
                                }
                            } else if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                                throw new AssertionError();
                            }
                        }
                        if (AbstractPollingIoProcessor.this.isDisposing()) {
                            boolean hasKeys = false;
                            Iterator<S> i = AbstractPollingIoProcessor.this.allSessions();
                            while (i.hasNext()) {
                                IoSession session = (IoSession) i.next();
                                if (session.isActive()) {
                                    AbstractPollingIoProcessor.this.scheduleRemove((AbstractIoSession) session);
                                    hasKeys = true;
                                }
                            }
                            if (hasKeys) {
                                AbstractPollingIoProcessor.this.wakeup();
                            }
                        }
                    } catch (ClosedSelectorException cse) {
                        ExceptionMonitor.getInstance().exceptionCaught(cse);
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            ExceptionMonitor.getInstance().exceptionCaught(e1);
                        }
                    }
                }
                try {
                    synchronized (AbstractPollingIoProcessor.this.disposalLock) {
                        if (AbstractPollingIoProcessor.this.disposing) {
                            AbstractPollingIoProcessor.this.doDispose();
                        }
                    }
                    AbstractPollingIoProcessor.this.disposalFuture.setValue(1);
                } catch (Exception e2) {
                    try {
                        ExceptionMonitor.getInstance().exceptionCaught(e2);
                        AbstractPollingIoProcessor.this.disposalFuture.setValue(1);
                    } catch (Throwable th) {
                        AbstractPollingIoProcessor.this.disposalFuture.setValue(true);
                        throw th;
                    }
                }
            } else {
                throw new AssertionError();
            }
        }
    }
}
