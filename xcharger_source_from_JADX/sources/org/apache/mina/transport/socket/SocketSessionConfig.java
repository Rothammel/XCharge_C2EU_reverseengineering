package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface SocketSessionConfig extends IoSessionConfig {
    int getReceiveBufferSize();

    int getSendBufferSize();

    int getSoLinger();

    int getTrafficClass();

    boolean isKeepAlive();

    boolean isOobInline();

    boolean isReuseAddress();

    boolean isTcpNoDelay();

    void setKeepAlive(boolean z);

    void setOobInline(boolean z);

    void setReceiveBufferSize(int i);

    void setReuseAddress(boolean z);

    void setSendBufferSize(int i);

    void setSoLinger(int i);

    void setTcpNoDelay(boolean z);

    void setTrafficClass(int i);
}
