package org.apache.mina.core.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IoServiceStatistics {
    private double largestReadBytesThroughput;
    private double largestReadMessagesThroughput;
    private double largestWrittenBytesThroughput;
    private double largestWrittenMessagesThroughput;
    private long lastReadBytes;
    private long lastReadMessages;
    private long lastReadTime;
    private long lastThroughputCalculationTime;
    private long lastWriteTime;
    private long lastWrittenBytes;
    private long lastWrittenMessages;
    private long readBytes;
    private double readBytesThroughput;
    private long readMessages;
    private double readMessagesThroughput;
    private int scheduledWriteBytes;
    private int scheduledWriteMessages;
    private AbstractIoService service;
    private final AtomicInteger throughputCalculationInterval = new AtomicInteger(3);
    private final Lock throughputCalculationLock = new ReentrantLock();
    private long writtenBytes;
    private double writtenBytesThroughput;
    private long writtenMessages;
    private double writtenMessagesThroughput;

    public IoServiceStatistics(AbstractIoService service2) {
        this.service = service2;
    }

    public final int getLargestManagedSessionCount() {
        return this.service.getListeners().getLargestManagedSessionCount();
    }

    public final long getCumulativeManagedSessionCount() {
        return this.service.getListeners().getCumulativeManagedSessionCount();
    }

    public final long getLastIoTime() {
        this.throughputCalculationLock.lock();
        try {
            return Math.max(this.lastReadTime, this.lastWriteTime);
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getLastReadTime() {
        this.throughputCalculationLock.lock();
        try {
            return this.lastReadTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getLastWriteTime() {
        this.throughputCalculationLock.lock();
        try {
            return this.lastWriteTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getReadBytes() {
        this.throughputCalculationLock.lock();
        try {
            return this.readBytes;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getWrittenBytes() {
        this.throughputCalculationLock.lock();
        try {
            return this.writtenBytes;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getReadMessages() {
        this.throughputCalculationLock.lock();
        try {
            return this.readMessages;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final long getWrittenMessages() {
        this.throughputCalculationLock.lock();
        try {
            return this.writtenMessages;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getReadBytesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            resetThroughput();
            return this.readBytesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getWrittenBytesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            resetThroughput();
            return this.writtenBytesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getReadMessagesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            resetThroughput();
            return this.readMessagesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getWrittenMessagesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            resetThroughput();
            return this.writtenMessagesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getLargestReadBytesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            return this.largestReadBytesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getLargestWrittenBytesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            return this.largestWrittenBytesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getLargestReadMessagesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            return this.largestReadMessagesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final double getLargestWrittenMessagesThroughput() {
        this.throughputCalculationLock.lock();
        try {
            return this.largestWrittenMessagesThroughput;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final int getThroughputCalculationInterval() {
        return this.throughputCalculationInterval.get();
    }

    public final long getThroughputCalculationIntervalInMillis() {
        return ((long) this.throughputCalculationInterval.get()) * 1000;
    }

    public final void setThroughputCalculationInterval(int throughputCalculationInterval2) {
        if (throughputCalculationInterval2 < 0) {
            throw new IllegalArgumentException("throughputCalculationInterval: " + throughputCalculationInterval2);
        }
        this.throughputCalculationInterval.set(throughputCalculationInterval2);
    }

    /* access modifiers changed from: protected */
    public final void setLastReadTime(long lastReadTime2) {
        this.throughputCalculationLock.lock();
        try {
            this.lastReadTime = lastReadTime2;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public final void setLastWriteTime(long lastWriteTime2) {
        this.throughputCalculationLock.lock();
        try {
            this.lastWriteTime = lastWriteTime2;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    private void resetThroughput() {
        if (this.service.getManagedSessionCount() == 0) {
            this.readBytesThroughput = 0.0d;
            this.writtenBytesThroughput = 0.0d;
            this.readMessagesThroughput = 0.0d;
            this.writtenMessagesThroughput = 0.0d;
        }
    }

    public void updateThroughput(long currentTime) {
        this.throughputCalculationLock.lock();
        try {
            int interval = (int) (currentTime - this.lastThroughputCalculationTime);
            long minInterval = getThroughputCalculationIntervalInMillis();
            if (minInterval == 0 || ((long) interval) < minInterval) {
                this.throughputCalculationLock.unlock();
                return;
            }
            long readBytes2 = this.readBytes;
            long writtenBytes2 = this.writtenBytes;
            long readMessages2 = this.readMessages;
            long writtenMessages2 = this.writtenMessages;
            this.readBytesThroughput = (((double) (readBytes2 - this.lastReadBytes)) * 1000.0d) / ((double) interval);
            this.writtenBytesThroughput = (((double) (writtenBytes2 - this.lastWrittenBytes)) * 1000.0d) / ((double) interval);
            this.readMessagesThroughput = (((double) (readMessages2 - this.lastReadMessages)) * 1000.0d) / ((double) interval);
            this.writtenMessagesThroughput = (((double) (writtenMessages2 - this.lastWrittenMessages)) * 1000.0d) / ((double) interval);
            if (this.readBytesThroughput > this.largestReadBytesThroughput) {
                this.largestReadBytesThroughput = this.readBytesThroughput;
            }
            if (this.writtenBytesThroughput > this.largestWrittenBytesThroughput) {
                this.largestWrittenBytesThroughput = this.writtenBytesThroughput;
            }
            if (this.readMessagesThroughput > this.largestReadMessagesThroughput) {
                this.largestReadMessagesThroughput = this.readMessagesThroughput;
            }
            if (this.writtenMessagesThroughput > this.largestWrittenMessagesThroughput) {
                this.largestWrittenMessagesThroughput = this.writtenMessagesThroughput;
            }
            this.lastReadBytes = readBytes2;
            this.lastWrittenBytes = writtenBytes2;
            this.lastReadMessages = readMessages2;
            this.lastWrittenMessages = writtenMessages2;
            this.lastThroughputCalculationTime = currentTime;
            this.throughputCalculationLock.unlock();
        } catch (Throwable th) {
            this.throughputCalculationLock.unlock();
            throw th;
        }
    }

    public final void increaseReadBytes(long nbBytesRead, long currentTime) {
        this.throughputCalculationLock.lock();
        try {
            this.readBytes += nbBytesRead;
            this.lastReadTime = currentTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void increaseReadMessages(long currentTime) {
        this.throughputCalculationLock.lock();
        try {
            this.readMessages++;
            this.lastReadTime = currentTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void increaseWrittenBytes(int nbBytesWritten, long currentTime) {
        this.throughputCalculationLock.lock();
        try {
            this.writtenBytes += (long) nbBytesWritten;
            this.lastWriteTime = currentTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void increaseWrittenMessages(long currentTime) {
        this.throughputCalculationLock.lock();
        try {
            this.writtenMessages++;
            this.lastWriteTime = currentTime;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final int getScheduledWriteBytes() {
        this.throughputCalculationLock.lock();
        try {
            return this.scheduledWriteBytes;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void increaseScheduledWriteBytes(int increment) {
        this.throughputCalculationLock.lock();
        try {
            this.scheduledWriteBytes += increment;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final int getScheduledWriteMessages() {
        this.throughputCalculationLock.lock();
        try {
            return this.scheduledWriteMessages;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void increaseScheduledWriteMessages() {
        this.throughputCalculationLock.lock();
        try {
            this.scheduledWriteMessages++;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    public final void decreaseScheduledWriteMessages() {
        this.throughputCalculationLock.lock();
        try {
            this.scheduledWriteMessages--;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public void setLastThroughputCalculationTime(long lastThroughputCalculationTime2) {
        this.throughputCalculationLock.lock();
        try {
            this.lastThroughputCalculationTime = lastThroughputCalculationTime2;
        } finally {
            this.throughputCalculationLock.unlock();
        }
    }
}
