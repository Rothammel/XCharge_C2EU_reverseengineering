package org.apache.mina.core.service;

import java.util.Map;
import java.util.Set;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;

public interface IoService {
    void addListener(IoServiceListener ioServiceListener);

    Set<WriteFuture> broadcast(Object obj);

    void dispose();

    void dispose(boolean z);

    long getActivationTime();

    DefaultIoFilterChainBuilder getFilterChain();

    IoFilterChainBuilder getFilterChainBuilder();

    IoHandler getHandler();

    int getManagedSessionCount();

    Map<Long, IoSession> getManagedSessions();

    int getScheduledWriteBytes();

    int getScheduledWriteMessages();

    IoSessionConfig getSessionConfig();

    IoSessionDataStructureFactory getSessionDataStructureFactory();

    IoServiceStatistics getStatistics();

    TransportMetadata getTransportMetadata();

    boolean isActive();

    boolean isDisposed();

    boolean isDisposing();

    void removeListener(IoServiceListener ioServiceListener);

    void setFilterChainBuilder(IoFilterChainBuilder ioFilterChainBuilder);

    void setHandler(IoHandler ioHandler);

    void setSessionDataStructureFactory(IoSessionDataStructureFactory ioSessionDataStructureFactory);
}
