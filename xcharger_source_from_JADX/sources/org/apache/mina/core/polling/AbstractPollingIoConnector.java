package org.apache.mina.core.polling;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultConnectFuture;
import org.apache.mina.core.service.AbstractIoConnector;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.util.ExceptionMonitor;

public abstract class AbstractPollingIoConnector<T extends AbstractIoSession, H> extends AbstractIoConnector {
    /* access modifiers changed from: private */
    public final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> cancelQueue;
    /* access modifiers changed from: private */
    public final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> connectQueue;
    /* access modifiers changed from: private */
    public final AtomicReference<AbstractPollingIoConnector<T, H>.Connector> connectorRef;
    /* access modifiers changed from: private */
    public final boolean createdProcessor;
    /* access modifiers changed from: private */
    public final AbstractIoService.ServiceOperationFuture disposalFuture;
    /* access modifiers changed from: private */
    public final IoProcessor<T> processor;
    /* access modifiers changed from: private */
    public volatile boolean selectable;

    /* access modifiers changed from: protected */
    public abstract Iterator<H> allHandles();

    /* access modifiers changed from: protected */
    public abstract void close(H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract boolean connect(H h, SocketAddress socketAddress) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void destroy() throws Exception;

    /* access modifiers changed from: protected */
    public abstract boolean finishConnect(H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract AbstractPollingIoConnector<T, H>.ConnectionRequest getConnectionRequest(H h);

    /* access modifiers changed from: protected */
    public abstract void init() throws Exception;

    /* access modifiers changed from: protected */
    public abstract H newHandle(SocketAddress socketAddress) throws Exception;

    /* access modifiers changed from: protected */
    public abstract T newSession(IoProcessor<T> ioProcessor, H h) throws Exception;

    /* access modifiers changed from: protected */
    public abstract void register(H h, AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest) throws Exception;

    /* access modifiers changed from: protected */
    public abstract int select(int i) throws Exception;

    /* access modifiers changed from: protected */
    public abstract Iterator<H> selectedHandles();

    /* access modifiers changed from: protected */
    public abstract void wakeup();

    protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, Class<? extends IoProcessor<T>> processorClass) {
        this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass), true);
    }

    protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, Class<? extends IoProcessor<T>> processorClass, int processorCount) {
        this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass, processorCount), true);
    }

    protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, IoProcessor<T> processor2) {
        this(sessionConfig, (Executor) null, processor2, false);
    }

    protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor2) {
        this(sessionConfig, executor, processor2, false);
    }

    private AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor2, boolean createdProcessor2) {
        super(sessionConfig, executor);
        this.connectQueue = new ConcurrentLinkedQueue();
        this.cancelQueue = new ConcurrentLinkedQueue();
        this.disposalFuture = new AbstractIoService.ServiceOperationFuture();
        this.connectorRef = new AtomicReference<>();
        if (processor2 == null) {
            throw new IllegalArgumentException("processor");
        }
        this.processor = processor2;
        this.createdProcessor = createdProcessor2;
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

    /* access modifiers changed from: protected */
    public final void dispose0() throws Exception {
        startupWorker();
        wakeup();
    }

    /* access modifiers changed from: protected */
    public final ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        try {
            H handle = newHandle(localAddress);
            if (connect(handle, remoteAddress)) {
                ConnectFuture future = new DefaultConnectFuture();
                T session = newSession(this.processor, handle);
                initSession(session, future, sessionInitializer);
                session.getProcessor().add(session);
                if (1 != 0 || handle == null) {
                    return future;
                }
                try {
                    close(handle);
                    return future;
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                    return future;
                }
            } else {
                if (1 == 0 && handle != null) {
                    try {
                        close(handle);
                    } catch (Exception e2) {
                        ExceptionMonitor.getInstance().exceptionCaught(e2);
                    }
                }
                ConnectFuture request = new ConnectionRequest(handle, sessionInitializer);
                this.connectQueue.add(request);
                startupWorker();
                wakeup();
                return request;
            }
        } catch (Exception e3) {
            ConnectFuture future2 = DefaultConnectFuture.newFailedFuture(e3);
            if (0 != 0 || 0 == 0) {
                return future2;
            }
            try {
                close((Object) null);
                return future2;
            } catch (Exception e4) {
                ExceptionMonitor.getInstance().exceptionCaught(e4);
                return future2;
            }
        } catch (Throwable th) {
            if (0 == 0 && 0 != 0) {
                try {
                    close((Object) null);
                } catch (Exception e5) {
                    ExceptionMonitor.getInstance().exceptionCaught(e5);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void startupWorker() {
        if (!this.selectable) {
            this.connectQueue.clear();
            this.cancelQueue.clear();
        }
        if (this.connectorRef.get() == null) {
            AbstractPollingIoConnector<T, H>.Connector connector = new Connector();
            if (this.connectorRef.compareAndSet((Object) null, connector)) {
                executeWorker(connector);
            }
        }
    }

    /* access modifiers changed from: private */
    public int registerNew() {
        int nHandles = 0;
        while (true) {
            AbstractPollingIoConnector<T, H>.ConnectionRequest req = this.connectQueue.poll();
            if (req == null) {
                return nHandles;
            }
            H handle = req.handle;
            try {
                register(handle, req);
                nHandles++;
            } catch (Exception e) {
                req.setException(e);
                try {
                    close(handle);
                } catch (Exception e2) {
                    ExceptionMonitor.getInstance().exceptionCaught(e2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public int cancelKeys() {
        int nHandles = 0;
        while (true) {
            AbstractPollingIoConnector<T, H>.ConnectionRequest req = this.cancelQueue.poll();
            if (req == null) {
                break;
            }
            try {
                close(req.handle);
            } catch (Exception e) {
                ExceptionMonitor.getInstance().exceptionCaught(e);
            } finally {
                int nHandles2 = nHandles + 1;
            }
        }
        if (nHandles > 0) {
            wakeup();
        }
        return nHandles;
    }

    /* access modifiers changed from: private */
    public int processConnections(Iterator<H> handlers) {
        int nHandles = 0;
        while (handlers.hasNext()) {
            H handle = handlers.next();
            handlers.remove();
            AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest = getConnectionRequest(handle);
            if (connectionRequest != null) {
                try {
                    if (finishConnect(handle)) {
                        T session = newSession(this.processor, handle);
                        initSession(session, connectionRequest, connectionRequest.getSessionInitializer());
                        session.getProcessor().add(session);
                        nHandles++;
                    }
                    if (1 == 0) {
                        this.cancelQueue.offer(connectionRequest);
                    }
                } catch (Exception e) {
                    connectionRequest.setException(e);
                    if (0 == 0) {
                        this.cancelQueue.offer(connectionRequest);
                    }
                } catch (Throwable th) {
                    if (0 == 0) {
                        this.cancelQueue.offer(connectionRequest);
                    }
                    throw th;
                }
            }
        }
        return nHandles;
    }

    /* access modifiers changed from: private */
    public void processTimedOutSessions(Iterator<H> handles) {
        long currentTime = System.currentTimeMillis();
        while (handles.hasNext()) {
            AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest = getConnectionRequest(handles.next());
            if (connectionRequest != null && currentTime >= connectionRequest.deadline) {
                connectionRequest.setException(new ConnectException("Connection timed out."));
                this.cancelQueue.offer(connectionRequest);
            }
        }
    }

    private class Connector implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled = (!AbstractPollingIoConnector.class.desiredAssertionStatus());

        private Connector() {
        }

        public void run() {
            if ($assertionsDisabled || AbstractPollingIoConnector.this.connectorRef.get() == this) {
                int nHandles = 0;
                while (true) {
                    if (!AbstractPollingIoConnector.this.selectable) {
                        break;
                    }
                    try {
                        int selected = AbstractPollingIoConnector.this.select((int) Math.min(AbstractPollingIoConnector.this.getConnectTimeoutMillis(), 1000));
                        nHandles += AbstractPollingIoConnector.this.registerNew();
                        if (nHandles == 0) {
                            AbstractPollingIoConnector.this.connectorRef.set((Object) null);
                            if (AbstractPollingIoConnector.this.connectQueue.isEmpty()) {
                                if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() == this) {
                                    throw new AssertionError();
                                }
                            } else if (!AbstractPollingIoConnector.this.connectorRef.compareAndSet((Object) null, this)) {
                                if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() == this) {
                                    throw new AssertionError();
                                }
                            } else if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() != this) {
                                throw new AssertionError();
                            }
                        }
                        if (selected > 0) {
                            nHandles -= AbstractPollingIoConnector.this.processConnections(AbstractPollingIoConnector.this.selectedHandles());
                        }
                        AbstractPollingIoConnector.this.processTimedOutSessions(AbstractPollingIoConnector.this.allHandles());
                        nHandles -= AbstractPollingIoConnector.this.cancelKeys();
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
                if (AbstractPollingIoConnector.this.selectable && AbstractPollingIoConnector.this.isDisposing()) {
                    boolean unused = AbstractPollingIoConnector.this.selectable = false;
                    try {
                        if (AbstractPollingIoConnector.this.createdProcessor) {
                            AbstractPollingIoConnector.this.processor.dispose();
                        }
                        try {
                            synchronized (AbstractPollingIoConnector.this.disposalLock) {
                                if (AbstractPollingIoConnector.this.isDisposing()) {
                                    AbstractPollingIoConnector.this.destroy();
                                }
                            }
                            AbstractPollingIoConnector.this.disposalFuture.setDone();
                        } catch (Exception e2) {
                            try {
                                ExceptionMonitor.getInstance().exceptionCaught(e2);
                            } finally {
                                AbstractPollingIoConnector.this.disposalFuture.setDone();
                            }
                        }
                    } catch (Throwable th) {
                        try {
                            synchronized (AbstractPollingIoConnector.this.disposalLock) {
                                if (AbstractPollingIoConnector.this.isDisposing()) {
                                    AbstractPollingIoConnector.this.destroy();
                                }
                                AbstractPollingIoConnector.this.disposalFuture.setDone();
                                throw th;
                            }
                        } catch (Exception e3) {
                            try {
                                ExceptionMonitor.getInstance().exceptionCaught(e3);
                            } finally {
                                AbstractPollingIoConnector.this.disposalFuture.setDone();
                            }
                        }
                    }
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    public final class ConnectionRequest extends DefaultConnectFuture {
        /* access modifiers changed from: private */
        public final long deadline;
        /* access modifiers changed from: private */
        public final H handle;
        private final IoSessionInitializer<? extends ConnectFuture> sessionInitializer;

        public ConnectionRequest(H handle2, IoSessionInitializer<? extends ConnectFuture> callback) {
            this.handle = handle2;
            long timeout = AbstractPollingIoConnector.this.getConnectTimeoutMillis();
            if (timeout <= 0) {
                this.deadline = Long.MAX_VALUE;
            } else {
                this.deadline = System.currentTimeMillis() + timeout;
            }
            this.sessionInitializer = callback;
        }

        public H getHandle() {
            return this.handle;
        }

        public long getDeadline() {
            return this.deadline;
        }

        public IoSessionInitializer<? extends ConnectFuture> getSessionInitializer() {
            return this.sessionInitializer;
        }

        public boolean cancel() {
            if (isDone() || !super.cancel()) {
                return true;
            }
            AbstractPollingIoConnector.this.cancelQueue.add(this);
            AbstractPollingIoConnector.this.startupWorker();
            AbstractPollingIoConnector.this.wakeup();
            return true;
        }
    }
}
