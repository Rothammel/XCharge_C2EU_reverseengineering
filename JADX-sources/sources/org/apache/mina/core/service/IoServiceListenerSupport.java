package org.apache.mina.core.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ExceptionMonitor;

/* loaded from: classes.dex */
public class IoServiceListenerSupport {
    private volatile long activationTime;
    private final IoService service;
    private final List<IoServiceListener> listeners = new CopyOnWriteArrayList();
    private final ConcurrentMap<Long, IoSession> managedSessions = new ConcurrentHashMap();
    private final Map<Long, IoSession> readOnlyManagedSessions = Collections.unmodifiableMap(this.managedSessions);
    private final AtomicBoolean activated = new AtomicBoolean();
    private volatile int largestManagedSessionCount = 0;
    private AtomicLong cumulativeManagedSessionCount = new AtomicLong(0);

    public IoServiceListenerSupport(IoService service) {
        if (service == null) {
            throw new IllegalArgumentException("service");
        }
        this.service = service;
    }

    public void add(IoServiceListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void remove(IoServiceListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    public long getActivationTime() {
        return this.activationTime;
    }

    public Map<Long, IoSession> getManagedSessions() {
        return this.readOnlyManagedSessions;
    }

    public int getManagedSessionCount() {
        return this.managedSessions.size();
    }

    public int getLargestManagedSessionCount() {
        return this.largestManagedSessionCount;
    }

    public long getCumulativeManagedSessionCount() {
        return this.cumulativeManagedSessionCount.get();
    }

    public boolean isActive() {
        return this.activated.get();
    }

    public void fireServiceActivated() {
        if (this.activated.compareAndSet(false, true)) {
            this.activationTime = System.currentTimeMillis();
            for (IoServiceListener listener : this.listeners) {
                try {
                    listener.serviceActivated(this.service);
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
            }
        }
    }

    public void fireServiceDeactivated() {
        if (this.activated.compareAndSet(true, false)) {
            try {
                for (IoServiceListener listener : this.listeners) {
                    try {
                        listener.serviceDeactivated(this.service);
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    }
                }
            } finally {
                disconnectSessions();
            }
        }
    }

    public void fireSessionCreated(IoSession session) {
        boolean firstSession = false;
        if (session.getService() instanceof IoConnector) {
            synchronized (this.managedSessions) {
                firstSession = this.managedSessions.isEmpty();
            }
        }
        if (this.managedSessions.putIfAbsent(Long.valueOf(session.getId()), session) == null) {
            if (firstSession) {
                fireServiceActivated();
            }
            IoFilterChain filterChain = session.getFilterChain();
            filterChain.fireSessionCreated();
            filterChain.fireSessionOpened();
            int managedSessionCount = this.managedSessions.size();
            if (managedSessionCount > this.largestManagedSessionCount) {
                this.largestManagedSessionCount = managedSessionCount;
            }
            this.cumulativeManagedSessionCount.incrementAndGet();
            for (IoServiceListener l : this.listeners) {
                try {
                    l.sessionCreated(session);
                } catch (Exception e) {
                    ExceptionMonitor.getInstance().exceptionCaught(e);
                }
            }
        }
    }

    public void fireSessionDestroyed(IoSession session) {
        boolean lastSession;
        if (this.managedSessions.remove(Long.valueOf(session.getId())) != null) {
            session.getFilterChain().fireSessionClosed();
            try {
                for (IoServiceListener l : this.listeners) {
                    try {
                        l.sessionDestroyed(session);
                    } catch (Exception e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    }
                }
                if (session.getService() instanceof IoConnector) {
                    synchronized (this.managedSessions) {
                        lastSession = this.managedSessions.isEmpty();
                    }
                    if (lastSession) {
                        fireServiceDeactivated();
                    }
                }
            } catch (Throwable th) {
                if (session.getService() instanceof IoConnector) {
                    synchronized (this.managedSessions) {
                        boolean lastSession2 = this.managedSessions.isEmpty();
                        if (lastSession2) {
                            fireServiceDeactivated();
                        }
                    }
                }
                throw th;
            }
        }
    }

    private void disconnectSessions() {
        if ((this.service instanceof IoAcceptor) && ((IoAcceptor) this.service).isCloseOnDeactivation()) {
            Object lock = new Object();
            IoFutureListener<IoFuture> listener = new LockNotifyingListener(lock);
            for (IoSession s : this.managedSessions.values()) {
                s.closeNow().addListener((IoFutureListener<?>) listener);
            }
            try {
                synchronized (lock) {
                    while (!this.managedSessions.isEmpty()) {
                        lock.wait(500L);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class LockNotifyingListener implements IoFutureListener<IoFuture> {
        private final Object lock;

        public LockNotifyingListener(Object lock) {
            this.lock = lock;
        }

        @Override // org.apache.mina.core.future.IoFutureListener
        public void operationComplete(IoFuture future) {
            synchronized (this.lock) {
                this.lock.notifyAll();
            }
        }
    }
}
