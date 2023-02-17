package org.apache.mina.core.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.conn.ssl.TokenParser;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.file.FilenameFileRegion;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultCloseFuture;
import org.apache.mina.core.future.DefaultReadFuture;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteTimeoutException;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.util.ExceptionMonitor;

/* loaded from: classes.dex */
public abstract class AbstractIoSession implements IoSession {
    private IoSessionAttributeMap attributes;
    private volatile boolean closing;
    protected IoSessionConfig config;
    private final long creationTime;
    private WriteRequest currentWriteRequest;
    private final IoHandler handler;
    private long lastIdleTimeForBoth;
    private long lastIdleTimeForRead;
    private long lastIdleTimeForWrite;
    private long lastReadBytes;
    private long lastReadMessages;
    private long lastReadTime;
    private long lastThroughputCalculationTime;
    private long lastWriteTime;
    private long lastWrittenBytes;
    private long lastWrittenMessages;
    private long readBytes;
    private double readBytesThroughput;
    private long readMessages;
    private double readMessagesThroughput;
    private final IoService service;
    private long sessionId;
    private WriteRequestQueue writeRequestQueue;
    private long writtenBytes;
    private double writtenBytesThroughput;
    private long writtenMessages;
    private double writtenMessagesThroughput;
    private static final AttributeKey READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "readyReadFutures");
    private static final AttributeKey WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "waitingReadFutures");
    private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER = new IoFutureListener<CloseFuture>() { // from class: org.apache.mina.core.session.AbstractIoSession.1
        @Override // org.apache.mina.core.future.IoFutureListener
        public void operationComplete(CloseFuture future) {
            AbstractIoSession session = (AbstractIoSession) future.getSession();
            session.scheduledWriteBytes.set(0);
            session.scheduledWriteMessages.set(0);
            session.readBytesThroughput = 0.0d;
            session.readMessagesThroughput = 0.0d;
            session.writtenBytesThroughput = 0.0d;
            session.writtenMessagesThroughput = 0.0d;
        }
    };
    public static final WriteRequest CLOSE_REQUEST = new DefaultWriteRequest(new Object());
    public static final WriteRequest MESSAGE_SENT_REQUEST = new DefaultWriteRequest(DefaultWriteRequest.EMPTY_MESSAGE);
    private static AtomicLong idGenerator = new AtomicLong(0);
    private final Object lock = new Object();
    private final CloseFuture closeFuture = new DefaultCloseFuture(this);
    private boolean readSuspended = false;
    private boolean writeSuspended = false;
    private final AtomicBoolean scheduledForFlush = new AtomicBoolean();
    private final AtomicInteger scheduledWriteBytes = new AtomicInteger();
    private final AtomicInteger scheduledWriteMessages = new AtomicInteger();
    private AtomicInteger idleCountForBoth = new AtomicInteger();
    private AtomicInteger idleCountForRead = new AtomicInteger();
    private AtomicInteger idleCountForWrite = new AtomicInteger();
    private boolean deferDecreaseReadBuffer = true;

    public abstract IoProcessor getProcessor();

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractIoSession(IoService service) {
        this.service = service;
        this.handler = service.getHandler();
        long currentTime = System.currentTimeMillis();
        this.creationTime = currentTime;
        this.lastThroughputCalculationTime = currentTime;
        this.lastReadTime = currentTime;
        this.lastWriteTime = currentTime;
        this.lastIdleTimeForBoth = currentTime;
        this.lastIdleTimeForRead = currentTime;
        this.lastIdleTimeForWrite = currentTime;
        this.closeFuture.addListener((IoFutureListener<?>) SCHEDULED_COUNTER_RESETTER);
        this.sessionId = idGenerator.incrementAndGet();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getId() {
        return this.sessionId;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isConnected() {
        return !this.closeFuture.isClosed();
    }

    @Override // org.apache.mina.core.session.IoSession
    public boolean isActive() {
        return true;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isClosing() {
        return this.closing || this.closeFuture.isClosed();
    }

    @Override // org.apache.mina.core.session.IoSession
    public boolean isSecured() {
        return false;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final CloseFuture getCloseFuture() {
        return this.closeFuture;
    }

    public final boolean isScheduledForFlush() {
        return this.scheduledForFlush.get();
    }

    public final void scheduledForFlush() {
        this.scheduledForFlush.set(true);
    }

    public final void unscheduledForFlush() {
        this.scheduledForFlush.set(false);
    }

    public final boolean setScheduledForFlush(boolean schedule) {
        if (schedule) {
            return this.scheduledForFlush.compareAndSet(false, schedule);
        }
        this.scheduledForFlush.set(schedule);
        return true;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final CloseFuture close(boolean rightNow) {
        return rightNow ? closeNow() : closeOnFlush();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final CloseFuture close() {
        return closeNow();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final CloseFuture closeOnFlush() {
        if (!isClosing()) {
            getWriteRequestQueue().offer(this, CLOSE_REQUEST);
            getProcessor().flush(this);
        }
        return this.closeFuture;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final CloseFuture closeNow() {
        synchronized (this.lock) {
            if (isClosing()) {
                return this.closeFuture;
            }
            this.closing = true;
            try {
                destroy();
            } catch (Exception e) {
                IoFilterChain filterChain = getFilterChain();
                filterChain.fireExceptionCaught(e);
            }
            getFilterChain().fireFilterClose();
            return this.closeFuture;
        }
    }

    protected void destroy() {
        if (this.writeRequestQueue != null) {
            while (!this.writeRequestQueue.isEmpty(this)) {
                WriteRequest writeRequest = this.writeRequestQueue.poll(this);
                WriteFuture writeFuture = writeRequest.getFuture();
                if (writeFuture != null) {
                    writeFuture.setWritten();
                }
            }
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public IoHandler getHandler() {
        return this.handler;
    }

    @Override // org.apache.mina.core.session.IoSession
    public IoSessionConfig getConfig() {
        return this.config;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final ReadFuture read() {
        ReadFuture future;
        if (!getConfig().isUseReadOperation()) {
            throw new IllegalStateException("useReadOperation is not enabled.");
        }
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        synchronized (readyReadFutures) {
            future = readyReadFutures.poll();
            if (future != null) {
                if (future.isClosed()) {
                    readyReadFutures.offer(future);
                }
            } else {
                future = new DefaultReadFuture(this);
                getWaitingReadFutures().offer(future);
            }
        }
        return future;
    }

    public final void offerReadFuture(Object message) {
        newReadFuture().setRead(message);
    }

    public final void offerFailedReadFuture(Throwable exception) {
        newReadFuture().setException(exception);
    }

    public final void offerClosedReadFuture() {
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        synchronized (readyReadFutures) {
            newReadFuture().setClosed();
        }
    }

    private ReadFuture newReadFuture() {
        ReadFuture future;
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        Queue<ReadFuture> waitingReadFutures = getWaitingReadFutures();
        synchronized (readyReadFutures) {
            future = waitingReadFutures.poll();
            if (future == null) {
                future = new DefaultReadFuture(this);
                readyReadFutures.offer(future);
            }
        }
        return future;
    }

    private Queue<ReadFuture> getReadyReadFutures() {
        Queue<ReadFuture> readyReadFutures = (Queue) getAttribute(READY_READ_FUTURES_KEY);
        if (readyReadFutures == null) {
            Queue<ReadFuture> readyReadFutures2 = new ConcurrentLinkedQueue<>();
            Queue<ReadFuture> oldReadyReadFutures = (Queue) setAttributeIfAbsent(READY_READ_FUTURES_KEY, readyReadFutures2);
            if (oldReadyReadFutures != null) {
                return oldReadyReadFutures;
            }
            return readyReadFutures2;
        }
        return readyReadFutures;
    }

    private Queue<ReadFuture> getWaitingReadFutures() {
        Queue<ReadFuture> waitingReadyReadFutures = (Queue) getAttribute(WAITING_READ_FUTURES_KEY);
        if (waitingReadyReadFutures == null) {
            Queue<ReadFuture> waitingReadyReadFutures2 = new ConcurrentLinkedQueue<>();
            Queue<ReadFuture> oldWaitingReadyReadFutures = (Queue) setAttributeIfAbsent(WAITING_READ_FUTURES_KEY, waitingReadyReadFutures2);
            if (oldWaitingReadyReadFutures != null) {
                return oldWaitingReadyReadFutures;
            }
            return waitingReadyReadFutures2;
        }
        return waitingReadyReadFutures;
    }

    @Override // org.apache.mina.core.session.IoSession
    public WriteFuture write(Object message) {
        return write(message, null);
    }

    @Override // org.apache.mina.core.session.IoSession
    public WriteFuture write(Object message, SocketAddress remoteAddress) {
        FileChannel openedFileChannel;
        if (message == null) {
            throw new IllegalArgumentException("Trying to write a null message : not allowed");
        }
        if (!getTransportMetadata().isConnectionless() && remoteAddress != null) {
            throw new UnsupportedOperationException();
        }
        if (isClosing() || !isConnected()) {
            WriteFuture future = new DefaultWriteFuture(this);
            WriteRequest request = new DefaultWriteRequest(message, future, remoteAddress);
            WriteException writeException = new WriteToClosedSessionException(request);
            future.setException(writeException);
            return future;
        }
        try {
            if ((message instanceof IoBuffer) && !((IoBuffer) message).hasRemaining()) {
                throw new IllegalArgumentException("message is empty. Forgot to call flip()?");
            }
            if (message instanceof FileChannel) {
                FileChannel fileChannel = (FileChannel) message;
                message = new DefaultFileRegion(fileChannel, 0L, fileChannel.size());
                openedFileChannel = null;
            } else if (!(message instanceof File)) {
                openedFileChannel = null;
            } else {
                File file = (File) message;
                openedFileChannel = new FileInputStream(file).getChannel();
                try {
                    message = new FilenameFileRegion(file, openedFileChannel, 0L, openedFileChannel.size());
                } catch (IOException e) {
                    e = e;
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                    return DefaultWriteFuture.newNotWrittenFuture(this, e);
                }
            }
            WriteFuture writeFuture = new DefaultWriteFuture(this);
            WriteRequest writeRequest = new DefaultWriteRequest(message, writeFuture, remoteAddress);
            IoFilterChain filterChain = getFilterChain();
            filterChain.fireFilterWrite(writeRequest);
            if (openedFileChannel != null) {
                final FileChannel finalChannel = openedFileChannel;
                writeFuture.addListener(new IoFutureListener<WriteFuture>() { // from class: org.apache.mina.core.session.AbstractIoSession.2
                    @Override // org.apache.mina.core.future.IoFutureListener
                    public void operationComplete(WriteFuture future2) {
                        try {
                            finalChannel.close();
                        } catch (IOException e2) {
                            ExceptionMonitor.getInstance().exceptionCaught(e2);
                        }
                    }
                });
                return writeFuture;
            }
            return writeFuture;
        } catch (IOException e2) {
            e = e2;
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object getAttachment() {
        return getAttribute("");
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object setAttachment(Object attachment) {
        return setAttribute("", attachment);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object getAttribute(Object key) {
        return getAttribute(key, null);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object getAttribute(Object key, Object defaultValue) {
        return this.attributes.getAttribute(this, key, defaultValue);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object setAttribute(Object key, Object value) {
        return this.attributes.setAttribute(this, key, value);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object setAttribute(Object key) {
        return setAttribute(key, Boolean.TRUE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object setAttributeIfAbsent(Object key, Object value) {
        return this.attributes.setAttributeIfAbsent(this, key, value);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object setAttributeIfAbsent(Object key) {
        return setAttributeIfAbsent(key, Boolean.TRUE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object removeAttribute(Object key) {
        return this.attributes.removeAttribute(this, key);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean removeAttribute(Object key, Object value) {
        return this.attributes.removeAttribute(this, key, value);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
        return this.attributes.replaceAttribute(this, key, oldValue, newValue);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean containsAttribute(Object key) {
        return this.attributes.containsAttribute(this, key);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Set<Object> getAttributeKeys() {
        return this.attributes.getAttributeKeys(this);
    }

    public final IoSessionAttributeMap getAttributeMap() {
        return this.attributes;
    }

    public final void setAttributeMap(IoSessionAttributeMap attributes) {
        this.attributes = attributes;
    }

    public final void setWriteRequestQueue(WriteRequestQueue writeRequestQueue) {
        this.writeRequestQueue = writeRequestQueue;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void suspendRead() {
        this.readSuspended = true;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void suspendWrite() {
        this.writeSuspended = true;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void resumeRead() {
        this.readSuspended = false;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void resumeWrite() {
        this.writeSuspended = false;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public boolean isReadSuspended() {
        return this.readSuspended;
    }

    @Override // org.apache.mina.core.session.IoSession
    public boolean isWriteSuspended() {
        return this.writeSuspended;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getReadBytes() {
        return this.readBytes;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getWrittenBytes() {
        return this.writtenBytes;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getReadMessages() {
        return this.readMessages;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getWrittenMessages() {
        return this.writtenMessages;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final double getReadBytesThroughput() {
        return this.readBytesThroughput;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final double getWrittenBytesThroughput() {
        return this.writtenBytesThroughput;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final double getReadMessagesThroughput() {
        return this.readMessagesThroughput;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final double getWrittenMessagesThroughput() {
        return this.writtenMessagesThroughput;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void updateThroughput(long currentTime, boolean force) {
        int interval = (int) (currentTime - this.lastThroughputCalculationTime);
        long minInterval = getConfig().getThroughputCalculationIntervalInMillis();
        if ((minInterval != 0 && interval >= minInterval) || force) {
            this.readBytesThroughput = ((this.readBytes - this.lastReadBytes) * 1000.0d) / interval;
            this.writtenBytesThroughput = ((this.writtenBytes - this.lastWrittenBytes) * 1000.0d) / interval;
            this.readMessagesThroughput = ((this.readMessages - this.lastReadMessages) * 1000.0d) / interval;
            this.writtenMessagesThroughput = ((this.writtenMessages - this.lastWrittenMessages) * 1000.0d) / interval;
            this.lastReadBytes = this.readBytes;
            this.lastWrittenBytes = this.writtenBytes;
            this.lastReadMessages = this.readMessages;
            this.lastWrittenMessages = this.writtenMessages;
            this.lastThroughputCalculationTime = currentTime;
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getScheduledWriteBytes() {
        return this.scheduledWriteBytes.get();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final int getScheduledWriteMessages() {
        return this.scheduledWriteMessages.get();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setScheduledWriteBytes(int byteCount) {
        this.scheduledWriteBytes.set(byteCount);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setScheduledWriteMessages(int messages) {
        this.scheduledWriteMessages.set(messages);
    }

    public final void increaseReadBytes(long increment, long currentTime) {
        if (increment > 0) {
            this.readBytes += increment;
            this.lastReadTime = currentTime;
            this.idleCountForBoth.set(0);
            this.idleCountForRead.set(0);
            if (getService() instanceof AbstractIoService) {
                ((AbstractIoService) getService()).getStatistics().increaseReadBytes(increment, currentTime);
            }
        }
    }

    public final void increaseReadMessages(long currentTime) {
        this.readMessages++;
        this.lastReadTime = currentTime;
        this.idleCountForBoth.set(0);
        this.idleCountForRead.set(0);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseReadMessages(currentTime);
        }
    }

    public final void increaseWrittenBytes(int increment, long currentTime) {
        if (increment > 0) {
            this.writtenBytes += increment;
            this.lastWriteTime = currentTime;
            this.idleCountForBoth.set(0);
            this.idleCountForWrite.set(0);
            if (getService() instanceof AbstractIoService) {
                ((AbstractIoService) getService()).getStatistics().increaseWrittenBytes(increment, currentTime);
            }
            increaseScheduledWriteBytes(-increment);
        }
    }

    public final void increaseWrittenMessages(WriteRequest request, long currentTime) {
        Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            IoBuffer b = (IoBuffer) message;
            if (b.hasRemaining()) {
                return;
            }
        }
        this.writtenMessages++;
        this.lastWriteTime = currentTime;
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseWrittenMessages(currentTime);
        }
        decreaseScheduledWriteMessages();
    }

    public final void increaseScheduledWriteBytes(int increment) {
        this.scheduledWriteBytes.addAndGet(increment);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseScheduledWriteBytes(increment);
        }
    }

    public final void increaseScheduledWriteMessages() {
        this.scheduledWriteMessages.incrementAndGet();
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseScheduledWriteMessages();
        }
    }

    private void decreaseScheduledWriteMessages() {
        this.scheduledWriteMessages.decrementAndGet();
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().decreaseScheduledWriteMessages();
        }
    }

    public final void decreaseScheduledBytesAndMessages(WriteRequest request) {
        Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            IoBuffer b = (IoBuffer) message;
            if (b.hasRemaining()) {
                increaseScheduledWriteBytes(-((IoBuffer) message).remaining());
                return;
            } else {
                decreaseScheduledWriteMessages();
                return;
            }
        }
        decreaseScheduledWriteMessages();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final WriteRequestQueue getWriteRequestQueue() {
        if (this.writeRequestQueue == null) {
            throw new IllegalStateException();
        }
        return this.writeRequestQueue;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final WriteRequest getCurrentWriteRequest() {
        return this.currentWriteRequest;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final Object getCurrentWriteMessage() {
        WriteRequest req = getCurrentWriteRequest();
        if (req == null) {
            return null;
        }
        return req.getMessage();
    }

    @Override // org.apache.mina.core.session.IoSession
    public final void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
        this.currentWriteRequest = currentWriteRequest;
    }

    public final void increaseReadBufferSize() {
        int newReadBufferSize = getConfig().getReadBufferSize() << 1;
        if (newReadBufferSize <= getConfig().getMaxReadBufferSize()) {
            getConfig().setReadBufferSize(newReadBufferSize);
        } else {
            getConfig().setReadBufferSize(getConfig().getMaxReadBufferSize());
        }
        this.deferDecreaseReadBuffer = true;
    }

    public final void decreaseReadBufferSize() {
        if (this.deferDecreaseReadBuffer) {
            this.deferDecreaseReadBuffer = false;
            return;
        }
        if (getConfig().getReadBufferSize() > getConfig().getMinReadBufferSize()) {
            getConfig().setReadBufferSize(getConfig().getReadBufferSize() >>> 1);
        }
        this.deferDecreaseReadBuffer = true;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getCreationTime() {
        return this.creationTime;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastIoTime() {
        return Math.max(this.lastReadTime, this.lastWriteTime);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastReadTime() {
        return this.lastReadTime;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastWriteTime() {
        return this.lastWriteTime;
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isIdle(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleCountForBoth.get() > 0;
        } else if (status == IdleStatus.READER_IDLE) {
            return this.idleCountForRead.get() > 0;
        } else if (status == IdleStatus.WRITER_IDLE) {
            return this.idleCountForWrite.get() > 0;
        } else {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isBothIdle() {
        return isIdle(IdleStatus.BOTH_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isReaderIdle() {
        return isIdle(IdleStatus.READER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final boolean isWriterIdle() {
        return isIdle(IdleStatus.WRITER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final int getIdleCount(IdleStatus status) {
        if (getConfig().getIdleTime(status) == 0) {
            if (status == IdleStatus.BOTH_IDLE) {
                this.idleCountForBoth.set(0);
            }
            if (status == IdleStatus.READER_IDLE) {
                this.idleCountForRead.set(0);
            }
            if (status == IdleStatus.WRITER_IDLE) {
                this.idleCountForWrite.set(0);
            }
        }
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleCountForBoth.get();
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.idleCountForRead.get();
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.idleCountForWrite.get();
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastIdleTime(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.lastIdleTimeForBoth;
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.lastIdleTimeForRead;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.lastIdleTimeForWrite;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    public final void increaseIdleCount(IdleStatus status, long currentTime) {
        if (status == IdleStatus.BOTH_IDLE) {
            this.idleCountForBoth.incrementAndGet();
            this.lastIdleTimeForBoth = currentTime;
        } else if (status == IdleStatus.READER_IDLE) {
            this.idleCountForRead.incrementAndGet();
            this.lastIdleTimeForRead = currentTime;
        } else if (status == IdleStatus.WRITER_IDLE) {
            this.idleCountForWrite.incrementAndGet();
            this.lastIdleTimeForWrite = currentTime;
        } else {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        }
    }

    @Override // org.apache.mina.core.session.IoSession
    public final int getBothIdleCount() {
        return getIdleCount(IdleStatus.BOTH_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastBothIdleTime() {
        return getLastIdleTime(IdleStatus.BOTH_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastReaderIdleTime() {
        return getLastIdleTime(IdleStatus.READER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final long getLastWriterIdleTime() {
        return getLastIdleTime(IdleStatus.WRITER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final int getReaderIdleCount() {
        return getIdleCount(IdleStatus.READER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public final int getWriterIdleCount() {
        return getIdleCount(IdleStatus.WRITER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSession
    public SocketAddress getServiceAddress() {
        IoService service = getService();
        return service instanceof IoAcceptor ? ((IoAcceptor) service).getLocalAddress() : getRemoteAddress();
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public final boolean equals(Object o) {
        return super.equals(o);
    }

    public String toString() {
        String remote;
        if (isConnected() || isClosing()) {
            String local = null;
            try {
                remote = String.valueOf(getRemoteAddress());
            } catch (Exception e) {
                remote = "Cannot get the remote address informations: " + e.getMessage();
            }
            try {
                local = String.valueOf(getLocalAddress());
            } catch (Exception e2) {
            }
            if (getService() instanceof IoAcceptor) {
                return "(" + getIdAsString() + ": " + getServiceName() + ", server, " + remote + " => " + local + ')';
            }
            return "(" + getIdAsString() + ": " + getServiceName() + ", client, " + local + " => " + remote + ')';
        }
        return "(" + getIdAsString() + ") Session disconnected ...";
    }

    private String getIdAsString() {
        String id = Long.toHexString(getId()).toUpperCase();
        return id.length() <= 8 ? "0x00000000".substring(0, 10 - id.length()) + id : "0x" + id;
    }

    private String getServiceName() {
        TransportMetadata tm = getTransportMetadata();
        return tm == null ? "null" : tm.getProviderName() + TokenParser.SP + tm.getName();
    }

    @Override // org.apache.mina.core.session.IoSession
    public IoService getService() {
        return this.service;
    }

    public static void notifyIdleness(Iterator<? extends IoSession> sessions, long currentTime) {
        while (sessions.hasNext()) {
            IoSession session = sessions.next();
            if (!session.getCloseFuture().isClosed()) {
                notifyIdleSession(session, currentTime);
            }
        }
    }

    public static void notifyIdleSession(IoSession session, long currentTime) {
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.BOTH_IDLE), IdleStatus.BOTH_IDLE, Math.max(session.getLastIoTime(), session.getLastIdleTime(IdleStatus.BOTH_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.READER_IDLE), IdleStatus.READER_IDLE, Math.max(session.getLastReadTime(), session.getLastIdleTime(IdleStatus.READER_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.WRITER_IDLE), IdleStatus.WRITER_IDLE, Math.max(session.getLastWriteTime(), session.getLastIdleTime(IdleStatus.WRITER_IDLE)));
        notifyWriteTimeout(session, currentTime);
    }

    private static void notifyIdleSession0(IoSession session, long currentTime, long idleTime, IdleStatus status, long lastIoTime) {
        if (idleTime > 0 && lastIoTime != 0 && currentTime - lastIoTime >= idleTime) {
            session.getFilterChain().fireSessionIdle(status);
        }
    }

    private static void notifyWriteTimeout(IoSession session, long currentTime) {
        WriteRequest request;
        long writeTimeout = session.getConfig().getWriteTimeoutInMillis();
        if (writeTimeout > 0 && currentTime - session.getLastWriteTime() >= writeTimeout && !session.getWriteRequestQueue().isEmpty(session) && (request = session.getCurrentWriteRequest()) != null) {
            session.setCurrentWriteRequest(null);
            WriteTimeoutException cause = new WriteTimeoutException(request);
            request.getFuture().setException(cause);
            session.getFilterChain().fireExceptionCaught(cause);
            session.closeNow();
        }
    }
}