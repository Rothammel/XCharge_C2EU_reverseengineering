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

/* loaded from: classes.dex */
public abstract class AbstractPollingIoConnector<T extends AbstractIoSession, H> extends AbstractIoConnector {
    private final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> cancelQueue;
    private final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> connectQueue;
    private final AtomicReference<AbstractPollingIoConnector<T, H>.Connector> connectorRef;
    private final boolean createdProcessor;
    private final AbstractIoService.ServiceOperationFuture disposalFuture;
    private final IoProcessor<T> processor;
    private volatile boolean selectable;

    protected abstract Iterator<H> allHandles();

    protected abstract void close(H h) throws Exception;

    protected abstract boolean connect(H h, SocketAddress socketAddress) throws Exception;

    protected abstract void destroy() throws Exception;

    protected abstract boolean finishConnect(H h) throws Exception;

    protected abstract AbstractPollingIoConnector<T, H>.ConnectionRequest getConnectionRequest(H h);

    protected abstract void init() throws Exception;

    protected abstract H newHandle(SocketAddress socketAddress) throws Exception;

    protected abstract T newSession(IoProcessor<T> ioProcessor, H h) throws Exception;

    protected abstract void register(H h, AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest) throws Exception;

    protected abstract int select(int i) throws Exception;

    protected abstract Iterator<H> selectedHandles();

