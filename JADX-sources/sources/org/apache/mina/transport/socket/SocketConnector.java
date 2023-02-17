package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoConnector;

/* loaded from: classes.dex */
public interface SocketConnector extends IoConnector {
    @Override // org.apache.mina.core.service.IoConnector
    InetSocketAddress getDefaultRemoteAddress();

    @Override // org.apache.mina.core.service.IoService
    SocketSessionConfig getSessionConfig();

    void setDefaultRemoteAddress(InetSocketAddress inetSocketAddress);
}
