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

/* loaded from: classes.dex */
public final class NioDatagramAcceptor extends AbstractIoAcceptor implements DatagramAcceptor, IoProcessor<NioSession> {
    private static final IoSessionRecycler DEFAULT_RECYCLER = new ExpiringSessionRecycler();
    private static final long SELECT_TIMEOUT = 1000;
    private Acceptor acceptor;
    private final Map<SocketAddress, DatagramChannel> boundHandles;
    private final Queue<AbstractIoAcceptor.AcceptorOperationFuture> cancelQueue;
    private final AbstractIoService.ServiceOperationFuture disposalFuture;
    private final Queue<NioSession> flushingSessions;
    private long lastIdleCheckTime;
    private final Semaphore lock;
    private final Queue<AbstractIoAcceptor.AcceptorOperationFuture> registerQueue;
    private volatile boolean selectable;
    private volatile Selector selector;
    private IoSessionRecycler sessionRecycler;

    public NioDatagramAcceptor() {
        this(new DefaultDatagramSessionConfig(), null);
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
            }
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Acceptor implements Runnable {
        private Acceptor() {
        }

        @Override // java.lang.Runnable
        public void run() {
            int nHandles = 0;
            NioDatagramAcceptor.this.lastIdleCheckTime = System.currentTimeMillis();
            NioDatagramAcceptor.this.lock.release();
            while (NioDatagramAcceptor.this.selectable) {
                try {
                    int selected = NioDatagramAcceptor.this.select(1000L);
                    nHandles += NioDatagramAcceptor.this.registerHandles();
                    if (nHandles == 0) {
                        NioDatagramAcceptor.this.lock.acquire();
                        if (!NioDatagramAcceptor.this.registerQueue.isEmpty() || !NioDatagramAcceptor.this.cancelQueue.isEmpty()) {
                            NioDatagramAcceptor.this.lock.release();
                        } else {
                            NioDatagramAcceptor.this.acceptor = null;
                            NioDatagramAcceptor.this.lock.release();
                            break;
                        }
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
                        Thread.sleep(1000L);
                    } catch (InterruptedException e2) {
                    }
                }
            }
            if (NioDatagramAcceptor.this.selectable && NioDatagramAcceptor.this.isDisposing()) {
                NioDatagramAcceptor.this.selectable = false;
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

    /* JADX INFO: Access modifiers changed from: private */
    public int registerHandles() {
        while (true) {
            AbstractIoAcceptor.AcceptorOperationFuture req = this.registerQueue.poll();
            if (req != null) {
                Map<SocketAddress, DatagramChannel> newHandles = new HashMap<>();
                List<SocketAddress> localAddresses = req.getLocalAddresses();
                try {
                    for (SocketAddress socketAddress : localAddresses) {
                        DatagramChannel handle = open(socketAddress);
                        newHandles.put(localAddress(handle), handle);
                    }
                    this.boundHandles.putAll(newHandles);
                    getListeners().fireServiceActivated();
                    req.setDone();
                    int size = newHandles.size();
                } catch (Exception e) {
                    try {
                        req.setException(e);
                        if (req.getException() != null) {
                            for (DatagramChannel handle2 : newHandles.values()) {
                                try {
                                    close(handle2);
                                } catch (Exception e2) {
                                    ExceptionMonitor.getInstance().exceptionCaught(e2);
                                }
                            }
                            wakeup();
                        }
                    } finally {
                        if (req.getException() != null) {
                            for (DatagramChannel handle3 : newHandles.values()) {
                                try {
                                    close(handle3);
                                } catch (Exception e3) {
                                    ExceptionMonitor.getInstance().exceptionCaught(e3);
                                }
                            }
                            wakeup();
                        }
                    }
                }
            } else {
                return 0;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
                    for (IoSession session : getManagedSessions().values()) {
                        scheduleFlush((NioSession) session);
                    }
                }
            } catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
        }
    }

    private boolean scheduleFlush(NioSession session) {
        if (session.setScheduledForFlush(true)) {
            this.flushingSessions.add(session);
            return true;
        }
        return false;
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

    private IoSession newSessionWithoutLock(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        DatagramChannel handle = this.boundHandles.get(localAddress);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown local address: " + localAddress);
        }
        synchronized (this.sessionRecycler) {
            IoSession session = this.sessionRecycler.recycle(remoteAddress);
            if (session != null) {
                return session;
            }
            NioSession newSession = newSession(this, handle, remoteAddress);
            getSessionRecycler().put(newSession);
            initSession(newSession, null, null);
            try {
                getFilterChainBuilder().buildFilterChain(newSession.getFilterChain());
                getListeners().fireSessionCreated(newSession);
            } catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            }
            return newSession;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void flushSessions(long currentTime) {
        while (true) {
            NioSession session = this.flushingSessions.poll();
            if (session != null) {
                session.unscheduledForFlush();
                try {
                    boolean flushedAll = flush(session, currentTime);
                    if (flushedAll && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
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

    /* JADX WARN: Code restructure failed: missing block: B:24:0x0064, code lost:
        setInterestedInWrite(r13, true);
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:?, code lost:
        return false;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean flush(org.apache.mina.transport.socket.nio.NioSession r13, long r14) throws java.lang.Exception {
        /*
            r12 = this;
            r7 = 1
            r8 = 0
            org.apache.mina.core.write.WriteRequestQueue r5 = r13.getWriteRequestQueue()
            org.apache.mina.core.session.IoSessionConfig r9 = r13.getConfig()
            int r9 = r9.getMaxReadBufferSize()
            org.apache.mina.core.session.IoSessionConfig r10 = r13.getConfig()
            int r10 = r10.getMaxReadBufferSize()
            int r10 = r10 >>> 1
            int r3 = r9 + r10
            r6 = 0
        L1b:
            org.apache.mina.core.write.WriteRequest r4 = r13.getCurrentWriteRequest()     // Catch: java.lang.Throwable -> L4d
            if (r4 != 0) goto L32
            org.apache.mina.core.write.WriteRequest r4 = r5.poll(r13)     // Catch: java.lang.Throwable -> L4d
            if (r4 != 0) goto L2f
            r8 = 0
            r12.setInterestedInWrite(r13, r8)     // Catch: java.lang.Throwable -> L4d
            r13.increaseWrittenBytes(r6, r14)
        L2e:
            return r7
        L2f:
            r13.setCurrentWriteRequest(r4)     // Catch: java.lang.Throwable -> L4d
        L32:
            java.lang.Object r0 = r4.getMessage()     // Catch: java.lang.Throwable -> L4d
            org.apache.mina.core.buffer.IoBuffer r0 = (org.apache.mina.core.buffer.IoBuffer) r0     // Catch: java.lang.Throwable -> L4d
            int r9 = r0.remaining()     // Catch: java.lang.Throwable -> L4d
            if (r9 != 0) goto L52
            r9 = 0
            r13.setCurrentWriteRequest(r9)     // Catch: java.lang.Throwable -> L4d
            r0.reset()     // Catch: java.lang.Throwable -> L4d
            org.apache.mina.core.filterchain.IoFilterChain r9 = r13.getFilterChain()     // Catch: java.lang.Throwable -> L4d
            r9.fireMessageSent(r4)     // Catch: java.lang.Throwable -> L4d
            goto L1b
        L4d:
            r7 = move-exception
            r13.increaseWrittenBytes(r6, r14)
            throw r7
        L52:
            java.net.SocketAddress r1 = r4.getDestination()     // Catch: java.lang.Throwable -> L4d
            if (r1 != 0) goto L5c
            java.net.SocketAddress r1 = r13.getRemoteAddress()     // Catch: java.lang.Throwable -> L4d
        L5c:
            int r2 = r12.send(r13, r0, r1)     // Catch: java.lang.Throwable -> L4d
            if (r2 == 0) goto L64
            if (r6 < r3) goto L6d
        L64:
            r7 = 1
            r12.setInterestedInWrite(r13, r7)     // Catch: java.lang.Throwable -> L4d
            r13.increaseWrittenBytes(r6, r14)
            r7 = r8
            goto L2e
        L6d:
            r9 = 0
            r12.setInterestedInWrite(r13, r9)     // Catch: java.lang.Throwable -> L4d
            r9 = 0
            r13.setCurrentWriteRequest(r9)     // Catch: java.lang.Throwable -> L4d
            int r6 = r6 + r2
            r0.reset()     // Catch: java.lang.Throwable -> L4d
            org.apache.mina.core.filterchain.IoFilterChain r9 = r13.getFilterChain()     // Catch: java.lang.Throwable -> L4d
            r9.fireMessageSent(r4)     // Catch: java.lang.Throwable -> L4d
            goto L1b
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.transport.socket.nio.NioDatagramAcceptor.flush(org.apache.mina.transport.socket.nio.NioSession, long):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int unregisterHandles() {
        int nHandles = 0;
        while (true) {
            AbstractIoAcceptor.AcceptorOperationFuture request = this.cancelQueue.poll();
            if (request != null) {
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
            } else {
                return nHandles;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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

    protected void init() throws Exception {
        this.selector = Selector.open();
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public void add(NioSession session) {
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor
    protected final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture request = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.registerQueue.add(request);
        startupAcceptor();
        try {
            this.lock.acquire();
            Thread.sleep(10L);
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

    protected void close(DatagramChannel handle) throws Exception {
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.disconnect();
        handle.close();
    }

    protected void destroy() throws Exception {
        if (this.selector != null) {
            this.selector.close();
        }
    }

    @Override // org.apache.mina.core.service.AbstractIoService
    protected void dispose0() throws Exception {
        unbind();
        startupAcceptor();
        wakeup();
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public void flush(NioSession session) {
        if (scheduleFlush(session)) {
            wakeup();
        }
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress) super.getDefaultLocalAddress();
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) super.getLocalAddress();
    }

    @Override // org.apache.mina.core.service.IoService
    public DatagramSessionConfig getSessionConfig() {
        return (DatagramSessionConfig) this.sessionConfig;
    }

    @Override // org.apache.mina.transport.socket.DatagramAcceptor
    public final IoSessionRecycler getSessionRecycler() {
        return this.sessionRecycler;
    }

    @Override // org.apache.mina.core.service.IoService
    public TransportMetadata getTransportMetadata() {
        return NioDatagramSession.METADATA;
    }

    protected boolean isReadable(DatagramChannel handle) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        return key.isReadable();
    }

    protected boolean isWritable(DatagramChannel handle) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        return key.isWritable();
    }

    protected SocketAddress localAddress(DatagramChannel handle) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) handle.socket().getLocalSocketAddress();
        InetAddress inetAddress = inetSocketAddress.getAddress();
        if ((inetAddress instanceof Inet6Address) && ((Inet6Address) inetAddress).isIPv4CompatibleAddress()) {
            byte[] ipV6Address = ((Inet6Address) inetAddress).getAddress();
            byte[] ipV4Address = new byte[4];
            System.arraycopy(ipV6Address, 12, ipV4Address, 0, 4);
            InetAddress inet4Adress = Inet4Address.getByAddress(ipV4Address);
            return new InetSocketAddress(inet4Adress, inetSocketAddress.getPort());
        }
        return inetSocketAddress;
    }

    protected NioSession newSession(IoProcessor<NioSession> processor, DatagramChannel handle, SocketAddress remoteAddress) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return null;
        }
        NioDatagramSession newSession = new NioDatagramSession(this, handle, processor, remoteAddress);
        newSession.setSelectionKey(key);
        return newSession;
    }

    @Override // org.apache.mina.core.service.IoAcceptor
    public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
        IoSession newSessionWithoutLock;
        if (isDisposing()) {
            throw new IllegalStateException("The Acceptor is being disposed.");
        }
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress");
        }
        synchronized (this.bindLock) {
            if (!isActive()) {
                throw new IllegalStateException("Can't create a session from a unbound service.");
            }
            try {
                try {
                    newSessionWithoutLock = newSessionWithoutLock(remoteAddress, localAddress);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e2) {
                    throw new RuntimeIoException("Failed to create a session.", e2);
                }
            } catch (Error e3) {
                throw e3;
            }
        }
        return newSessionWithoutLock;
    }

    protected DatagramChannel open(SocketAddress localAddress) throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        boolean success = false;
        try {
            new NioDatagramSessionConfig(ch).setAll(getSessionConfig());
            ch.configureBlocking(false);
            try {
                ch.socket().bind(localAddress);
                ch.register(this.selector, 1);
                success = true;
                return ch;
            } catch (IOException ioe) {
                String newMessage = "Error while binding on " + localAddress + StringUtils.LF + "original message : " + ioe.getMessage();
                Exception e = new IOException(newMessage);
                e.initCause(ioe.getCause());
                ch.close();
                throw e;
            }
        } finally {
            if (!success) {
                close(ch);
            }
        }
    }

    protected SocketAddress receive(DatagramChannel handle, IoBuffer buffer) throws Exception {
        return handle.receive(buffer.buf());
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public void remove(NioSession session) {
        getSessionRecycler().remove(session);
        getListeners().fireSessionDestroyed(session);
    }

    protected int select() throws Exception {
        return this.selector.select();
    }

    protected int select(long timeout) throws Exception {
        return this.selector.select(timeout);
    }

    protected Set<SelectionKey> selectedHandles() {
        return this.selector.selectedKeys();
    }

    protected int send(NioSession session, IoBuffer buffer, SocketAddress remoteAddress) throws Exception {
        return ((DatagramChannel) session.getChannel()).send(buffer.buf(), remoteAddress);
    }

    @Override // org.apache.mina.transport.socket.DatagramAcceptor
    public void setDefaultLocalAddress(InetSocketAddress localAddress) {
        setDefaultLocalAddress((SocketAddress) localAddress);
    }

    protected void setInterestedInWrite(NioSession session, boolean isInterested) throws Exception {
        int newInterestOps;
        SelectionKey key = session.getSelectionKey();
        if (key != null) {
            int newInterestOps2 = key.interestOps();
            if (isInterested) {
                newInterestOps = newInterestOps2 | 4;
            } else {
                newInterestOps = newInterestOps2 & (-5);
            }
            key.interestOps(newInterestOps);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramAcceptor
    public final void setSessionRecycler(IoSessionRecycler sessionRecycler) {
        synchronized (this.bindLock) {
            if (isActive()) {
                throw new IllegalStateException("sessionRecycler can't be set while the acceptor is bound.");
            }
            if (sessionRecycler == null) {
                sessionRecycler = DEFAULT_RECYCLER;
            }
            this.sessionRecycler = sessionRecycler;
        }
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor
    protected final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture request = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.cancelQueue.add(request);
        startupAcceptor();
        wakeup();
        request.awaitUninterruptibly();
        if (request.getException() != null) {
            throw request.getException();
        }
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public void updateTrafficControl(NioSession session) {
        throw new UnsupportedOperationException();
    }

    protected void wakeup() {
        this.selector.wakeup();
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public void write(NioSession session, WriteRequest writeRequest) {
        long currentTime = System.currentTimeMillis();
        WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
        int maxWrittenBytes = session.getConfig().getMaxReadBufferSize() + (session.getConfig().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        IoBuffer buf = (IoBuffer) writeRequest.getMessage();
        if (buf.remaining() == 0) {
            session.setCurrentWriteRequest(null);
            buf.reset();
            session.getFilterChain().fireMessageSent(writeRequest);
            return;
        }
        while (true) {
            if (writeRequest == null) {
                writeRequest = writeRequestQueue.poll(session);
                if (writeRequest == null) {
                    setInterestedInWrite(session, false);
                    break;
                }
                session.setCurrentWriteRequest(writeRequest);
            }
            IoBuffer buf2 = (IoBuffer) writeRequest.getMessage();
            if (buf2.remaining() == 0) {
                try {
                    session.setCurrentWriteRequest(null);
                    buf2.reset();
                    session.getFilterChain().fireMessageSent(writeRequest);
                } catch (Exception e) {
                    session.getFilterChain().fireExceptionCaught(e);
                    return;
                } finally {
                    session.increaseWrittenBytes(0, currentTime);
                }
            } else {
                SocketAddress destination = writeRequest.getDestination();
                if (destination == null) {
                    destination = session.getRemoteAddress();
                }
                int localWrittenBytes = send(session, buf2, destination);
                if (localWrittenBytes == 0 || 0 >= maxWrittenBytes) {
                    setInterestedInWrite(session, true);
                    session.getWriteRequestQueue().offer(session, writeRequest);
                    scheduleFlush(session);
                } else {
                    setInterestedInWrite(session, false);
                    session.setCurrentWriteRequest(null);
                    writtenBytes = 0 + localWrittenBytes;
                    buf2.reset();
                    session.getFilterChain().fireMessageSent(writeRequest);
                    break;
                }
            }
        }
    }
}