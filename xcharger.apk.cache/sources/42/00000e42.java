package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSessionRecycler;

/* loaded from: classes.dex */
public interface DatagramAcceptor extends IoAcceptor {
    @Override // org.apache.mina.core.service.IoAcceptor
    InetSocketAddress getDefaultLocalAddress();

    @Override // org.apache.mina.core.service.IoAcceptor
    InetSocketAddress getLocalAddress();

    @Override // org.apache.mina.core.service.IoService
    DatagramSessionConfig getSessionConfig();

    IoSessionRecycler getSessionRecycler();

    void setDefaultLocalAddress(InetSocketAddress inetSocketAddress);

    void setSessionRecycler(IoSessionRecycler ioSessionRecycler);
}