    protected abstract void wakeup();

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractPollingIoConnector(IoSessionConfig sessionConfig, Class<? extends IoProcessor<T>> processorClass) {
        this(sessionConfig, null, new SimpleIoProcessorPool(processorClass), true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractPollingIoConnector(IoSessionConfig sessionConfig, Class<? extends IoProcessor<T>> processorClass, int processorCount) {
        this(sessionConfig, null, new SimpleIoProcessorPool(processorClass, processorCount), true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractPollingIoConnector(IoSessionConfig sessionConfig, IoProcessor<T> processor) {
        this(sessionConfig, null, processor, false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor) {
        this(sessionConfig, executor, processor, false);
    }

    private AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor, boolean createdProcessor) {
        super(sessionConfig, executor);
        this.connectQueue = new ConcurrentLinkedQueue();
        this.cancelQueue = new ConcurrentLinkedQueue();
        this.disposalFuture = new AbstractIoService.ServiceOperationFuture();
        this.connectorRef = new AtomicReference<>();
        if (processor == null) {
            throw new IllegalArgumentException("processor");
        }
        this.processor = processor;
        this.createdProcessor = createdProcessor;
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

    @Override // org.apache.mina.core.service.AbstractIoService
    protected final void dispose0() throws Exception {
        startupWorker();
        wakeup();
    }

    @Override // org.apache.mina.core.service.AbstractIoConnector
    protected final ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        H handle = null;
        boolean success = false;
        try {
            try {
                handle = newHandle(localAddress);
                if (!connect((AbstractPollingIoConnector<T, H>) handle, remoteAddress)) {
                    success = true;
                    AbstractPollingIoConnector<T, H>.ConnectionRequest request = new ConnectionRequest(handle, sessionInitializer);
                    this.connectQueue.add(request);
                    startupWorker();
                    wakeup();
                    return request;
                }
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
            } finally {
                if (!success && handle != null) {
                    try {
                        close(handle);
                    } catch (Exception e2) {
                        ExceptionMonitor.getInstance().exceptionCaught(e2);
                    }
                }
            }
        } catch (Exception e3) {
            ConnectFuture future2 = DefaultConnectFuture.newFailedFuture(e3);
            if (success || handle == null) {
                return future2;
            }
            try {
                close(handle);
                return future2;
            } catch (Exception e4) {
                ExceptionMonitor.getInstance().exceptionCaught(e4);
                return future2;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startupWorker() {
        if (!this.selectable) {
            this.connectQueue.clear();
            this.cancelQueue.clear();
        }
        if (this.connectorRef.get() == null) {
            AbstractPollingIoConnector<T, H>.Connector connector = new Connector();
            if (this.connectorRef.compareAndSet(null, connector)) {
                executeWorker(connector);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public int registerNew() {
        int nHandles = 0;
        while (true) {
            AbstractPollingIoConnector<T, H>.ConnectionRequest req = this.connectQueue.poll();
            if (req == null) {
                return nHandles;
            }
            Object obj = ((ConnectionRequest) req).handle;
            try {
                register(obj, req);
                nHandles++;
            } catch (Exception e) {
                req.setException(e);
                try {
                    close(obj);
                } catch (Exception e2) {
                    ExceptionMonitor.getInstance().exceptionCaught(e2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public int cancelKeys() {
        int nHandles = 0;
        while (true) {
            AbstractPollingIoConnector<T, H>.ConnectionRequest req = this.cancelQueue.poll();
            if (req == null) {
                break;
            }
            try {
                close(((ConnectionRequest) req).handle);
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

    /* JADX INFO: Access modifiers changed from: private */
    public int processConnections(Iterator<H> handlers) {
        int nHandles = 0;
        while (handlers.hasNext()) {
            H handle = handlers.next();
            handlers.remove();
            AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest = getConnectionRequest(handle);
            if (connectionRequest != null) {
                try {
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

    /* JADX INFO: Access modifiers changed from: private */
    public void processTimedOutSessions(Iterator<H> handles) {
        long currentTime = System.currentTimeMillis();
        while (handles.hasNext()) {
            H handle = handles.next();
            AbstractPollingIoConnector<T, H>.ConnectionRequest connectionRequest = getConnectionRequest(handle);
            if (connectionRequest != null && currentTime >= ((ConnectionRequest) connectionRequest).deadline) {
                connectionRequest.setException(new ConnectException("Connection timed out."));
                this.cancelQueue.offer(connectionRequest);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Connector implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled;

        static {
            $assertionsDisabled = !AbstractPollingIoConnector.class.desiredAssertionStatus();
        }

        private Connector() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() != this) {
                throw new AssertionError();
            }
            int nHandles = 0;
            while (true) {
                if (!AbstractPollingIoConnector.this.selectable) {
                    break;
                }
                try {
                    int timeout = (int) Math.min(AbstractPollingIoConnector.this.getConnectTimeoutMillis(), 1000L);
                    int selected = AbstractPollingIoConnector.this.select(timeout);
                    int nHandles2 = nHandles + AbstractPollingIoConnector.this.registerNew();
                    if (nHandles2 == 0) {
                        AbstractPollingIoConnector.this.connectorRef.set(null);
                        if (AbstractPollingIoConnector.this.connectQueue.isEmpty()) {
                            if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() == this) {
                                throw new AssertionError();
                            }
                        } else if (AbstractPollingIoConnector.this.connectorRef.compareAndSet(null, this)) {
                            if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() != this) {
                                throw new AssertionError();
                            }
                        } else if (!$assertionsDisabled && AbstractPollingIoConnector.this.connectorRef.get() == this) {
                            throw new AssertionError();
                        }
                    }
                    if (selected > 0) {
                        nHandles2 -= AbstractPollingIoConnector.this.processConnections(AbstractPollingIoConnector.this.selectedHandles());
                    }
                    AbstractPollingIoConnector.this.processTimedOutSessions(AbstractPollingIoConnector.this.allHandles());
                    nHandles = nHandles2 - AbstractPollingIoConnector.this.cancelKeys();
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
            if (AbstractPollingIoConnector.this.selectable && AbstractPollingIoConnector.this.isDisposing()) {
                AbstractPollingIoConnector.this.selectable = false;
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
                    } catch (Exception e2) {
                        ExceptionMonitor.getInstance().exceptionCaught(e2);
                    } finally {
                    }
                } catch (Throwable th) {
                    try {
                    } catch (Exception e3) {
                        ExceptionMonitor.getInstance().exceptionCaught(e3);
                    } finally {
                    }
                    synchronized (AbstractPollingIoConnector.this.disposalLock) {
                        if (AbstractPollingIoConnector.this.isDisposing()) {
                            AbstractPollingIoConnector.this.destroy();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public final class ConnectionRequest extends DefaultConnectFuture {
        private final long deadline;
        private final H handle;
        private final IoSessionInitializer<? extends ConnectFuture> sessionInitializer;

        public ConnectionRequest(H handle, IoSessionInitializer<? extends ConnectFuture> callback) {
            this.handle = handle;
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

        @Override // org.apache.mina.core.future.DefaultConnectFuture, org.apache.mina.core.future.ConnectFuture
        public boolean cancel() {
            if (!isDone()) {
                boolean justCancelled = super.cancel();
                if (justCancelled) {
                    AbstractPollingIoConnector.this.cancelQueue.add(this);
                    AbstractPollingIoConnector.this.startupWorker();
                    AbstractPollingIoConnector.this.wakeup();
                    return true;
                }
                return true;
            }
            return true;
        }
    }
}
