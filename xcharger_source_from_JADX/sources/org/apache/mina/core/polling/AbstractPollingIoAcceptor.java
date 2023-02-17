package org.apache.mina.core.polling;

import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.spi.SelectorProvider;
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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.util.ExceptionMonitor;

public abstract class AbstractPollingIoAcceptor<S extends AbstractIoSession, H> extends AbstractIoAcceptor {
    /* access modifiers changed from: private */
    public AtomicReference<AbstractPollingIoAcceptor<S, H>.Acceptor> acceptorRef;
    protected int backlog;
    private final Map<SocketAddress, H> boundHandles;
    /* access modifiers changed from: private */
    public final Queue<AbstractIoAcceptor.AcceptorOperationFuture> cancelQueue;
    /* access modifiers changed from: private */
    public final boolean createdProcessor;
    /* access modifiers changed from: private */
    public final AbstractIoService.ServiceOperationFuture disposalFuture;
    /* access modifiers changed from: private */
    public final Semaphore lock;
    /* access modifiers changed from: private */
    public final IoProcessor<S> processor;
    /* access modifiers changed from: private */
    public final Queue<AbstractIoAcceptor.AcceptorOperationFuture> registerQueue;
    protected boolean reuseAddress;
    /* access modifiers changed from: private */
    public volatile boolean selectable;

    /* access modifiers changed from: protected */
    public abstract S accept(IoProcessor<S> ioProcessor, H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void close(H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void destroy() throws Exception;

    /* access modifiers changed from: protected */
    public abstract void init() throws Exception;

    /* access modifiers changed from: protected */
    public abstract void init(SelectorProvider selectorProvider) throws Exception;

    /* access modifiers changed from: protected */
    public abstract SocketAddress localAddress(H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract H open(SocketAddress socketAddress) throws Exception;

    /* access modifiers changed from: protected */
    public abstract int select() throws Exception;

    /* access modifiers changed from: protected */
    public abstract Iterator<H> selectedHandles();

    /* access modifiers changed from: protected */
    public abstract void wakeup();

    protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass) {
        this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass), true, (SelectorProvider) null);
    }

    protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass, int processorCount) {
        this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass, processorCount), true, (SelectorProvider) null);
    }

    protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass, int processorCount, SelectorProvider selectorProvider) {
        this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass, processorCount, selectorProvider), true, selectorProvider);
    }

    protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, IoProcessor<S> processor2) {
        this(sessionConfig, (Executor) null, processor2, false, (SelectorProvider) null);
    }

    protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor2) {
        this(sessionConfig, executor, processor2, false, (SelectorProvider) null);
    }

    private AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor2, boolean createdProcessor2, SelectorProvider selectorProvider) {
        super(sessionConfig, executor);
        this.lock = new Semaphore(1);
        this.registerQueue = new ConcurrentLinkedQueue();
        this.cancelQueue = new ConcurrentLinkedQueue();
        this.boundHandles = Collections.synchronizedMap(new HashMap());
        this.disposalFuture = new AbstractIoService.ServiceOperationFuture();
        this.acceptorRef = new AtomicReference<>();
        this.reuseAddress = false;
        this.backlog = 50;
        if (processor2 == null) {
            throw new IllegalArgumentException("processor");
        }
        this.processor = processor2;
        this.createdProcessor = createdProcessor2;
        try {
            init(selectorProvider);
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

    /* access modifiers changed from: protected */
    public void dispose0() throws Exception {
        unbind();
        startupAcceptor();
        wakeup();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture request = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.registerQueue.add(request);
        startupAcceptor();
        try {
            this.lock.acquire();
            wakeup();
            this.lock.release();
            request.awaitUninterruptibly();
            if (request.getException() != null) {
                throw request.getException();
            }
            Set<SocketAddress> newLocalAddresses = new HashSet<>();
            for (H handle : this.boundHandles.values()) {
                newLocalAddresses.add(localAddress(handle));
            }
            return newLocalAddresses;
        } catch (Throwable th) {
            this.lock.release();
            throw th;
        }
    }

    private void startupAcceptor() throws InterruptedException {
        if (!this.selectable) {
            this.registerQueue.clear();
            this.cancelQueue.clear();
        }
        if (this.acceptorRef.get() == null) {
            this.lock.acquire();
            AbstractPollingIoAcceptor<S, H>.Acceptor acceptor = new Acceptor();
            if (this.acceptorRef.compareAndSet((Object) null, acceptor)) {
                executeWorker(acceptor);
            } else {
                this.lock.release();
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
        AbstractIoAcceptor.AcceptorOperationFuture future = new AbstractIoAcceptor.AcceptorOperationFuture(localAddresses);
        this.cancelQueue.add(future);
        startupAcceptor();
        wakeup();
        future.awaitUninterruptibly();
        if (future.getException() != null) {
            throw future.getException();
        }
    }

    private class Acceptor implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled = (!AbstractPollingIoAcceptor.class.desiredAssertionStatus());

        private Acceptor() {
        }

        public void run() {
            if ($assertionsDisabled || AbstractPollingIoAcceptor.this.acceptorRef.get() == this) {
                int nHandles = 0;
                AbstractPollingIoAcceptor.this.lock.release();
                while (true) {
                    if (!AbstractPollingIoAcceptor.this.selectable) {
                        break;
                    }
                    try {
                        nHandles += AbstractPollingIoAcceptor.this.registerHandles();
                        int selected = AbstractPollingIoAcceptor.this.select();
                        if (nHandles == 0) {
                            AbstractPollingIoAcceptor.this.acceptorRef.set((Object) null);
                            if (!AbstractPollingIoAcceptor.this.registerQueue.isEmpty() || !AbstractPollingIoAcceptor.this.cancelQueue.isEmpty()) {
                                if (!AbstractPollingIoAcceptor.this.acceptorRef.compareAndSet((Object) null, this)) {
                                    if (!$assertionsDisabled && AbstractPollingIoAcceptor.this.acceptorRef.get() == this) {
                                        throw new AssertionError();
                                    }
                                } else if (!$assertionsDisabled && AbstractPollingIoAcceptor.this.acceptorRef.get() != this) {
                                    throw new AssertionError();
                                }
                            } else if (!$assertionsDisabled && AbstractPollingIoAcceptor.this.acceptorRef.get() == this) {
                                throw new AssertionError();
                            }
                        }
                        if (selected > 0) {
                            processHandles(AbstractPollingIoAcceptor.this.selectedHandles());
                        }
                        nHandles -= AbstractPollingIoAcceptor.this.unregisterHandles();
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
                if (AbstractPollingIoAcceptor.this.selectable && AbstractPollingIoAcceptor.this.isDisposing()) {
                    boolean unused = AbstractPollingIoAcceptor.this.selectable = false;
                    try {
                        if (AbstractPollingIoAcceptor.this.createdProcessor) {
                            AbstractPollingIoAcceptor.this.processor.dispose();
                        }
                        try {
                            synchronized (AbstractPollingIoAcceptor.this.disposalLock) {
                                if (AbstractPollingIoAcceptor.this.isDisposing()) {
                                    AbstractPollingIoAcceptor.this.destroy();
                                }
                            }
                            AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                        } catch (Exception e2) {
                            try {
                                ExceptionMonitor.getInstance().exceptionCaught(e2);
                            } finally {
                                AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                            }
                        }
                    } catch (Throwable th) {
                        try {
                            synchronized (AbstractPollingIoAcceptor.this.disposalLock) {
                                if (AbstractPollingIoAcceptor.this.isDisposing()) {
                                    AbstractPollingIoAcceptor.this.destroy();
                                }
                                AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                                throw th;
                            }
                        } catch (Exception e3) {
                            try {
                                ExceptionMonitor.getInstance().exceptionCaught(e3);
                            } finally {
                                AbstractPollingIoAcceptor.this.disposalFuture.setDone();
                            }
                        }
                    }
                }
            } else {
                throw new AssertionError();
            }
        }

        private void processHandles(Iterator<H> handles) throws Exception {
            while (handles.hasNext()) {
                H handle = handles.next();
                handles.remove();
                S session = AbstractPollingIoAcceptor.this.accept(AbstractPollingIoAcceptor.this.processor, handle);
                if (session != null) {
                    AbstractPollingIoAcceptor.this.initSession(session, (IoFuture) null, (IoSessionInitializer) null);
                    session.getProcessor().add(session);
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
            java.lang.Object r2 = r6.poll()
            org.apache.mina.core.service.AbstractIoAcceptor$AcceptorOperationFuture r2 = (org.apache.mina.core.service.AbstractIoAcceptor.AcceptorOperationFuture) r2
            if (r2 != 0) goto L_0x000c
            r6 = 0
        L_0x000b:
            return r6
        L_0x000c:
            java.util.concurrent.ConcurrentHashMap r5 = new java.util.concurrent.ConcurrentHashMap
            r5.<init>()
            java.util.List r4 = r2.getLocalAddresses()
            java.util.Iterator r6 = r4.iterator()     // Catch:{ Exception -> 0x0031 }
        L_0x0019:
            boolean r7 = r6.hasNext()     // Catch:{ Exception -> 0x0031 }
            if (r7 == 0) goto L_0x005a
            java.lang.Object r0 = r6.next()     // Catch:{ Exception -> 0x0031 }
            java.net.SocketAddress r0 = (java.net.SocketAddress) r0     // Catch:{ Exception -> 0x0031 }
            java.lang.Object r3 = r9.open(r0)     // Catch:{ Exception -> 0x0031 }
            java.net.SocketAddress r7 = r9.localAddress(r3)     // Catch:{ Exception -> 0x0031 }
            r5.put(r7, r3)     // Catch:{ Exception -> 0x0031 }
            goto L_0x0019
        L_0x0031:
            r1 = move-exception
            r2.setException(r1)     // Catch:{ all -> 0x0095 }
            java.lang.Exception r6 = r2.getException()
            if (r6 == 0) goto L_0x0000
            java.util.Collection r6 = r5.values()
            java.util.Iterator r6 = r6.iterator()
        L_0x0043:
            boolean r7 = r6.hasNext()
            if (r7 == 0) goto L_0x0090
            java.lang.Object r3 = r6.next()
            r9.close(r3)     // Catch:{ Exception -> 0x0051 }
            goto L_0x0043
        L_0x0051:
            r1 = move-exception
            org.apache.mina.util.ExceptionMonitor r7 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r7.exceptionCaught(r1)
            goto L_0x0043
        L_0x005a:
            java.util.Map<java.net.SocketAddress, H> r6 = r9.boundHandles     // Catch:{ Exception -> 0x0031 }
            r6.putAll(r5)     // Catch:{ Exception -> 0x0031 }
            r2.setDone()     // Catch:{ Exception -> 0x0031 }
            int r6 = r5.size()     // Catch:{ Exception -> 0x0031 }
            java.lang.Exception r7 = r2.getException()
            if (r7 == 0) goto L_0x000b
            java.util.Collection r7 = r5.values()
            java.util.Iterator r7 = r7.iterator()
        L_0x0074:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x008b
            java.lang.Object r3 = r7.next()
            r9.close(r3)     // Catch:{ Exception -> 0x0082 }
            goto L_0x0074
        L_0x0082:
            r1 = move-exception
            org.apache.mina.util.ExceptionMonitor r8 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r8.exceptionCaught(r1)
            goto L_0x0074
        L_0x008b:
            r9.wakeup()
            goto L_0x000b
        L_0x0090:
            r9.wakeup()
            goto L_0x0000
        L_0x0095:
            r6 = move-exception
            java.lang.Exception r7 = r2.getException()
            if (r7 == 0) goto L_0x00be
            java.util.Collection r7 = r5.values()
            java.util.Iterator r7 = r7.iterator()
        L_0x00a4:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x00bb
            java.lang.Object r3 = r7.next()
            r9.close(r3)     // Catch:{ Exception -> 0x00b2 }
            goto L_0x00a4
        L_0x00b2:
            r1 = move-exception
            org.apache.mina.util.ExceptionMonitor r8 = org.apache.mina.util.ExceptionMonitor.getInstance()
            r8.exceptionCaught(r1)
            goto L_0x00a4
        L_0x00bb:
            r9.wakeup()
        L_0x00be:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.polling.AbstractPollingIoAcceptor.registerHandles():int");
    }

    /* access modifiers changed from: private */
    public int unregisterHandles() {
        int cancelledHandles = 0;
        while (true) {
            AbstractIoAcceptor.AcceptorOperationFuture future = this.cancelQueue.poll();
            if (future == null) {
                return cancelledHandles;
            }
            for (SocketAddress a : future.getLocalAddresses()) {
                H handle = this.boundHandles.remove(a);
                if (handle != null) {
                    try {
                        close(handle);
                        wakeup();
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    } finally {
                        int cancelledHandles2 = cancelledHandles + 1;
                    }
                }
            }
            future.setDone();
        }
    }

    public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    public int getBacklog() {
        return this.backlog;
    }

    public void setBacklog(int backlog2) {
        synchronized (this.bindLock) {
            if (isActive()) {
                throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
            }
            this.backlog = backlog2;
        }
    }

    public boolean isReuseAddress() {
        return this.reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress2) {
        synchronized (this.bindLock) {
            if (isActive()) {
                throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
            }
            this.reuseAddress = reuseAddress2;
        }
    }

    public SocketSessionConfig getSessionConfig() {
        return (SocketSessionConfig) this.sessionConfig;
    }
}
