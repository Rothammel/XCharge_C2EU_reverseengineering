package org.apache.mina.core.polling;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.channels.ClosedSelectorException;
import java.util.ArrayList;
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
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoServiceListenerSupport;
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

/* loaded from: classes.dex */
public abstract class AbstractPollingIoProcessor<S extends AbstractIoSession> implements IoProcessor<S> {
    private static final long SELECT_TIMEOUT = 1000;
    private volatile boolean disposed;
    private volatile boolean disposing;
    private final Executor executor;
    private long lastIdleCheckTime;
    private final String threadName;
    private static final Logger LOG = LoggerFactory.getLogger(IoProcessor.class);
    private static final ConcurrentHashMap<Class<?>, AtomicInteger> threadIds = new ConcurrentHashMap<>();
    private final Queue<S> newSessions = new ConcurrentLinkedQueue();
    private final Queue<S> removingSessions = new ConcurrentLinkedQueue();
    private final Queue<S> flushingSessions = new ConcurrentLinkedQueue();
    private final Queue<S> trafficControllingSessions = new ConcurrentLinkedQueue();
    private final AtomicReference<AbstractPollingIoProcessor<S>.Processor> processorRef = new AtomicReference<>();
    private final Object disposalLock = new Object();
    private final DefaultIoFuture disposalFuture = new DefaultIoFuture(null);
    protected AtomicBoolean wakeupCalled = new AtomicBoolean(false);

    protected abstract Iterator<S> allSessions();

    protected abstract void destroy(S s) throws Exception;

    protected abstract void doDispose() throws Exception;

    protected abstract SessionState getState(S s);

    protected abstract void init(S s) throws Exception;

    protected abstract boolean isBrokenConnection() throws IOException;

    protected abstract boolean isInterestedInRead(S s);

    protected abstract boolean isInterestedInWrite(S s);

    protected abstract boolean isReadable(S s);

    protected abstract boolean isSelectorEmpty();

    protected abstract boolean isWritable(S s);

    protected abstract int read(S s, IoBuffer ioBuffer) throws Exception;

    protected abstract void registerNewSelector() throws IOException;

    protected abstract int select() throws Exception;

    protected abstract int select(long j) throws Exception;

    protected abstract Iterator<S> selectedSessions();

    protected abstract void setInterestedInRead(S s, boolean z) throws Exception;

    protected abstract void setInterestedInWrite(S s, boolean z) throws Exception;

    protected abstract int transferFile(S s, FileRegion fileRegion, int i) throws Exception;

    protected abstract void wakeup();

