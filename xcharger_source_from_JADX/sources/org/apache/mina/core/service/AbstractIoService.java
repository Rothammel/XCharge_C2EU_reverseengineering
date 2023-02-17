package org.apache.mina.core.service;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.IoUtil;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.DefaultIoSessionDataStructureFactory;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;
import org.apache.mina.core.session.IoSessionInitializationException;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.util.NamePreservingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIoService implements IoService {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) AbstractIoService.class);

    /* renamed from: id */
    private static final AtomicInteger f184id = new AtomicInteger();
    private final boolean createdExecutor;
    /* access modifiers changed from: protected */
    public final Object disposalLock = new Object();
    private volatile boolean disposed;
    private volatile boolean disposing;
    private final Executor executor;
    private IoFilterChainBuilder filterChainBuilder = new DefaultIoFilterChainBuilder();
    private IoHandler handler;
    private final IoServiceListenerSupport listeners;
    private final IoServiceListener serviceActivationListener = new IoServiceListener() {
        public void serviceActivated(IoService service) {
            AbstractIoService s = (AbstractIoService) service;
            IoServiceStatistics _stats = s.getStatistics();
            _stats.setLastReadTime(s.getActivationTime());
            _stats.setLastWriteTime(s.getActivationTime());
            _stats.setLastThroughputCalculationTime(s.getActivationTime());
        }

        public void serviceDeactivated(IoService service) throws Exception {
        }

        public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
        }

        public void sessionCreated(IoSession session) throws Exception {
        }

        public void sessionClosed(IoSession session) throws Exception {
        }

        public void sessionDestroyed(IoSession session) throws Exception {
        }
    };
    protected final IoSessionConfig sessionConfig;
    private IoSessionDataStructureFactory sessionDataStructureFactory = new DefaultIoSessionDataStructureFactory();
    private IoServiceStatistics stats = new IoServiceStatistics(this);
    private final String threadName;

    /* access modifiers changed from: protected */
    public abstract void dispose0() throws Exception;

    protected AbstractIoService(IoSessionConfig sessionConfig2, Executor executor2) {
        if (sessionConfig2 == null) {
            throw new IllegalArgumentException("sessionConfig");
        } else if (getTransportMetadata() == null) {
            throw new IllegalArgumentException("TransportMetadata");
        } else if (!getTransportMetadata().getSessionConfigType().isAssignableFrom(sessionConfig2.getClass())) {
            throw new IllegalArgumentException("sessionConfig type: " + sessionConfig2.getClass() + " (expected: " + getTransportMetadata().getSessionConfigType() + ")");
        } else {
            this.listeners = new IoServiceListenerSupport(this);
            this.listeners.add(this.serviceActivationListener);
            this.sessionConfig = sessionConfig2;
            ExceptionMonitor.getInstance();
            if (executor2 == null) {
                this.executor = Executors.newCachedThreadPool();
                this.createdExecutor = true;
            } else {
                this.executor = executor2;
                this.createdExecutor = false;
            }
            this.threadName = getClass().getSimpleName() + '-' + f184id.incrementAndGet();
        }
    }

    public final IoFilterChainBuilder getFilterChainBuilder() {
        return this.filterChainBuilder;
    }

    public final void setFilterChainBuilder(IoFilterChainBuilder builder) {
        if (builder == null) {
            builder = new DefaultIoFilterChainBuilder();
        }
        this.filterChainBuilder = builder;
    }

    public final DefaultIoFilterChainBuilder getFilterChain() {
        if (this.filterChainBuilder instanceof DefaultIoFilterChainBuilder) {
            return (DefaultIoFilterChainBuilder) this.filterChainBuilder;
        }
        throw new IllegalStateException("Current filter chain builder is not a DefaultIoFilterChainBuilder.");
    }

    public final void addListener(IoServiceListener listener) {
        this.listeners.add(listener);
    }

    public final void removeListener(IoServiceListener listener) {
        this.listeners.remove(listener);
    }

    public final boolean isActive() {
        return this.listeners.isActive();
    }

    public final boolean isDisposing() {
        return this.disposing;
    }

    public final boolean isDisposed() {
        return this.disposed;
    }

    public final void dispose() {
        dispose(false);
    }

    public final void dispose(boolean awaitTermination) {
        if (!this.disposed) {
            synchronized (this.disposalLock) {
                if (!this.disposing) {
                    this.disposing = true;
                    try {
                        dispose0();
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    }
                }
            }
            if (this.createdExecutor) {
                ExecutorService e2 = (ExecutorService) this.executor;
                e2.shutdownNow();
                if (awaitTermination) {
                    try {
                        LOGGER.debug("awaitTermination on {} called by thread=[{}]", (Object) this, (Object) Thread.currentThread().getName());
                        e2.awaitTermination(2147483647L, TimeUnit.SECONDS);
                        LOGGER.debug("awaitTermination on {} finished", (Object) this);
                    } catch (InterruptedException e3) {
                        LOGGER.warn("awaitTermination on [{}] was interrupted", (Object) this);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            this.disposed = true;
        }
    }

    public final Map<Long, IoSession> getManagedSessions() {
        return this.listeners.getManagedSessions();
    }

    public final int getManagedSessionCount() {
        return this.listeners.getManagedSessionCount();
    }

    public final IoHandler getHandler() {
        return this.handler;
    }

    public final void setHandler(IoHandler handler2) {
        if (handler2 == null) {
            throw new IllegalArgumentException("handler cannot be null");
        } else if (isActive()) {
            throw new IllegalStateException("handler cannot be set while the service is active.");
        } else {
            this.handler = handler2;
        }
    }

    public final IoSessionDataStructureFactory getSessionDataStructureFactory() {
        return this.sessionDataStructureFactory;
    }

    public final void setSessionDataStructureFactory(IoSessionDataStructureFactory sessionDataStructureFactory2) {
        if (sessionDataStructureFactory2 == null) {
            throw new IllegalArgumentException("sessionDataStructureFactory");
        } else if (isActive()) {
            throw new IllegalStateException("sessionDataStructureFactory cannot be set while the service is active.");
        } else {
            this.sessionDataStructureFactory = sessionDataStructureFactory2;
        }
    }

    public IoServiceStatistics getStatistics() {
        return this.stats;
    }

    public final long getActivationTime() {
        return this.listeners.getActivationTime();
    }

    public final Set<WriteFuture> broadcast(Object message) {
        final List<WriteFuture> futures = IoUtil.broadcast(message, getManagedSessions().values());
        return new AbstractSet<WriteFuture>() {
            public Iterator<WriteFuture> iterator() {
                return futures.iterator();
            }

            public int size() {
                return futures.size();
            }
        };
    }

    public final IoServiceListenerSupport getListeners() {
        return this.listeners;
    }

    /* access modifiers changed from: protected */
    public final void executeWorker(Runnable worker) {
        executeWorker(worker, (String) null);
    }

    /* access modifiers changed from: protected */
    public final void executeWorker(Runnable worker, String suffix) {
        String actualThreadName = this.threadName;
        if (suffix != null) {
            actualThreadName = actualThreadName + '-' + suffix;
        }
        this.executor.execute(new NamePreservingRunnable(worker, actualThreadName));
    }

    /* access modifiers changed from: protected */
    public final void initSession(IoSession session, IoFuture future, IoSessionInitializer sessionInitializer) {
        if (this.stats.getLastReadTime() == 0) {
            this.stats.setLastReadTime(getActivationTime());
        }
        if (this.stats.getLastWriteTime() == 0) {
            this.stats.setLastWriteTime(getActivationTime());
        }
        try {
            ((AbstractIoSession) session).setAttributeMap(session.getService().getSessionDataStructureFactory().getAttributeMap(session));
            try {
                ((AbstractIoSession) session).setWriteRequestQueue(session.getService().getSessionDataStructureFactory().getWriteRequestQueue(session));
                if (future != null && (future instanceof ConnectFuture)) {
                    session.setAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE, future);
                }
                if (sessionInitializer != null) {
                    sessionInitializer.initializeSession(session, future);
                }
                finishSessionInitialization0(session, future);
            } catch (IoSessionInitializationException e) {
                throw e;
            } catch (Exception e2) {
                throw new IoSessionInitializationException("Failed to initialize a writeRequestQueue.", e2);
            }
        } catch (IoSessionInitializationException e3) {
            throw e3;
        } catch (Exception e4) {
            throw new IoSessionInitializationException("Failed to initialize an attributeMap.", e4);
        }
    }

    /* access modifiers changed from: protected */
    public void finishSessionInitialization0(IoSession session, IoFuture future) {
    }

    protected static class ServiceOperationFuture extends DefaultIoFuture {
        public ServiceOperationFuture() {
            super((IoSession) null);
        }

        public final boolean isDone() {
            return getValue() == Boolean.TRUE;
        }

        public final void setDone() {
            setValue(Boolean.TRUE);
        }

        public final Exception getException() {
            if (getValue() instanceof Exception) {
                return (Exception) getValue();
            }
            return null;
        }

        public final void setException(Exception exception) {
            if (exception == null) {
                throw new IllegalArgumentException("exception");
            }
            setValue(exception);
        }
    }

    public int getScheduledWriteBytes() {
        return this.stats.getScheduledWriteBytes();
    }

    public int getScheduledWriteMessages() {
        return this.stats.getScheduledWriteMessages();
    }
}
