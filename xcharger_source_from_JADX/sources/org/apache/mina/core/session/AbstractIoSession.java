package org.apache.mina.core.session;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultCloseFuture;
import org.apache.mina.core.future.DefaultReadFuture;
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
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteTimeoutException;

public abstract class AbstractIoSession implements IoSession {
    public static final WriteRequest CLOSE_REQUEST = new DefaultWriteRequest(new Object());
    public static final WriteRequest MESSAGE_SENT_REQUEST = new DefaultWriteRequest(DefaultWriteRequest.EMPTY_MESSAGE);
    private static final AttributeKey READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "readyReadFutures");
    private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER = new IoFutureListener<CloseFuture>() {
        public void operationComplete(CloseFuture future) {
            AbstractIoSession session = (AbstractIoSession) future.getSession();
            session.scheduledWriteBytes.set(0);
            session.scheduledWriteMessages.set(0);
            double unused = session.readBytesThroughput = 0.0d;
            double unused2 = session.readMessagesThroughput = 0.0d;
            double unused3 = session.writtenBytesThroughput = 0.0d;
            double unused4 = session.writtenMessagesThroughput = 0.0d;
        }
    };
    private static final AttributeKey WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "waitingReadFutures");
    private static AtomicLong idGenerator = new AtomicLong(0);
    private IoSessionAttributeMap attributes;
    private final CloseFuture closeFuture = new DefaultCloseFuture(this);
    private volatile boolean closing;
    protected IoSessionConfig config;
    private final long creationTime;
    private WriteRequest currentWriteRequest;
    private boolean deferDecreaseReadBuffer = true;
    private final IoHandler handler;
    private AtomicInteger idleCountForBoth = new AtomicInteger();
    private AtomicInteger idleCountForRead = new AtomicInteger();
    private AtomicInteger idleCountForWrite = new AtomicInteger();
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
    private final Object lock = new Object();
    private long readBytes;
    /* access modifiers changed from: private */
    public double readBytesThroughput;
    private long readMessages;
    /* access modifiers changed from: private */
    public double readMessagesThroughput;
    private boolean readSuspended = false;
    private final AtomicBoolean scheduledForFlush = new AtomicBoolean();
    /* access modifiers changed from: private */
    public final AtomicInteger scheduledWriteBytes = new AtomicInteger();
    /* access modifiers changed from: private */
    public final AtomicInteger scheduledWriteMessages = new AtomicInteger();
    private final IoService service;
    private long sessionId;
    private WriteRequestQueue writeRequestQueue;
    private boolean writeSuspended = false;
    private long writtenBytes;
    /* access modifiers changed from: private */
    public double writtenBytesThroughput;
    private long writtenMessages;
    /* access modifiers changed from: private */
    public double writtenMessagesThroughput;

    public abstract IoProcessor getProcessor();

    protected AbstractIoSession(IoService service2) {
        this.service = service2;
        this.handler = service2.getHandler();
        long currentTime = System.currentTimeMillis();
        this.creationTime = currentTime;
        this.lastThroughputCalculationTime = currentTime;
        this.lastReadTime = currentTime;
        this.lastWriteTime = currentTime;
        this.lastIdleTimeForBoth = currentTime;
        this.lastIdleTimeForRead = currentTime;
        this.lastIdleTimeForWrite = currentTime;
        this.closeFuture.addListener(SCHEDULED_COUNTER_RESETTER);
        this.sessionId = idGenerator.incrementAndGet();
    }

    public final long getId() {
        return this.sessionId;
    }

    public final boolean isConnected() {
        return !this.closeFuture.isClosed();
    }

    public boolean isActive() {
        return true;
    }

    public final boolean isClosing() {
        return this.closing || this.closeFuture.isClosed();
    }

    public boolean isSecured() {
        return false;
    }

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

    public final CloseFuture close(boolean rightNow) {
        if (rightNow) {
            return closeNow();
        }
        return closeOnFlush();
    }

    public final CloseFuture close() {
        return closeNow();
    }

    public final CloseFuture closeOnFlush() {
        if (!isClosing()) {
            getWriteRequestQueue().offer(this, CLOSE_REQUEST);
            getProcessor().flush(this);
        }
        return this.closeFuture;
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final org.apache.mina.core.future.CloseFuture closeNow() {
        /*
            r4 = this;
            java.lang.Object r3 = r4.lock
            monitor-enter(r3)
            boolean r2 = r4.isClosing()     // Catch:{ all -> 0x0027 }
            if (r2 == 0) goto L_0x000d
            org.apache.mina.core.future.CloseFuture r2 = r4.closeFuture     // Catch:{ all -> 0x0027 }
            monitor-exit(r3)     // Catch:{ all -> 0x0027 }
        L_0x000c:
            return r2
        L_0x000d:
            r2 = 1
            r4.closing = r2     // Catch:{ all -> 0x0027 }
            r4.destroy()     // Catch:{ Exception -> 0x001e }
        L_0x0013:
            monitor-exit(r3)     // Catch:{ all -> 0x0027 }
            org.apache.mina.core.filterchain.IoFilterChain r2 = r4.getFilterChain()
            r2.fireFilterClose()
            org.apache.mina.core.future.CloseFuture r2 = r4.closeFuture
            goto L_0x000c
        L_0x001e:
            r0 = move-exception
            org.apache.mina.core.filterchain.IoFilterChain r1 = r4.getFilterChain()     // Catch:{ all -> 0x0027 }
            r1.fireExceptionCaught(r0)     // Catch:{ all -> 0x0027 }
            goto L_0x0013
        L_0x0027:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0027 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.session.AbstractIoSession.closeNow():org.apache.mina.core.future.CloseFuture");
    }

    /* access modifiers changed from: protected */
    public void destroy() {
        if (this.writeRequestQueue != null) {
            while (!this.writeRequestQueue.isEmpty(this)) {
                WriteFuture writeFuture = this.writeRequestQueue.poll(this).getFuture();
                if (writeFuture != null) {
                    writeFuture.setWritten();
                }
            }
        }
    }

    public IoHandler getHandler() {
        return this.handler;
    }

    public IoSessionConfig getConfig() {
        return this.config;
    }

    public final ReadFuture read() {
        ReadFuture future;
        if (!getConfig().isUseReadOperation()) {
            throw new IllegalStateException("useReadOperation is not enabled.");
        }
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        synchronized (readyReadFutures) {
            future = readyReadFutures.poll();
            if (future == null) {
                future = new DefaultReadFuture(this);
                getWaitingReadFutures().offer(future);
            } else if (future.isClosed()) {
                readyReadFutures.offer(future);
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
        synchronized (getReadyReadFutures()) {
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
        if (readyReadFutures != null) {
            return readyReadFutures;
        }
        Queue<ReadFuture> readyReadFutures2 = new ConcurrentLinkedQueue<>();
        Queue<ReadFuture> oldReadyReadFutures = (Queue) setAttributeIfAbsent(READY_READ_FUTURES_KEY, readyReadFutures2);
        if (oldReadyReadFutures != null) {
            return oldReadyReadFutures;
        }
        return readyReadFutures2;
    }

    private Queue<ReadFuture> getWaitingReadFutures() {
        Queue<ReadFuture> waitingReadyReadFutures = (Queue) getAttribute(WAITING_READ_FUTURES_KEY);
        if (waitingReadyReadFutures != null) {
            return waitingReadyReadFutures;
        }
        Queue<ReadFuture> waitingReadyReadFutures2 = new ConcurrentLinkedQueue<>();
        Queue<ReadFuture> oldWaitingReadyReadFutures = (Queue) setAttributeIfAbsent(WAITING_READ_FUTURES_KEY, waitingReadyReadFutures2);
        if (oldWaitingReadyReadFutures != null) {
            return oldWaitingReadyReadFutures;
        }
        return waitingReadyReadFutures2;
    }

    public WriteFuture write(Object message) {
        return write(message, (SocketAddress) null);
    }

    /* JADX WARNING: type inference failed for: r24v2 */
    /* JADX WARNING: type inference failed for: r24v3 */
    /* JADX WARNING: type inference failed for: r7v1, types: [org.apache.mina.core.file.FilenameFileRegion] */
    /* JADX WARNING: type inference failed for: r4v16, types: [org.apache.mina.core.file.DefaultFileRegion] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.apache.mina.core.future.WriteFuture write(java.lang.Object r24, java.net.SocketAddress r25) {
        /*
            r23 = this;
            if (r24 != 0) goto L_0x000a
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.String r6 = "Trying to write a null message : not allowed"
            r4.<init>(r6)
            throw r4
        L_0x000a:
            org.apache.mina.core.service.TransportMetadata r4 = r23.getTransportMetadata()
            boolean r4 = r4.isConnectionless()
            if (r4 != 0) goto L_0x001c
            if (r25 == 0) goto L_0x001c
            java.lang.UnsupportedOperationException r4 = new java.lang.UnsupportedOperationException
            r4.<init>()
            throw r4
        L_0x001c:
            boolean r4 = r23.isClosing()
            if (r4 != 0) goto L_0x0028
            boolean r4 = r23.isConnected()
            if (r4 != 0) goto L_0x0051
        L_0x0028:
            org.apache.mina.core.future.DefaultWriteFuture r17 = new org.apache.mina.core.future.DefaultWriteFuture
            r0 = r17
            r1 = r23
            r0.<init>(r1)
            org.apache.mina.core.write.DefaultWriteRequest r19 = new org.apache.mina.core.write.DefaultWriteRequest
            r0 = r19
            r1 = r24
            r2 = r17
            r3 = r25
            r0.<init>(r1, r2, r3)
            org.apache.mina.core.write.WriteToClosedSessionException r20 = new org.apache.mina.core.write.WriteToClosedSessionException
            r0 = r20
            r1 = r19
            r0.<init>((org.apache.mina.core.write.WriteRequest) r1)
            r0 = r17
            r1 = r20
            r0.setException(r1)
            r21 = r17
        L_0x0050:
            return r21
        L_0x0051:
            r18 = 0
            r0 = r24
            boolean r4 = r0 instanceof org.apache.mina.core.buffer.IoBuffer     // Catch:{ IOException -> 0x006c }
            if (r4 == 0) goto L_0x007d
            r0 = r24
            org.apache.mina.core.buffer.IoBuffer r0 = (org.apache.mina.core.buffer.IoBuffer) r0     // Catch:{ IOException -> 0x006c }
            r4 = r0
            boolean r4 = r4.hasRemaining()     // Catch:{ IOException -> 0x006c }
            if (r4 != 0) goto L_0x007d
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException     // Catch:{ IOException -> 0x006c }
            java.lang.String r6 = "message is empty. Forgot to call flip()?"
            r4.<init>(r6)     // Catch:{ IOException -> 0x006c }
            throw r4     // Catch:{ IOException -> 0x006c }
        L_0x006c:
            r14 = move-exception
            r9 = r18
        L_0x006f:
            org.apache.mina.util.ExceptionMonitor r4 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r4.exceptionCaught(r14)
            r0 = r23
            org.apache.mina.core.future.WriteFuture r21 = org.apache.mina.core.future.DefaultWriteFuture.newNotWrittenFuture(r0, r14)
            goto L_0x0050
        L_0x007d:
            r0 = r24
            boolean r4 = r0 instanceof java.nio.channels.FileChannel     // Catch:{ IOException -> 0x006c }
            if (r4 == 0) goto L_0x00c9
            r0 = r24
            java.nio.channels.FileChannel r0 = (java.nio.channels.FileChannel) r0     // Catch:{ IOException -> 0x006c }
            r5 = r0
            org.apache.mina.core.file.DefaultFileRegion r24 = new org.apache.mina.core.file.DefaultFileRegion     // Catch:{ IOException -> 0x006c }
            r6 = 0
            long r8 = r5.size()     // Catch:{ IOException -> 0x006c }
            r4 = r24
            r4.<init>(r5, r6, r8)     // Catch:{ IOException -> 0x006c }
            r9 = r18
        L_0x0097:
            org.apache.mina.core.future.DefaultWriteFuture r21 = new org.apache.mina.core.future.DefaultWriteFuture
            r0 = r21
            r1 = r23
            r0.<init>(r1)
            org.apache.mina.core.write.DefaultWriteRequest r22 = new org.apache.mina.core.write.DefaultWriteRequest
            r0 = r22
            r1 = r24
            r2 = r21
            r3 = r25
            r0.<init>(r1, r2, r3)
            org.apache.mina.core.filterchain.IoFilterChain r15 = r23.getFilterChain()
            r0 = r22
            r15.fireFilterWrite(r0)
            if (r9 == 0) goto L_0x0050
            r16 = r9
            org.apache.mina.core.session.AbstractIoSession$2 r4 = new org.apache.mina.core.session.AbstractIoSession$2
            r0 = r23
            r1 = r16
            r4.<init>(r1)
            r0 = r21
            r0.addListener(r4)
            goto L_0x0050
        L_0x00c9:
            r0 = r24
            boolean r4 = r0 instanceof java.io.File     // Catch:{ IOException -> 0x006c }
            if (r4 == 0) goto L_0x00ed
            r0 = r24
            java.io.File r0 = (java.io.File) r0     // Catch:{ IOException -> 0x006c }
            r8 = r0
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ IOException -> 0x006c }
            r4.<init>(r8)     // Catch:{ IOException -> 0x006c }
            java.nio.channels.FileChannel r9 = r4.getChannel()     // Catch:{ IOException -> 0x006c }
            org.apache.mina.core.file.FilenameFileRegion r24 = new org.apache.mina.core.file.FilenameFileRegion     // Catch:{ IOException -> 0x00eb }
            r10 = 0
            long r12 = r9.size()     // Catch:{ IOException -> 0x00eb }
            r7 = r24
            r7.<init>(r8, r9, r10, r12)     // Catch:{ IOException -> 0x00eb }
            goto L_0x0097
        L_0x00eb:
            r14 = move-exception
            goto L_0x006f
        L_0x00ed:
            r9 = r18
            goto L_0x0097
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.session.AbstractIoSession.write(java.lang.Object, java.net.SocketAddress):org.apache.mina.core.future.WriteFuture");
    }

    public final Object getAttachment() {
        return getAttribute("");
    }

    public final Object setAttachment(Object attachment) {
        return setAttribute("", attachment);
    }

    public final Object getAttribute(Object key) {
        return getAttribute(key, (Object) null);
    }

    public final Object getAttribute(Object key, Object defaultValue) {
        return this.attributes.getAttribute(this, key, defaultValue);
    }

    public final Object setAttribute(Object key, Object value) {
        return this.attributes.setAttribute(this, key, value);
    }

    public final Object setAttribute(Object key) {
        return setAttribute(key, Boolean.TRUE);
    }

    public final Object setAttributeIfAbsent(Object key, Object value) {
        return this.attributes.setAttributeIfAbsent(this, key, value);
    }

    public final Object setAttributeIfAbsent(Object key) {
        return setAttributeIfAbsent(key, Boolean.TRUE);
    }

    public final Object removeAttribute(Object key) {
        return this.attributes.removeAttribute(this, key);
    }

    public final boolean removeAttribute(Object key, Object value) {
        return this.attributes.removeAttribute(this, key, value);
    }

    public final boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
        return this.attributes.replaceAttribute(this, key, oldValue, newValue);
    }

    public final boolean containsAttribute(Object key) {
        return this.attributes.containsAttribute(this, key);
    }

    public final Set<Object> getAttributeKeys() {
        return this.attributes.getAttributeKeys(this);
    }

    public final IoSessionAttributeMap getAttributeMap() {
        return this.attributes;
    }

    public final void setAttributeMap(IoSessionAttributeMap attributes2) {
        this.attributes = attributes2;
    }

    public final void setWriteRequestQueue(WriteRequestQueue writeRequestQueue2) {
        this.writeRequestQueue = writeRequestQueue2;
    }

    public final void suspendRead() {
        this.readSuspended = true;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    public final void suspendWrite() {
        this.writeSuspended = true;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    public final void resumeRead() {
        this.readSuspended = false;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    public final void resumeWrite() {
        this.writeSuspended = false;
        if (!isClosing() && isConnected()) {
            getProcessor().updateTrafficControl(this);
        }
    }

    public boolean isReadSuspended() {
        return this.readSuspended;
    }

    public boolean isWriteSuspended() {
        return this.writeSuspended;
    }

    public final long getReadBytes() {
        return this.readBytes;
    }

    public final long getWrittenBytes() {
        return this.writtenBytes;
    }

    public final long getReadMessages() {
        return this.readMessages;
    }

    public final long getWrittenMessages() {
        return this.writtenMessages;
    }

    public final double getReadBytesThroughput() {
        return this.readBytesThroughput;
    }

    public final double getWrittenBytesThroughput() {
        return this.writtenBytesThroughput;
    }

    public final double getReadMessagesThroughput() {
        return this.readMessagesThroughput;
    }

    public final double getWrittenMessagesThroughput() {
        return this.writtenMessagesThroughput;
    }

    public final void updateThroughput(long currentTime, boolean force) {
        int interval = (int) (currentTime - this.lastThroughputCalculationTime);
        long minInterval = getConfig().getThroughputCalculationIntervalInMillis();
        if ((minInterval != 0 && ((long) interval) >= minInterval) || force) {
            this.readBytesThroughput = (((double) (this.readBytes - this.lastReadBytes)) * 1000.0d) / ((double) interval);
            this.writtenBytesThroughput = (((double) (this.writtenBytes - this.lastWrittenBytes)) * 1000.0d) / ((double) interval);
            this.readMessagesThroughput = (((double) (this.readMessages - this.lastReadMessages)) * 1000.0d) / ((double) interval);
            this.writtenMessagesThroughput = (((double) (this.writtenMessages - this.lastWrittenMessages)) * 1000.0d) / ((double) interval);
            this.lastReadBytes = this.readBytes;
            this.lastWrittenBytes = this.writtenBytes;
            this.lastReadMessages = this.readMessages;
            this.lastWrittenMessages = this.writtenMessages;
            this.lastThroughputCalculationTime = currentTime;
        }
    }

    public final long getScheduledWriteBytes() {
        return (long) this.scheduledWriteBytes.get();
    }

    public final int getScheduledWriteMessages() {
        return this.scheduledWriteMessages.get();
    }

    /* access modifiers changed from: protected */
    public void setScheduledWriteBytes(int byteCount) {
        this.scheduledWriteBytes.set(byteCount);
    }

    /* access modifiers changed from: protected */
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
            this.writtenBytes += (long) increment;
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
        if (!(message instanceof IoBuffer) || !((IoBuffer) message).hasRemaining()) {
            this.writtenMessages++;
            this.lastWriteTime = currentTime;
            if (getService() instanceof AbstractIoService) {
                ((AbstractIoService) getService()).getStatistics().increaseWrittenMessages(currentTime);
            }
            decreaseScheduledWriteMessages();
        }
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
        if (!(message instanceof IoBuffer)) {
            decreaseScheduledWriteMessages();
        } else if (((IoBuffer) message).hasRemaining()) {
            increaseScheduledWriteBytes(-((IoBuffer) message).remaining());
        } else {
            decreaseScheduledWriteMessages();
        }
    }

    public final WriteRequestQueue getWriteRequestQueue() {
        if (this.writeRequestQueue != null) {
            return this.writeRequestQueue;
        }
        throw new IllegalStateException();
    }

    public final WriteRequest getCurrentWriteRequest() {
        return this.currentWriteRequest;
    }

    public final Object getCurrentWriteMessage() {
        WriteRequest req = getCurrentWriteRequest();
        if (req == null) {
            return null;
        }
        return req.getMessage();
    }

    public final void setCurrentWriteRequest(WriteRequest currentWriteRequest2) {
        this.currentWriteRequest = currentWriteRequest2;
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

    public final long getCreationTime() {
        return this.creationTime;
    }

    public final long getLastIoTime() {
        return Math.max(this.lastReadTime, this.lastWriteTime);
    }

    public final long getLastReadTime() {
        return this.lastReadTime;
    }

    public final long getLastWriteTime() {
        return this.lastWriteTime;
    }

    public final boolean isIdle(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            if (this.idleCountForBoth.get() > 0) {
                return true;
            }
            return false;
        } else if (status == IdleStatus.READER_IDLE) {
            if (this.idleCountForRead.get() <= 0) {
                return false;
            }
            return true;
        } else if (status != IdleStatus.WRITER_IDLE) {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        } else if (this.idleCountForWrite.get() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public final boolean isBothIdle() {
        return isIdle(IdleStatus.BOTH_IDLE);
    }

    public final boolean isReaderIdle() {
        return isIdle(IdleStatus.READER_IDLE);
    }

    public final boolean isWriterIdle() {
        return isIdle(IdleStatus.WRITER_IDLE);
    }

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

    public final int getBothIdleCount() {
        return getIdleCount(IdleStatus.BOTH_IDLE);
    }

    public final long getLastBothIdleTime() {
        return getLastIdleTime(IdleStatus.BOTH_IDLE);
    }

    public final long getLastReaderIdleTime() {
        return getLastIdleTime(IdleStatus.READER_IDLE);
    }

    public final long getLastWriterIdleTime() {
        return getLastIdleTime(IdleStatus.WRITER_IDLE);
    }

    public final int getReaderIdleCount() {
        return getIdleCount(IdleStatus.READER_IDLE);
    }

    public final int getWriterIdleCount() {
        return getIdleCount(IdleStatus.WRITER_IDLE);
    }

    public SocketAddress getServiceAddress() {
        IoService service2 = getService();
        if (service2 instanceof IoAcceptor) {
            return ((IoAcceptor) service2).getLocalAddress();
        }
        return getRemoteAddress();
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public final boolean equals(Object o) {
        return super.equals(o);
    }

    public String toString() {
        String remote;
        if (!isConnected() && !isClosing()) {
            return "(" + getIdAsString() + ") Session disconnected ...";
        }
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

    private String getIdAsString() {
        String id = Long.toHexString(getId()).toUpperCase();
        if (id.length() <= 8) {
            return "0x00000000".substring(0, 10 - id.length()) + id;
        }
        return "0x" + id;
    }

    private String getServiceName() {
        TransportMetadata tm = getTransportMetadata();
        if (tm == null) {
            return "null";
        }
        return tm.getProviderName() + TokenParser.f168SP + tm.getName();
    }

    public IoService getService() {
        return this.service;
    }

    public static void notifyIdleness(Iterator<? extends IoSession> sessions, long currentTime) {
        while (sessions.hasNext()) {
            IoSession session = (IoSession) sessions.next();
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
            session.setCurrentWriteRequest((WriteRequest) null);
            WriteTimeoutException cause = new WriteTimeoutException(request);
            request.getFuture().setException(cause);
            session.getFilterChain().fireExceptionCaught(cause);
            session.closeNow();
        }
    }
}