    protected abstract int write(S s, IoBuffer ioBuffer, int i) throws IOException;

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void add(IoSession ioSession) {
        add((AbstractPollingIoProcessor<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void flush(IoSession ioSession) {
        flush((AbstractPollingIoProcessor<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void remove(IoSession ioSession) {
        remove((AbstractPollingIoProcessor<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void updateTrafficControl(IoSession ioSession) {
        updateTrafficControl((AbstractPollingIoProcessor<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void write(IoSession ioSession, WriteRequest writeRequest) {
        write((AbstractPollingIoProcessor<S>) ((AbstractIoSession) ioSession), writeRequest);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractPollingIoProcessor(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor");
        }
        this.threadName = nextThreadName();
        this.executor = executor;
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

    @Override // org.apache.mina.core.service.IoProcessor
    public final boolean isDisposing() {
        return this.disposing;
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public final boolean isDisposed() {
        return this.disposed;
    }

    @Override // org.apache.mina.core.service.IoProcessor
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

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleRemove(S session) {
        if (!this.removingSessions.contains(session)) {
            this.removingSessions.add(session);
        }
    }

    public void write(S session, WriteRequest writeRequest) {
        WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        writeRequestQueue.offer(session, writeRequest);
        if (!session.isWriteSuspended()) {
            flush((AbstractPollingIoProcessor<S>) session);
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
            if (this.processorRef.compareAndSet(null, processor)) {
                this.executor.execute(new NamePreservingRunnable(processor, this.threadName));
            }
        }
        wakeup();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int handleNewSessions() {
        int addedSessions = 0;
        S session = this.newSessions.poll();
        while (session != null) {
            if (addNow(session)) {
                addedSessions++;
            }
            S session2 = this.newSessions.poll();
            session = session2;
        }
        return addedSessions;
    }

    private boolean addNow(S session) {
        try {
            init(session);
            IoFilterChainBuilder chainBuilder = session.getService().getFilterChainBuilder();
            chainBuilder.buildFilterChain(session.getFilterChain());
            IoServiceListenerSupport listeners = ((AbstractIoService) session.getService()).getListeners();
            listeners.fireSessionCreated(session);
            return true;
        } catch (Exception e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
            try {
                try {
                    destroy(session);
                    return false;
                } catch (Exception e1) {
                    ExceptionMonitor.getInstance().exceptionCaught(e1);
                    return false;
                }
            } finally {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int removeSessions() {
        int removedSessions = 0;
        S session = this.removingSessions.poll();
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
            S session2 = this.removingSessions.poll();
            session = session2;
        }
        return removedSessions;
    }

    private boolean removeNow(S session) {
        clearWriteRequestQueue(session);
        try {
            try {
                destroy(session);
                try {
                    clearWriteRequestQueue(session);
                    ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
                } catch (Exception e) {
                    IoFilterChain filterChain = session.getFilterChain();
                    filterChain.fireExceptionCaught(e);
                }
                return true;
            } catch (Exception e2) {
                IoFilterChain filterChain2 = session.getFilterChain();
                filterChain2.fireExceptionCaught(e2);
                try {
                    clearWriteRequestQueue(session);
                    ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
                } catch (Exception e3) {
                    IoFilterChain filterChain3 = session.getFilterChain();
                    filterChain3.fireExceptionCaught(e3);
                }
                return false;
            }
        } catch (Throwable th) {
            try {
                clearWriteRequestQueue(session);
                ((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
            } catch (Exception e4) {
                IoFilterChain filterChain4 = session.getFilterChain();
                filterChain4.fireExceptionCaught(e4);
            }
            throw th;
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
                    IoFilterChain filterChain = session.getFilterChain();
                    filterChain.fireMessageSent(req);
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
            WriteToClosedSessionException cause = new WriteToClosedSessionException(failedRequests);
            for (WriteRequest r : failedRequests) {
                session.decreaseScheduledBytesAndMessages(r);
                r.getFuture().setException(cause);
            }
            IoFilterChain filterChain2 = session.getFilterChain();
            filterChain2.fireExceptionCaught(cause);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void process() throws Exception {
        Iterator<S> i = selectedSessions();
        while (i.hasNext()) {
            S session = i.next();
            process(session);
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
        int bufferSize = config.getReadBufferSize();
        IoBuffer buf = IoBuffer.allocate(bufferSize);
        boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
        int readBytes = 0;
        try {
            if (hasFragmentation) {
                do {
                    ret = read(session, buf);
                    if (ret <= 0) {
                        break;
                    }
                    readBytes += ret;
                } while (buf.hasRemaining());
            } else {
                ret = read(session, buf);
                if (ret > 0) {
                    readBytes = ret;
                }
            }
            buf.flip();
            if (readBytes > 0) {
                IoFilterChain filterChain = session.getFilterChain();
                filterChain.fireMessageReceived(buf);
                if (hasFragmentation) {
                    if ((readBytes << 1) < config.getReadBufferSize()) {
                        session.decreaseReadBufferSize();
                    } else if (readBytes == config.getReadBufferSize()) {
                        session.increaseReadBufferSize();
                    }
                }
            }
            if (ret < 0) {
                IoFilterChain filterChain2 = session.getFilterChain();
                filterChain2.fireInputClosed();
            }
        } catch (Exception e) {
            if ((e instanceof IOException) && (!(e instanceof PortUnreachableException) || !AbstractDatagramSessionConfig.class.isAssignableFrom(config.getClass()) || ((AbstractDatagramSessionConfig) config).isCloseOnPortUnreachable())) {
                scheduleRemove(session);
            }
            IoFilterChain filterChain3 = session.getFilterChain();
            filterChain3.fireExceptionCaught(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyIdleSessions(long currentTime) throws Exception {
        if (currentTime - this.lastIdleCheckTime >= 1000) {
            this.lastIdleCheckTime = currentTime;
            AbstractIoSession.notifyIdleness(allSessions(), currentTime);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public void flush(long currentTime) {
        if (!this.flushingSessions.isEmpty()) {
            do {
                S session = this.flushingSessions.poll();
                if (session != null) {
                    session.unscheduledForFlush();
                    SessionState state = getState(session);
                    switch (state) {
                        case OPENED:
                            try {
                                boolean flushedAll = flushNow(session, currentTime);
                                if (flushedAll && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
                                    scheduleFlush(session);
                                    break;
                                }
                            } catch (Exception e) {
                                scheduleRemove(session);
                                session.closeNow();
                                IoFilterChain filterChain = session.getFilterChain();
                                filterChain.fireExceptionCaught(e);
                                break;
                            }
                            break;
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
                WriteRequest req2 = session.getCurrentWriteRequest();
                if (req2 == null) {
                    req2 = writeRequestQueue.poll(session);
                    if (req2 == null) {
                        break;
                    }
                    session.setCurrentWriteRequest(req2);
                }
                Object message = req2.getMessage();
                if (message instanceof IoBuffer) {
                    localWrittenBytes = writeBuffer(session, req2, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((IoBuffer) message).hasRemaining()) {
                        int i = writtenBytes + localWrittenBytes;
                        setInterestedInWrite(session, true);
                        return false;
                    }
                } else if (message instanceof FileRegion) {
                    localWrittenBytes = writeFile(session, req2, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
                    if (localWrittenBytes > 0 && ((FileRegion) message).getRemainingBytes() > 0) {
                        int i2 = writtenBytes + localWrittenBytes;
                        setInterestedInWrite(session, true);
                        return false;
                    }
                } else {
                    throw new IllegalStateException("Don't know how to handle message of type '" + message.getClass().getName() + "'.  Are you missing a protocol encoder?");
                }
                if (localWrittenBytes == 0) {
                    if (!req2.equals(AbstractIoSession.MESSAGE_SENT_REQUEST)) {
                        setInterestedInWrite(session, true);
                        return false;
                    }
                } else {
                    writtenBytes += localWrittenBytes;
                    if (writtenBytes >= maxWrittenBytes) {
                        scheduleFlush(session);
                        return false;
                    }
                }
                if (message instanceof IoBuffer) {
                    ((IoBuffer) message).free();
                    continue;
                }
            } while (writtenBytes < maxWrittenBytes);
            return true;
        } catch (Exception e) {
            if (0 != 0) {
                req.getFuture().setException(e);
            }
            IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
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
            Object originalMessage = req.getOriginalRequest().getMessage();
            if (originalMessage instanceof IoBuffer) {
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
                length = (int) Math.min(region.getRemainingBytes(), maxLength);
            } else {
                length = (int) Math.min(2147483647L, region.getRemainingBytes());
            }
            localWrittenBytes = transferFile(session, region, length);
            region.update(localWrittenBytes);
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
        session.setCurrentWriteRequest(null);
        IoFilterChain filterChain = session.getFilterChain();
        filterChain.fireMessageSent(req);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTrafficMask() {
        for (int queueSize = this.trafficControllingSessions.size(); queueSize > 0; queueSize--) {
            S session = this.trafficControllingSessions.poll();
            if (session != null) {
                SessionState state = getState(session);
                switch (state) {
                    case OPENED:
                        updateTrafficControl((AbstractPollingIoProcessor<S>) session);
                        break;
                    case CLOSING:
                        break;
                    case OPENING:
                        this.trafficControllingSessions.add(session);
                        break;
                    default:
                        throw new IllegalStateException(String.valueOf(state));
                }
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
            IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireExceptionCaught(e);
        }
        try {
            if (session.getWriteRequestQueue().isEmpty(session) || session.isWriteSuspended()) {
                z = false;
            }
            setInterestedInWrite(session, z);
        } catch (Exception e2) {
            IoFilterChain filterChain2 = session.getFilterChain();
            filterChain2.fireExceptionCaught(e2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Processor implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled;

        static {
            $assertionsDisabled = !AbstractPollingIoProcessor.class.desiredAssertionStatus();
        }

        private Processor() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() != this) {
                throw new AssertionError();
            }
            int nSessions = 0;
            AbstractPollingIoProcessor.this.lastIdleCheckTime = System.currentTimeMillis();
            int nbTries = 10;
            while (true) {
                try {
                    long t0 = System.currentTimeMillis();
                    int selected = AbstractPollingIoProcessor.this.select(1000L);
                    long t1 = System.currentTimeMillis();
                    long delta = t1 - t0;
                    if (!AbstractPollingIoProcessor.this.wakeupCalled.getAndSet(false) && selected == 0 && delta < 100) {
                        if (AbstractPollingIoProcessor.this.isBrokenConnection()) {
                            AbstractPollingIoProcessor.LOG.warn("Broken connection");
                        } else if (nbTries == 0) {
                            AbstractPollingIoProcessor.LOG.warn("Create a new selector. Selected is 0, delta = " + delta);
                            AbstractPollingIoProcessor.this.registerNewSelector();
                            nbTries = 10;
                        } else {
                            nbTries--;
                        }
                    } else {
                        nbTries = 10;
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
                        AbstractPollingIoProcessor.this.processorRef.set(null);
                        if (AbstractPollingIoProcessor.this.newSessions.isEmpty() && AbstractPollingIoProcessor.this.isSelectorEmpty()) {
                            if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                                throw new AssertionError();
                            }
                        } else if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                            throw new AssertionError();
                        } else {
                            if (!AbstractPollingIoProcessor.this.processorRef.compareAndSet(null, this)) {
                                if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() == this) {
                                    throw new AssertionError();
                                }
                            } else if (!$assertionsDisabled && AbstractPollingIoProcessor.this.processorRef.get() != this) {
                                throw new AssertionError();
                            }
                        }
                    }
                    if (AbstractPollingIoProcessor.this.isDisposing()) {
                        boolean hasKeys = false;
                        Iterator<S> i = AbstractPollingIoProcessor.this.allSessions();
                        while (i.hasNext()) {
                            IoSession session = i.next();
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
                        Thread.sleep(1000L);
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
            } catch (Exception e2) {
                ExceptionMonitor.getInstance().exceptionCaught(e2);
            } finally {
                AbstractPollingIoProcessor.this.disposalFuture.setValue(Boolean.valueOf(true));
            }
        }
    }
}
