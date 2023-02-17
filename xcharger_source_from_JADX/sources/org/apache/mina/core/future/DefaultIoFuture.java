package org.apache.mina.core.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.polling.AbstractPollingIoProcessor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ExceptionMonitor;

public class DefaultIoFuture implements IoFuture {
    private static final long DEAD_LOCK_CHECK_INTERVAL = 5000;
    private IoFutureListener<?> firstListener;
    private final Object lock = this;
    private List<IoFutureListener<?>> otherListeners;
    private boolean ready;
    private Object result;
    private final IoSession session;
    private int waiters;

    public DefaultIoFuture(IoSession session2) {
        this.session = session2;
    }

    public IoSession getSession() {
        return this.session;
    }

    @Deprecated
    public void join() {
        awaitUninterruptibly();
    }

    @Deprecated
    public boolean join(long timeoutMillis) {
        return awaitUninterruptibly(timeoutMillis);
    }

    /* JADX INFO: finally extract failed */
    public IoFuture await() throws InterruptedException {
        synchronized (this.lock) {
            while (!this.ready) {
                this.waiters++;
                try {
                    this.lock.wait(5000);
                    this.waiters--;
                    if (!this.ready) {
                        checkDeadLock();
                    }
                } catch (Throwable th) {
                    this.waiters--;
                    if (!this.ready) {
                        checkDeadLock();
                    }
                    throw th;
                }
            }
        }
        return this;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return await0(unit.toMillis(timeout), true);
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(timeoutMillis, true);
    }

    public IoFuture awaitUninterruptibly() {
        try {
            await0(Long.MAX_VALUE, false);
        } catch (InterruptedException e) {
        }
        return this;
    }

    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        try {
            return await0(unit.toMillis(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    public boolean awaitUninterruptibly(long timeoutMillis) {
        try {
            return await0(timeoutMillis, false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    private boolean await0(long timeoutMillis, boolean interruptable) throws InterruptedException {
        boolean z;
        long endTime = System.currentTimeMillis() + timeoutMillis;
        if (endTime < 0) {
            endTime = Long.MAX_VALUE;
        }
        synchronized (this.lock) {
            if (this.ready || timeoutMillis <= 0) {
                z = this.ready;
            } else {
                this.waiters++;
                while (true) {
                    try {
                        this.lock.wait(Math.min(timeoutMillis, 5000));
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        }
                    } catch (Throwable th) {
                        this.waiters--;
                        if (!this.ready) {
                            checkDeadLock();
                        }
                        throw th;
                    }
                    if (this.ready || endTime < System.currentTimeMillis()) {
                        z = this.ready;
                    } else {
                        checkDeadLock();
                    }
                }
                z = this.ready;
                this.waiters--;
                if (!this.ready) {
                    checkDeadLock();
                }
            }
        }
        return z;
    }

    private void checkDeadLock() {
        int i = 0;
        if ((this instanceof CloseFuture) || (this instanceof WriteFuture) || (this instanceof ReadFuture) || (this instanceof ConnectFuture)) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackElement : stackTrace) {
                if (AbstractPollingIoProcessor.class.getName().equals(stackElement.getClassName())) {
                    new IllegalStateException("t").getStackTrace();
                    throw new IllegalStateException("DEAD LOCK: " + IoFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + IoFutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
                }
            }
            int length = stackTrace.length;
            while (i < length) {
                try {
                    if (IoProcessor.class.isAssignableFrom(DefaultIoFuture.class.getClassLoader().loadClass(stackTrace[i].getClassName()))) {
                        throw new IllegalStateException("DEAD LOCK: " + IoFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + IoFutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
                    }
                    i++;
                } catch (ClassNotFoundException e) {
                }
            }
        }
    }

    public boolean isDone() {
        boolean z;
        synchronized (this.lock) {
            z = this.ready;
        }
        return z;
    }

    public boolean setValue(Object newValue) {
        boolean z = true;
        synchronized (this.lock) {
            if (this.ready) {
                z = false;
            } else {
                this.result = newValue;
                this.ready = true;
                if (this.waiters > 0) {
                    this.lock.notifyAll();
                }
                notifyListeners();
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public Object getValue() {
        Object obj;
        synchronized (this.lock) {
            obj = this.result;
        }
        return obj;
    }

    public IoFuture addListener(IoFutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }
        synchronized (this.lock) {
            if (this.ready) {
                notifyListener(listener);
            } else if (this.firstListener == null) {
                this.firstListener = listener;
            } else {
                if (this.otherListeners == null) {
                    this.otherListeners = new ArrayList(1);
                }
                this.otherListeners.add(listener);
            }
        }
        return this;
    }

    public IoFuture removeListener(IoFutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }
        synchronized (this.lock) {
            if (!this.ready) {
                if (listener == this.firstListener) {
                    if (this.otherListeners == null || this.otherListeners.isEmpty()) {
                        this.firstListener = null;
                    } else {
                        this.firstListener = this.otherListeners.remove(0);
                    }
                } else if (this.otherListeners != null) {
                    this.otherListeners.remove(listener);
                }
            }
        }
        return this;
    }

    private void notifyListeners() {
        if (this.firstListener != null) {
            notifyListener(this.firstListener);
            this.firstListener = null;
            if (this.otherListeners != null) {
                for (IoFutureListener<?> listener : this.otherListeners) {
                    notifyListener(listener);
                }
                this.otherListeners = null;
            }
        }
    }

    private void notifyListener(IoFutureListener listener) {
        try {
            listener.operationComplete(this);
        } catch (Exception e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
        }
    }
}