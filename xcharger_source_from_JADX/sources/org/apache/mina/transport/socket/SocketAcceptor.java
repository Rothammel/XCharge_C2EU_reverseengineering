package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;

public interface SocketAcceptor extends IoAcceptor {
    int getBacklog();

    InetSocketAddress getDefaultLocalAddress();

    InetSocketAddress getLocalAddress();

    SocketSessionConfig getSessionConfig();

    boolean isReuseAddress();

    void setBacklog(int i);

    void setDefaultLocalAddress(InetSocketAddress inetSocketAddress);

    void setReuseAddress(boolean z);
}
