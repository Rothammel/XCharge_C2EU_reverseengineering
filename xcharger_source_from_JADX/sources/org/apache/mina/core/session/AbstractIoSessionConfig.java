package org.apache.mina.core.session;

public abstract class AbstractIoSessionConfig implements IoSessionConfig {
    private int idleTimeForBoth;
    private int idleTimeForRead;
    private int idleTimeForWrite;
    private int maxReadBufferSize = 65536;
    private int minReadBufferSize = 64;
    private int readBufferSize = 2048;
    private int throughputCalculationInterval = 3;
    private boolean useReadOperation;
    private int writeTimeout = 60;

    protected AbstractIoSessionConfig() {
    }

    public void setAll(IoSessionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        setReadBufferSize(config.getReadBufferSize());
        setMinReadBufferSize(config.getMinReadBufferSize());
        setMaxReadBufferSize(config.getMaxReadBufferSize());
        setIdleTime(IdleStatus.BOTH_IDLE, config.getIdleTime(IdleStatus.BOTH_IDLE));
        setIdleTime(IdleStatus.READER_IDLE, config.getIdleTime(IdleStatus.READER_IDLE));
        setIdleTime(IdleStatus.WRITER_IDLE, config.getIdleTime(IdleStatus.WRITER_IDLE));
        setWriteTimeout(config.getWriteTimeout());
        setUseReadOperation(config.isUseReadOperation());
        setThroughputCalculationInterval(config.getThroughputCalculationInterval());
    }

    public int getReadBufferSize() {
        return this.readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize2) {
        if (readBufferSize2 <= 0) {
            throw new IllegalArgumentException("readBufferSize: " + readBufferSize2 + " (expected: 1+)");
        }
        this.readBufferSize = readBufferSize2;
    }

    public int getMinReadBufferSize() {
        return this.minReadBufferSize;
    }

    public void setMinReadBufferSize(int minReadBufferSize2) {
        if (minReadBufferSize2 <= 0) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize2 + " (expected: 1+)");
        } else if (minReadBufferSize2 > this.maxReadBufferSize) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize2 + " (expected: smaller than " + this.maxReadBufferSize + ')');
        } else {
            this.minReadBufferSize = minReadBufferSize2;
        }
    }

    public int getMaxReadBufferSize() {
        return this.maxReadBufferSize;
    }

    public void setMaxReadBufferSize(int maxReadBufferSize2) {
        if (maxReadBufferSize2 <= 0) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize2 + " (expected: 1+)");
        } else if (maxReadBufferSize2 < this.minReadBufferSize) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize2 + " (expected: greater than " + this.minReadBufferSize + ')');
        } else {
            this.maxReadBufferSize = maxReadBufferSize2;
        }
    }

    public int getIdleTime(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return this.idleTimeForBoth;
        }
        if (status == IdleStatus.READER_IDLE) {
            return this.idleTimeForRead;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return this.idleTimeForWrite;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    public long getIdleTimeInMillis(IdleStatus status) {
        return ((long) getIdleTime(status)) * 1000;
    }

    public void setIdleTime(IdleStatus status, int idleTime) {
        if (idleTime < 0) {
            throw new IllegalArgumentException("Illegal idle time: " + idleTime);
        } else if (status == IdleStatus.BOTH_IDLE) {
            this.idleTimeForBoth = idleTime;
        } else if (status == IdleStatus.READER_IDLE) {
            this.idleTimeForRead = idleTime;
        } else if (status == IdleStatus.WRITER_IDLE) {
            this.idleTimeForWrite = idleTime;
        } else {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        }
    }

    public final int getBothIdleTime() {
        return getIdleTime(IdleStatus.BOTH_IDLE);
    }

    public final long getBothIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.BOTH_IDLE);
    }

    public final int getReaderIdleTime() {
        return getIdleTime(IdleStatus.READER_IDLE);
    }

    public final long getReaderIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.READER_IDLE);
    }

    public final int getWriterIdleTime() {
        return getIdleTime(IdleStatus.WRITER_IDLE);
    }

    public final long getWriterIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.WRITER_IDLE);
    }

    public void setBothIdleTime(int idleTime) {
        setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
    }

    public void setReaderIdleTime(int idleTime) {
        setIdleTime(IdleStatus.READER_IDLE, idleTime);
    }

    public void setWriterIdleTime(int idleTime) {
        setIdleTime(IdleStatus.WRITER_IDLE, idleTime);
    }

    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    public long getWriteTimeoutInMillis() {
        return ((long) this.writeTimeout) * 1000;
    }

    public void setWriteTimeout(int writeTimeout2) {
        if (writeTimeout2 < 0) {
            throw new IllegalArgumentException("Illegal write timeout: " + writeTimeout2);
        }
        this.writeTimeout = writeTimeout2;
    }

    public boolean isUseReadOperation() {
        return this.useReadOperation;
    }

    public void setUseReadOperation(boolean useReadOperation2) {
        this.useReadOperation = useReadOperation2;
    }

    public int getThroughputCalculationInterval() {
        return this.throughputCalculationInterval;
    }

    public void setThroughputCalculationInterval(int throughputCalculationInterval2) {
        if (throughputCalculationInterval2 < 0) {
            throw new IllegalArgumentException("throughputCalculationInterval: " + throughputCalculationInterval2);
        }
        this.throughputCalculationInterval = throughputCalculationInterval2;
    }

    public long getThroughputCalculationIntervalInMillis() {
        return ((long) this.throughputCalculationInterval) * 1000;
    }
}
