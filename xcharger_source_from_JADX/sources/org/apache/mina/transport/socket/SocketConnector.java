package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoConnector;

public interface SocketConnector extends IoConnector {
    InetSocketAddress getDefaultRemoteAddress();

    SocketSessionConfig getSessionConfig();

    void setDefaultRemoteAddress(InetSocketAddress inetSocketAddress);
}
