package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;

/* loaded from: classes.dex */
public interface SocketAcceptor extends IoAcceptor {
    int getBacklog();

    @Override // org.apache.mina.core.service.IoAcceptor
    InetSocketAddress getDefaultLocalAddress();

    @Override // org.apache.mina.core.service.IoAcceptor
    InetSocketAddress getLocalAddress();

    @Override // org.apache.mina.core.service.IoService
    SocketSessionConfig getSessionConfig();

    boolean isReuseAddress();

    void setBacklog(int i);

    void setDefaultLocalAddress(InetSocketAddress inetSocketAddress);

    void setReuseAddress(boolean z);
}