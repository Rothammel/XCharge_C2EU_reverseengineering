package org.apache.mina.core.session;

/* loaded from: classes.dex */
public abstract class AbstractIoSessionConfig implements IoSessionConfig {
    private int idleTimeForBoth;
    private int idleTimeForRead;
    private int idleTimeForWrite;
    private boolean useReadOperation;
    private int minReadBufferSize = 64;
    private int readBufferSize = 2048;
    private int maxReadBufferSize = 65536;
    private int writeTimeout = 60;
    private int throughputCalculationInterval = 3;

    @Override // org.apache.mina.core.session.IoSessionConfig
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

    @Override // org.apache.mina.core.session.IoSessionConfig
    public int getReadBufferSize() {
        return this.readBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setReadBufferSize(int readBufferSize) {
        if (readBufferSize <= 0) {
            throw new IllegalArgumentException("readBufferSize: " + readBufferSize + " (expected: 1+)");
        }
        this.readBufferSize = readBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public int getMinReadBufferSize() {
        return this.minReadBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setMinReadBufferSize(int minReadBufferSize) {
        if (minReadBufferSize <= 0) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: 1+)");
        }
        if (minReadBufferSize > this.maxReadBufferSize) {
            throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: smaller than " + this.maxReadBufferSize + ')');
        }
        this.minReadBufferSize = minReadBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public int getMaxReadBufferSize() {
        return this.maxReadBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setMaxReadBufferSize(int maxReadBufferSize) {
        if (maxReadBufferSize <= 0) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: 1+)");
        }
        if (maxReadBufferSize < this.minReadBufferSize) {
            throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: greater than " + this.minReadBufferSize + ')');
        }
        this.maxReadBufferSize = maxReadBufferSize;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
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

    @Override // org.apache.mina.core.session.IoSessionConfig
    public long getIdleTimeInMillis(IdleStatus status) {
        return getIdleTime(status) * 1000;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setIdleTime(IdleStatus status, int idleTime) {
        if (idleTime < 0) {
            throw new IllegalArgumentException("Illegal idle time: " + idleTime);
        }
        if (status == IdleStatus.BOTH_IDLE) {
            this.idleTimeForBoth = idleTime;
        } else if (status == IdleStatus.READER_IDLE) {
            this.idleTimeForRead = idleTime;
        } else if (status == IdleStatus.WRITER_IDLE) {
            this.idleTimeForWrite = idleTime;
        } else {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        }
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final int getBothIdleTime() {
        return getIdleTime(IdleStatus.BOTH_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final long getBothIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.BOTH_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final int getReaderIdleTime() {
        return getIdleTime(IdleStatus.READER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final long getReaderIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.READER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final int getWriterIdleTime() {
        return getIdleTime(IdleStatus.WRITER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public final long getWriterIdleTimeInMillis() {
        return getIdleTimeInMillis(IdleStatus.WRITER_IDLE);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setBothIdleTime(int idleTime) {
        setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setReaderIdleTime(int idleTime) {
        setIdleTime(IdleStatus.READER_IDLE, idleTime);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setWriterIdleTime(int idleTime) {
        setIdleTime(IdleStatus.WRITER_IDLE, idleTime);
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public long getWriteTimeoutInMillis() {
        return this.writeTimeout * 1000;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setWriteTimeout(int writeTimeout) {
        if (writeTimeout < 0) {
            throw new IllegalArgumentException("Illegal write timeout: " + writeTimeout);
        }
        this.writeTimeout = writeTimeout;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public boolean isUseReadOperation() {
        return this.useReadOperation;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setUseReadOperation(boolean useReadOperation) {
        this.useReadOperation = useReadOperation;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public int getThroughputCalculationInterval() {
        return this.throughputCalculationInterval;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public void setThroughputCalculationInterval(int throughputCalculationInterval) {
        if (throughputCalculationInterval < 0) {
            throw new IllegalArgumentException("throughputCalculationInterval: " + throughputCalculationInterval);
        }
        this.throughputCalculationInterval = throughputCalculationInterval;
    }

    @Override // org.apache.mina.core.session.IoSessionConfig
    public long getThroughputCalculationIntervalInMillis() {
        return this.throughputCalculationInterval * 1000;
    }
}
