package org.apache.mina.filter.executor;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.session.IoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoEventQueueThrottle implements IoEventQueueHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) IoEventQueueThrottle.class);
    private final AtomicInteger counter;
    private final IoEventSizeEstimator eventSizeEstimator;
    private final Object lock;
    private volatile int threshold;
    private int waiters;

    public IoEventQueueThrottle() {
        this(new DefaultIoEventSizeEstimator(), 65536);
    }

    public IoEventQueueThrottle(int threshold2) {
        this(new DefaultIoEventSizeEstimator(), threshold2);
    }

    public IoEventQueueThrottle(IoEventSizeEstimator eventSizeEstimator2, int threshold2) {
        this.lock = new Object();
        this.counter = new AtomicInteger();
        if (eventSizeEstimator2 == null) {
            throw new IllegalArgumentException("eventSizeEstimator");
        }
        this.eventSizeEstimator = eventSizeEstimator2;
        setThreshold(threshold2);
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

    public void setThreshold(int threshold2) {
        if (threshold2 <= 0) {
            throw new IllegalArgumentException("threshold: " + threshold2);
        }
        this.threshold = threshold2;
    }

    public boolean accept(Object source, IoEvent event) {
        return true;
    }

    public void offered(Object source, IoEvent event) {
        int currentCounter = this.counter.addAndGet(estimateSize(event));
        logState();
        if (currentCounter >= this.threshold) {
            block();
        }
    }

    public void polled(Object source, IoEvent event) {
        int currentCounter = this.counter.addAndGet(-estimateSize(event));
        logState();
        if (currentCounter < this.threshold) {
            unblock();
        }
    }

    private int estimateSize(IoEvent event) {
        int size = getEventSizeEstimator().estimateSize(event);
        if (size >= 0) {
            return size;
        }
        throw new IllegalStateException(IoEventSizeEstimator.class.getSimpleName() + " returned " + "a negative value (" + size + "): " + event);
    }

    private void logState() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " state: " + this.counter.get() + " / " + getThreshold());
        }
    }

    /* access modifiers changed from: protected */
    public void block() {
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

    /* access modifiers changed from: protected */
    public void unblock() {
        synchronized (this.lock) {
            if (this.waiters > 0) {
                this.lock.notifyAll();
            }
        }
    }
}
