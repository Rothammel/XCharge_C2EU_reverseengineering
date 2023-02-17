package org.apache.mina.filter.executor;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.session.IoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class IoEventQueueThrottle implements IoEventQueueHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoEventQueueThrottle.class);
    private final AtomicInteger counter;
    private final IoEventSizeEstimator eventSizeEstimator;
    private final Object lock;
    private volatile int threshold;
    private int waiters;

    public IoEventQueueThrottle() {
        this(new DefaultIoEventSizeEstimator(), 65536);
    }

    public IoEventQueueThrottle(int threshold) {
        this(new DefaultIoEventSizeEstimator(), threshold);
    }

    public IoEventQueueThrottle(IoEventSizeEstimator eventSizeEstimator, int threshold) {
        this.lock = new Object();
        this.counter = new AtomicInteger();
        if (eventSizeEstimator == null) {
            throw new IllegalArgumentException("eventSizeEstimator");
        }
        this.eventSizeEstimator = eventSizeEstimator;
        setThreshold(threshold);
    }

    public IoEventSizeEstimator getEventSizeEstimator() {
        return this.eventSizeEstimator;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public int getCounter() {
        return this.counter.get();
    }

    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold: " + threshold);
        }
        this.threshold = threshold;
    }

    @Override // org.apache.mina.filter.executor.IoEventQueueHandler
    public boolean accept(Object source, IoEvent event) {
        return true;
    }

    @Override // org.apache.mina.filter.executor.IoEventQueueHandler
    public void offered(Object source, IoEvent event) {
        int eventSize = estimateSize(event);
        int currentCounter = this.counter.addAndGet(eventSize);
        logState();
        if (currentCounter >= this.threshold) {
            block();
        }
    }

    @Override // org.apache.mina.filter.executor.IoEventQueueHandler
    public void polled(Object source, IoEvent event) {
        int eventSize = estimateSize(event);
        int currentCounter = this.counter.addAndGet(-eventSize);
        logState();
        if (currentCounter < this.threshold) {
            unblock();
        }
    }

    private int estimateSize(IoEvent event) {
        int size = getEventSizeEstimator().estimateSize(event);
        if (size < 0) {
            throw new IllegalStateException(IoEventSizeEstimator.class.getSimpleName() + " returned a negative value (" + size + "): " + event);
        }
        return size;
    }

    private void logState() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " state: " + this.counter.get() + " / " + getThreshold());
        }
    }

    protected void block() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " blocked: " + this.counter.get() + " >= " + this.threshold);
        }
        synchronized (this.lock) {
            while (this.counter.get() >= this.threshold) {
                this.waiters++;
                try {
                    this.lock.wait();
                    this.waiters--;
                } catch (InterruptedException e) {
                    this.waiters--;
                } catch (Throwable th) {
                    this.waiters--;
                    throw th;
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " unblocked: " + this.counter.get() + " < " + this.threshold);
        }
    }

    protected void unblock() {
        synchronized (this.lock) {
            if (this.waiters > 0) {
                this.lock.notifyAll();
            }
        }
    }
}