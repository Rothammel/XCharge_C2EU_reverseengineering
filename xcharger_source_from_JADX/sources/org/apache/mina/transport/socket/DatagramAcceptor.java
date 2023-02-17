package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSessionRecycler;

public interface DatagramAcceptor extends IoAcceptor {
    InetSocketAddress getDefaultLocalAddress();

    InetSocketAddress getLocalAddress();

    DatagramSessionConfig getSessionConfig();

    IoSessionRecycler getSessionRecycler();

    void setDefaultLocalAddress(InetSocketAddress inetSocketAddress);

    void setSessionRecycler(IoSessionRecycler ioSessionRecycler);
}
