package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionRecycler;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.transport.socket.DatagramAcceptor;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;
import org.apache.mina.util.ExceptionMonitor;

public final class NioDatagramAcceptor extends AbstractIoAcceptor implements DatagramAcceptor, IoProcessor<NioSession> {
    private static final IoSessionRecycler DEFAULT_RECYCLER = new ExpiringSessionRecycler();
    private static final long SELECT_TIMEOUT = 1000;
    /* access modifiers changed from: private */
    public Acceptor acceptor;
    private final Map<SocketAddress, DatagramChannel> boundHandles;
    /* access modifiers changed from: private */
    public final Queue<AbstractIoAcceptor.AcceptorOperationFuture> cancelQueue;
    /* access modifiers changed from: private */
    public final AbstractIoService.ServiceOperationFuture disposalFuture;
    private final Queue<NioSession> flushingSessions;
    /* access modifiers changed from: private */
    public long lastIdleCheckTime;
    /* access modifiers changed from: private */
    public final Semaphore lock;
    /* access modifiers changed from: private */
    public final Queue<AbstractIoAcceptor.AcceptorOperationFuture> registerQueue;
    /* access modifiers changed from: private */
    public volatile boolean selectable;
    private volatile Selector selector;
    private IoSessionRecycler sessionRecycler;

    public NioDatagramAcceptor() {
        this(new DefaultDatagramSessionConfig(), (Executor) null);
    }

    public NioDatagramAcceptor(Executor executor) {
        this(new DefaultDatagramSessionConfig(), executor);
    }

