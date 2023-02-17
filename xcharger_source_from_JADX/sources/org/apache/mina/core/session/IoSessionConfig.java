package org.apache.mina.core.session;

public interface IoSessionConfig {
    int getBothIdleTime();

    long getBothIdleTimeInMillis();

    int getIdleTime(IdleStatus idleStatus);

    long getIdleTimeInMillis(IdleStatus idleStatus);

    int getMaxReadBufferSize();

    int getMinReadBufferSize();

    int getReadBufferSize();

    int getReaderIdleTime();

    long getReaderIdleTimeInMillis();

    int getThroughputCalculationInterval();

    long getThroughputCalculationIntervalInMillis();

    int getWriteTimeout();

    long getWriteTimeoutInMillis();

    int getWriterIdleTime();

    long getWriterIdleTimeInMillis();

    boolean isUseReadOperation();

    void setAll(IoSessionConfig ioSessionConfig);

    void setBothIdleTime(int i);

    void setIdleTime(IdleStatus idleStatus, int i);

    void setMaxReadBufferSize(int i);

    void setMinReadBufferSize(int i);

    void setReadBufferSize(int i);

    void setReaderIdleTime(int i);

    void setThroughputCalculationInterval(int i);

    void setUseReadOperation(boolean z);

    void setWriteTimeout(int i);

    void setWriterIdleTime(int i);
}
