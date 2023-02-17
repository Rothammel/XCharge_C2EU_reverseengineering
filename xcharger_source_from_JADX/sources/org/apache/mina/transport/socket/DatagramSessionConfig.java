package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface DatagramSessionConfig extends IoSessionConfig {
    int getReceiveBufferSize();

    int getSendBufferSize();

    int getTrafficClass();

    boolean isBroadcast();

    boolean isCloseOnPortUnreachable();

    boolean isReuseAddress();

    void setBroadcast(boolean z);

    void setCloseOnPortUnreachable(boolean z);

    void setReceiveBufferSize(int i);

    void setReuseAddress(boolean z);

    void setSendBufferSize(int i);

    void setTrafficClass(int i);
}