    private NioDatagramAcceptor(IoSessionConfig sessionConfig, Executor executor) {
        super(sessionConfig, executor);
        this.lock = new Semaphore(1);
        this.registerQueue = new ConcurrentLinkedQueue();
        this.cancelQueue = new ConcurrentLinkedQueue();
        this.flushingSessions = new ConcurrentLinkedQueue();
        this.boundHandles = Collections.synchronizedMap(new HashMap());
        this.sessionRecycler = DEFAULT_RECYCLER;
        this.disposalFuture = new AbstractIoService.ServiceOperationFuture();
        try {
            init();
            this.selectable = true;
            if (!this.selectable) {
                try {
                    destroy();
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
            }
        } catch (RuntimeException e2) {
            throw e2;
        } catch (Exception e3) {
            throw new RuntimeIoException("Failed to initialize.", e3);
        } catch (Throwable th) {
            if (!this.selectable) {
                try {
                    destroy();
                } catch (Exception e4) {
                    ExceptionMonitor.getInstance().exceptionCaught(e4);
                }
            }
            throw th;
        }
    }

    private class Acceptor implements Runnable {
        private Acceptor() {
        }

        public void run() {
            int nHandles = 0;
            long unused = NioDatagramAcceptor.this.lastIdleCheckTime = System.currentTimeMillis();
            NioDatagramAcceptor.this.lock.release();
            while (NioDatagramAcceptor.this.selectable) {
                try {
                    int selected = NioDatagramAcceptor.this.select(1000);
                    nHandles += NioDatagramAcceptor.this.registerHandles();
                    if (nHandles == 0) {
                        NioDatagramAcceptor.this.lock.acquire();
                        if (NioDatagramAcceptor.this.registerQueue.isEmpty() && NioDatagramAcceptor.this.cancelQueue.isEmpty()) {
                            Acceptor unused2 = NioDatagramAcceptor.this.acceptor = null;
                            NioDatagramAcceptor.this.lock.release();
                            break;
                        }
                        NioDatagramAcceptor.this.lock.release();
                    }
                    if (selected > 0) {
                        NioDatagramAcceptor.this.processReadySessions(NioDatagramAcceptor.this.selectedHandles());
                    }
                    long currentTime = System.currentTimeMillis();
                    NioDatagramAcceptor.this.flushSessions(currentTime);
                    nHandles -= NioDatagramAcceptor.this.unregisterHandles();
                    NioDatagramAcceptor.this.notifyIdleSessions(currentTime);
                } catch (ClosedSelectorException cse) {
                    ExceptionMonitor.getInstance().exceptionCaught(cse);
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                } catch (Throwable th) {
                    NioDatagramAcceptor.this.lock.release();
                    throw th;
                }
            }
            if (NioDatagramAcceptor.this.selectable && NioDatagramAcceptor.this.isDisposing()) {
                boolean unused3 = NioDatagramAcceptor.this.selectable = false;
                try {
                    NioDatagramAcceptor.this.destroy();
                } catch (Exception e3) {
                    ExceptionMonitor.getInstance().exceptionCaught(e3);
                } finally {
                    NioDatagramAcceptor.this.disposalFuture.setValue(true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public int registerHandles() {
        /*
            r9 = this;
        L_0x0000:
            java.util.Queue<org.apache.mina.core.service.AbstractIoAcceptor$AcceptorOperationFuture> r6 = r9.registerQueue
            java.lang.Object r4 = r6.poll()
            org.apache.mina.core.service.AbstractIoAcceptor$AcceptorOperationFuture r4 = (org.apache.mina.core.service.AbstractIoAcceptor.AcceptorOperationFuture) r4
            if (r4 != 0) goto L_0x000c
            r6 = 0
        L_0x000b:
            return r6
        L_0x000c:
            java.util.HashMap r3 = new java.util.HashMap
            r3.<init>()
            java.util.List r2 = r4.getLocalAddresses()
            java.util.Iterator r6 = r2.iterator()     // Catch:{ Exception -> 0x0031 }
        L_0x0019:
            boolean r7 = r6.hasNext()     // Catch:{ Exception -> 0x0031 }
            if (r7 == 0) goto L_0x005c
            java.lang.Object r5 = r6.next()     // Catch:{ Exception -> 0x0031 }
            java.net.SocketAddress r5 = (java.net.SocketAddress) r5     // Catch:{ Exception -> 0x0031 }
            java.nio.channels.DatagramChannel r1 = r9.open(r5)     // Catch:{ Exception -> 0x0031 }
            java.net.SocketAddress r7 = r9.localAddress(r1)     // Catch:{ Exception -> 0x0031 }
            r3.put(r7, r1)     // Catch:{ Exception -> 0x0031 }
            goto L_0x0019
        L_0x0031:
            r0 = move-exception
            r4.setException(r0)     // Catch:{ all -> 0x00a0 }
            java.lang.Exception r6 = r4.getException()
            if (r6 == 0) goto L_0x0000
            java.util.Collection r6 = r3.values()
            java.util.Iterator r6 = r6.iterator()
        L_0x0043:
            boolean r7 = r6.hasNext()
            if (r7 == 0) goto L_0x009b
            java.lang.Object r1 = r6.next()
            java.nio.channels.DatagramChannel r1 = (java.nio.channels.DatagramChannel) r1
            r9.close(r1)     // Catch:{ Exception -> 0x0053 }
            goto L_0x0043
        L_0x0053:
            r0 = move-exception
            org.apache.mina.util.ExceptionMonitor r7 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r7.exceptionCaught(r0)
            goto L_0x0043
        L_0x005c:
            java.util.Map<java.net.SocketAddress, java.nio.channels.DatagramChannel> r6 = r9.boundHandles     // Catch:{ Exception -> 0x0031 }
            r6.putAll(r3)     // Catch:{ Exception -> 0x0031 }
            org.apache.mina.core.service.IoServiceListenerSupport r6 = r9.getListeners()     // Catch:{ Exception -> 0x0031 }
            r6.fireServiceActivated()     // Catch:{ Exception -> 0x0031 }
            r4.setDone()     // Catch:{ Exception -> 0x0031 }
            int r6 = r3.size()     // Catch:{ Exception -> 0x0031 }
            java.lang.Exception r7 = r4.getException()
            if (r7 == 0) goto L_0x000b
            java.util.Collection r7 = r3.values()
            java.util.Iterator r7 = r7.iterator()
        L_0x007d:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x0096
            java.lang.Object r1 = r7.next()
            java.nio.channels.DatagramChannel r1 = (java.nio.channels.DatagramChannel) r1
            r9.close(r1)     // Catch:{ Exception -> 0x008d }
            goto L_0x007d
        L_0x008d:
            r0 = move-exception
            org.apache.mina.util.ExceptionMonitor r8 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r8.exceptionCaught(r0)
            goto L_0x007d
        L_0x0096:
            r9.wakeup()
            goto L_0x000b
        L_0x009b:
            r9.wakeup()
            goto L_0x0000
        L_0x00a0:
            r6 = move-exception
            java.lang.Exception r7 = r4.getException()
            if (r7 == 0) goto L_0x00cb
            java.util.Collection r7 = r3.values()
            java.util.Iterator r7 = r7.iterator()
        L_0x00af:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x00c8
            java.lang.Object r1 = r7.next()
            java.nio.channels.DatagramChannel r1 = (java.nio.channels.DatagramChannel) r1
            r9.close(r1)     // Catch:{ Exception -> 0x00bf }
            goto L_0x00af
        L_0x00bf:
            r0 = move-exception
            org.apache.mina.util.ExceptionMonitor r8 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r8.exceptionCaught(r0)
            goto L_0x00af
        L_0x00c8:
            r9.wakeup()
        L_0x00cb:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.transport.socket.nio.NioDatagramAcceptor.registerHandles():int");
    }

    /* access modifiers changed from: private */
    public void processReadySessions(Set<SelectionKey> handles) {
        Iterator<SelectionKey> iterator = handles.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            DatagramChannel handle = (DatagramChannel) key.channel();
            iterator.remove();
            try {
                if (key.isValid() && key.isReadable()) {
                    readHandle(handle);
                }
                if (key.isValid() && key.isWritable()) {
                    Iterator<IoSession> it = getManagedSessions().values().iterator();
                    while (it.hasNext()) {
                        scheduleFlush((NioSession) it.next());
                    }
                }
            } catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
        }
    }

    private boolean scheduleFlush(NioSession session) {
        if (!session.setScheduledForFlush(true)) {
            return false;
        }
        this.flushingSessions.add(session);
        return true;
    }

    private void readHandle(DatagramChannel handle) throws Exception {
        IoBuffer readBuf = IoBuffer.allocate(getSessionConfig().getReadBufferSize());
        SocketAddress remoteAddress = receive(handle, readBuf);
        if (remoteAddress != null) {
            IoSession session = newSessionWithoutLock(remoteAddress, localAddress(handle));
            readBuf.flip();
            session.getFilterChain().fireMessageReceived(readBuf);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        initSession(r3, (org.apache.mina.core.future.IoFuture) null, (org.apache.mina.core.session.IoSessionInitializer) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        getFilterChainBuilder().buildFilterChain(r3.getFilterChain());
        getListeners().fireSessionCreated(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
        org.apache.mina.util.ExceptionMonitor.getInstance().exceptionCaught(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.apache.mina.core.session.IoSession newSessionWithoutLock(java.net.SocketAddress r9, java.net.SocketAddress r10) throws java.lang.Exception {
        /*
            r8 = this;
            r7 = 0
            java.util.Map<java.net.SocketAddress, java.nio.channels.DatagramChannel> r5 = r8.boundHandles
            java.lang.Object r1 = r5.get(r10)
            java.nio.channels.DatagramChannel r1 = (java.nio.channels.DatagramChannel) r1
            if (r1 != 0) goto L_0x0024
            java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Unknown local address: "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.StringBuilder r6 = r6.append(r10)
            java.lang.String r6 = r6.toString()
            r5.<init>(r6)
            throw r5
        L_0x0024:
            org.apache.mina.core.session.IoSessionRecycler r6 = r8.sessionRecycler
            monitor-enter(r6)
            org.apache.mina.core.session.IoSessionRecycler r5 = r8.sessionRecycler     // Catch:{ all -> 0x0056 }
            org.apache.mina.core.session.IoSession r3 = r5.recycle(r9)     // Catch:{ all -> 0x0056 }
            if (r3 == 0) goto L_0x0032
            monitor-exit(r6)     // Catch:{ all -> 0x0056 }
            r4 = r3
        L_0x0031:
            return r4
        L_0x0032:
            org.apache.mina.transport.socket.nio.NioSession r2 = r8.newSession(r8, r1, r9)     // Catch:{ all -> 0x0056 }
            org.apache.mina.core.session.IoSessionRecycler r5 = r8.getSessionRecycler()     // Catch:{ all -> 0x0056 }
            r5.put(r2)     // Catch:{ all -> 0x0056 }
            r3 = r2
            monitor-exit(r6)     // Catch:{ all -> 0x0056 }
            r8.initSession(r3, r7, r7)
            org.apache.mina.core.filterchain.IoFilterChainBuilder r5 = r8.getFilterChainBuilder()     // Catch:{ Exception -> 0x0059 }
            org.apache.mina.core.filterchain.IoFilterChain r6 = r3.getFilterChain()     // Catch:{ Exception -> 0x0059 }
            r5.buildFilterChain(r6)     // Catch:{ Exception -> 0x0059 }
            org.apache.mina.core.service.IoServiceListenerSupport r5 = r8.getListeners()     // Catch:{ Exception -> 0x0059 }
            r5.fireSessionCreated(r3)     // Catch:{ Exception -> 0x0059 }
        L_0x0054:
            r4 = r3
            goto L_0x0031
        L_0x0056:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0056 }
            throw r5
        L_0x0059:
            r0 = move-exception
            org.apache.mina.util.ExceptionMonitor r5 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r5.exceptionCaught(r0)
            goto L_0x0054
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.transport.socket.nio.NioDatagramAcceptor.newSessionWithoutLock(java.net.SocketAddress, java.net.SocketAddress):org.apache.mina.core.session.IoSession");
    }

    /* access modifiers changed from: private */
    public void flushSessions(long currentTime) {
        while (true) {
            NioSession session = this.flushingSessions.poll();
            if (session != null) {
                session.unscheduledForFlush();
                try {
                    if (flush(session, currentTime) && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
                        scheduleFlush(session);
                    }
                } catch (Exception e) {
                    session.getFilterChain().fireExceptionCaught(e);
                }
            } else {
                return;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean flush(NioSession session, long currentTime) throws Exception {
        WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        int maxWrittenBytes = session.getConfig().getMaxReadBufferSize() + (session.getConfig().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        while (true) {
            try {
                WriteRequest req = session.getCurrentWriteRequest();
                if (req == null) {
                    req = writeRequestQueue.poll(session);
                    if (req == null) {
                        setInterestedInWrite(session, false);
                        session.increaseWrittenBytes(writtenBytes, currentTime);
                        return true;
                    }
                    session.setCurrentWriteRequest(req);
                }
                IoBuffer buf = (IoBuffer) req.getMessage();
                if (buf.remaining() == 0) {
                    session.setCurrentWriteRequest((WriteRequest) null);
                    buf.reset();
                    session.getFilterChain().fireMessageSent(req);
                } else {
                    SocketAddress destination = req.getDestination();
                    if (destination == null) {
                        destination = session.getRemoteAddress();
                    }
                    int localWrittenBytes = send(session, buf, destination);
                    if (localWrittenBytes == 0 || writtenBytes >= maxWrittenBytes) {
                        setInterestedInWrite(session, true);
                    } else {
                        setInterestedInWrite(session, false);
                        session.setCurrentWriteRequest((WriteRequest) null);
                        writtenBytes += localWrittenBytes;
                        buf.reset();
                        session.getFilterChain().fireMessageSent(req);
                    }
                }
            } catch (Throwable th) {
                session.increaseWrittenBytes(writtenBytes, currentTime);
                throw th;
            }
        }
        setInterestedInWrite(session, true);
        session.increaseWrittenBytes(writtenBytes, currentTime);
        return false;
    }

    /* access modifiers changed from: private */
    public int unregisterHandles() {
        int nHandles = 0;
        while (true) {
            AbstractIoAcceptor.AcceptorOperationFuture request = this.cancelQueue.poll();
            if (request == null) {
                return nHandles;
            }
            for (SocketAddress socketAddress : request.getLocalAddresses()) {
                DatagramChannel handle = this.boundHandles.remove(socketAddress);
                if (handle != null) {
                    try {
                        close(handle);
                        wakeup();
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    } finally {
                        int nHandles2 = nHandles + 1;
                    }
                }
            }
            request.setDone();
        }
    }

    /* access modifiers changed from: private */
    public void notifyIdleSessions(long currentTime) {
        if (currentTime - this.lastIdleCheckTime >= 1000) {
            this.lastIdleCheckTime = currentTime;
            AbstractIoSession.notifyIdleness(getListeners().getManagedSessions().values().iterator(), currentTime);
        }
    }

    private void startupAcceptor() throws InterruptedException {
        if (!this.selectable) {
            this.registerQueue.clear();
            this.cancelQueue.clear();
            this.flushingSessions.clear();
        }
        this.lock.acquire();
        if (this.acceptor == null) {
            this.acceptor = new Acceptor();
            executeWorker(this.acceptor);
            return;
        }
        this.lock.release();
    }

    /* access modifiers changed from: protected */
    public void init() throws Exception {
        this.selector = Selector.open();
    }

    public void add(NioSession session) {
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture request = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.registerQueue.add(request);
        startupAcceptor();
        try {
            this.lock.acquire();
            Thread.sleep(10);
            wakeup();
            this.lock.release();
            request.awaitUninterruptibly();
            if (request.getException() != null) {
                throw request.getException();
            }
            Set<SocketAddress> newLocalAddresses = new HashSet<>();
            for (DatagramChannel handle : this.boundHandles.values()) {
                newLocalAddresses.add(localAddress(handle));
            }
            return newLocalAddresses;
        } catch (Throwable th) {
            this.lock.release();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void close(DatagramChannel handle) throws Exception {
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.disconnect();
        handle.close();
    }

    /* access modifiers changed from: protected */
    public void destroy() throws Exception {
        if (this.selector != null) {
            this.selector.close();
        }
    }

    /* access modifiers changed from: protected */
    public void dispose0() throws Exception {
        unbind();
        startupAcceptor();
        wakeup();
    }

    public void flush(NioSession session) {
        if (scheduleFlush(session)) {
            wakeup();
        }
    }

    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress) super.getDefaultLocalAddress();
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) super.getLocalAddress();
    }

    public DatagramSessionConfig getSessionConfig() {
        return (DatagramSessionConfig) this.sessionConfig;
    }

    public final IoSessionRecycler getSessionRecycler() {
        return this.sessionRecycler;
    }

    public TransportMetadata getTransportMetadata() {
        return NioDatagramSession.METADATA;
    }

    /* access modifiers changed from: protected */
    public boolean isReadable(DatagramChannel handle) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        return key.isReadable();
    }

    /* access modifiers changed from: protected */
    public boolean isWritable(DatagramChannel handle) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        return key.isWritable();
    }

    /* access modifiers changed from: protected */
    public SocketAddress localAddress(DatagramChannel handle) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) handle.socket().getLocalSocketAddress();
        InetAddress inetAddress = inetSocketAddress.getAddress();
        if (!(inetAddress instanceof Inet6Address) || !((Inet6Address) inetAddress).isIPv4CompatibleAddress()) {
            return inetSocketAddress;
        }
        byte[] ipV4Address = new byte[4];
        System.arraycopy(((Inet6Address) inetAddress).getAddress(), 12, ipV4Address, 0, 4);
        return new InetSocketAddress(Inet4Address.getByAddress(ipV4Address), inetSocketAddress.getPort());
    }

    /* access modifiers changed from: protected */
    public NioSession newSession(IoProcessor<NioSession> processor, DatagramChannel handle, SocketAddress remoteAddress) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return null;
        }
        NioDatagramSession newSession = new NioDatagramSession(this, handle, processor, remoteAddress);
        newSession.setSelectionKey(key);
        return newSession;
    }

    public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
        IoSession newSessionWithoutLock;
        if (isDisposing()) {
            throw new IllegalStateException("The Acceptor is being disposed.");
        } else if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress");
        } else {
            synchronized (this.bindLock) {
                if (!isActive()) {
                    throw new IllegalStateException("Can't create a session from a unbound service.");
                }
                try {
                    newSessionWithoutLock = newSessionWithoutLock(remoteAddress, localAddress);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Error e2) {
                    throw e2;
                } catch (Exception e3) {
                    throw new RuntimeIoException("Failed to create a session.", e3);
                }
            }
            return newSessionWithoutLock;
        }
    }

    /* access modifiers changed from: protected */
    public DatagramChannel open(SocketAddress localAddress) throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        try {
            new NioDatagramSessionConfig(ch).setAll(getSessionConfig());
            ch.configureBlocking(false);
            ch.socket().bind(localAddress);
            ch.register(this.selector, 1);
            if (1 == 0) {
                close(ch);
            }
            return ch;
        } catch (IOException ioe) {
            Exception e = new IOException("Error while binding on " + localAddress + StringUtils.f146LF + "original message : " + ioe.getMessage());
            e.initCause(ioe.getCause());
            ch.close();
            throw e;
        } catch (Throwable th) {
            if (0 == 0) {
                close(ch);
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public SocketAddress receive(DatagramChannel handle, IoBuffer buffer) throws Exception {
        return handle.receive(buffer.buf());
    }

    public void remove(NioSession session) {
        getSessionRecycler().remove(session);
        getListeners().fireSessionDestroyed(session);
    }

    /* access modifiers changed from: protected */
    public int select() throws Exception {
        return this.selector.select();
    }

    /* access modifiers changed from: protected */
    public int select(long timeout) throws Exception {
        return this.selector.select(timeout);
    }

    /* access modifiers changed from: protected */
    public Set<SelectionKey> selectedHandles() {
        return this.selector.selectedKeys();
    }

    /* access modifiers changed from: protected */
    public int send(NioSession session, IoBuffer buffer, SocketAddress remoteAddress) throws Exception {
        return ((DatagramChannel) session.getChannel()).send(buffer.buf(), remoteAddress);
    }

    public void setDefaultLocalAddress(InetSocketAddress localAddress) {
        setDefaultLocalAddress(localAddress);
    }

    /* access modifiers changed from: protected */
    public void setInterestedInWrite(NioSession session, boolean isInterested) throws Exception {
        int newInterestOps;
        SelectionKey key = session.getSelectionKey();
        if (key != null) {
            int newInterestOps2 = key.interestOps();
            if (isInterested) {
                newInterestOps = newInterestOps2 | 4;
            } else {
                newInterestOps = newInterestOps2 & -5;
            }
            key.interestOps(newInterestOps);
        }
    }

    public final void setSessionRecycler(IoSessionRecycler sessionRecycler2) {
        synchronized (this.bindLock) {
            if (isActive()) {
                throw new IllegalStateException("sessionRecycler can't be set while the acceptor is bound.");
            }
            if (sessionRecycler2 == null) {
                sessionRecycler2 = DEFAULT_RECYCLER;
            }
            this.sessionRecycler = sessionRecycler2;
        }
    }

    /* access modifiers changed from: protected */
    public final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture request = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.cancelQueue.add(request);
        startupAcceptor();
        wakeup();
        request.awaitUninterruptibly();
        if (request.getException() != null) {
            throw request.getException();
        }
    }

    public void updateTrafficControl(NioSession session) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void wakeup() {
        this.selector.wakeup();
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 134 */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3 = r16.getDestination();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006f, code lost:
        if (r3 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0071, code lost:
        r3 = r15.getRemoteAddress();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0075, code lost:
        r7 = send(r15, r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0079, code lost:
        if (r7 == 0) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x007b, code lost:
        if (0 < r8) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007d, code lost:
        setInterestedInWrite(r15, true);
        r15.getWriteRequestQueue().offer(r15, r16);
        scheduleFlush(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        setInterestedInWrite(r15, false);
        r15.setCurrentWriteRequest((org.apache.mina.core.write.WriteRequest) null);
        r10 = 0 + r7;
        r2.reset();
        r15.getFilterChain().fireMessageSent(r16);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(org.apache.mina.transport.socket.nio.NioSession r15, org.apache.mina.core.write.WriteRequest r16) {
        /*
            r14 = this;
            r13 = 0
            long r4 = java.lang.System.currentTimeMillis()
            org.apache.mina.core.write.WriteRequestQueue r9 = r15.getWriteRequestQueue()
            org.apache.mina.core.session.IoSessionConfig r11 = r15.getConfig()
            int r11 = r11.getMaxReadBufferSize()
            org.apache.mina.core.session.IoSessionConfig r12 = r15.getConfig()
            int r12 = r12.getMaxReadBufferSize()
            int r12 = r12 >>> 1
            int r8 = r11 + r12
            r10 = 0
            java.lang.Object r2 = r16.getMessage()
            org.apache.mina.core.buffer.IoBuffer r2 = (org.apache.mina.core.buffer.IoBuffer) r2
            int r11 = r2.remaining()
            if (r11 != 0) goto L_0x005b
            r15.setCurrentWriteRequest(r13)
            r2.reset()
            org.apache.mina.core.filterchain.IoFilterChain r11 = r15.getFilterChain()
            r0 = r16
            r11.fireMessageSent(r0)
        L_0x0039:
            return
        L_0x003a:
            r15.setCurrentWriteRequest(r16)     // Catch:{ Exception -> 0x008e }
        L_0x003d:
            java.lang.Object r11 = r16.getMessage()     // Catch:{ Exception -> 0x008e }
            r0 = r11
            org.apache.mina.core.buffer.IoBuffer r0 = (org.apache.mina.core.buffer.IoBuffer) r0     // Catch:{ Exception -> 0x008e }
            r2 = r0
            int r11 = r2.remaining()     // Catch:{ Exception -> 0x008e }
            if (r11 != 0) goto L_0x006b
            r11 = 0
            r15.setCurrentWriteRequest(r11)     // Catch:{ Exception -> 0x008e }
            r2.reset()     // Catch:{ Exception -> 0x008e }
            org.apache.mina.core.filterchain.IoFilterChain r11 = r15.getFilterChain()     // Catch:{ Exception -> 0x008e }
            r0 = r16
            r11.fireMessageSent(r0)     // Catch:{ Exception -> 0x008e }
        L_0x005b:
            if (r16 != 0) goto L_0x003d
            org.apache.mina.core.write.WriteRequest r16 = r9.poll(r15)     // Catch:{ Exception -> 0x008e }
            if (r16 != 0) goto L_0x003a
            r11 = 0
            r14.setInterestedInWrite(r15, r11)     // Catch:{ Exception -> 0x008e }
        L_0x0067:
            r15.increaseWrittenBytes(r10, r4)
            goto L_0x0039
        L_0x006b:
            java.net.SocketAddress r3 = r16.getDestination()     // Catch:{ Exception -> 0x008e }
            if (r3 != 0) goto L_0x0075
            java.net.SocketAddress r3 = r15.getRemoteAddress()     // Catch:{ Exception -> 0x008e }
        L_0x0075:
            int r7 = r14.send(r15, r2, r3)     // Catch:{ Exception -> 0x008e }
            if (r7 == 0) goto L_0x007d
            if (r10 < r8) goto L_0x009a
        L_0x007d:
            r11 = 1
            r14.setInterestedInWrite(r15, r11)     // Catch:{ Exception -> 0x008e }
            org.apache.mina.core.write.WriteRequestQueue r11 = r15.getWriteRequestQueue()     // Catch:{ Exception -> 0x008e }
            r0 = r16
            r11.offer(r15, r0)     // Catch:{ Exception -> 0x008e }
            r14.scheduleFlush(r15)     // Catch:{ Exception -> 0x008e }
            goto L_0x005b
        L_0x008e:
            r6 = move-exception
            org.apache.mina.core.filterchain.IoFilterChain r11 = r15.getFilterChain()     // Catch:{ all -> 0x00b0 }
            r11.fireExceptionCaught(r6)     // Catch:{ all -> 0x00b0 }
            r15.increaseWrittenBytes(r10, r4)
            goto L_0x0039
        L_0x009a:
            r11 = 0
            r14.setInterestedInWrite(r15, r11)     // Catch:{ Exception -> 0x008e }
            r11 = 0
            r15.setCurrentWriteRequest(r11)     // Catch:{ Exception -> 0x008e }
            int r10 = r10 + r7
            r2.reset()     // Catch:{ Exception -> 0x008e }
            org.apache.mina.core.filterchain.IoFilterChain r11 = r15.getFilterChain()     // Catch:{ Exception -> 0x008e }
            r0 = r16
            r11.fireMessageSent(r0)     // Catch:{ Exception -> 0x008e }
            goto L_0x0067
        L_0x00b0:
            r11 = move-exception
            r15.increaseWrittenBytes(r10, r4)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.transport.socket.nio.NioDatagramAcceptor.write(org.apache.mina.transport.socket.nio.NioSession, org.apache.mina.core.write.WriteRequest):void");
    }
}
