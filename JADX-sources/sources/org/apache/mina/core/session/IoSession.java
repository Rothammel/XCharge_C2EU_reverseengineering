package org.apache.mina.core.session;

import java.net.SocketAddress;
import java.util.Set;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

/* loaded from: classes.dex */
public interface IoSession {
    @Deprecated
    CloseFuture close();

    CloseFuture close(boolean z);

    CloseFuture closeNow();

    CloseFuture closeOnFlush();

    boolean containsAttribute(Object obj);

    @Deprecated
    Object getAttachment();

    Object getAttribute(Object obj);

    Object getAttribute(Object obj, Object obj2);

    Set<Object> getAttributeKeys();

    int getBothIdleCount();

    CloseFuture getCloseFuture();

    IoSessionConfig getConfig();

    long getCreationTime();

    Object getCurrentWriteMessage();

    WriteRequest getCurrentWriteRequest();

    IoFilterChain getFilterChain();

    IoHandler getHandler();

    long getId();

    int getIdleCount(IdleStatus idleStatus);

    long getLastBothIdleTime();

    long getLastIdleTime(IdleStatus idleStatus);

    long getLastIoTime();

    long getLastReadTime();

    long getLastReaderIdleTime();

    long getLastWriteTime();

    long getLastWriterIdleTime();

    SocketAddress getLocalAddress();

    long getReadBytes();

    double getReadBytesThroughput();

    long getReadMessages();

    double getReadMessagesThroughput();

    int getReaderIdleCount();

    SocketAddress getRemoteAddress();

    long getScheduledWriteBytes();

    int getScheduledWriteMessages();

    IoService getService();

    SocketAddress getServiceAddress();

    TransportMetadata getTransportMetadata();

    WriteRequestQueue getWriteRequestQueue();

    int getWriterIdleCount();

    long getWrittenBytes();

    double getWrittenBytesThroughput();

    long getWrittenMessages();

    double getWrittenMessagesThroughput();

    boolean isActive();

    boolean isBothIdle();

    boolean isClosing();

    boolean isConnected();

    boolean isIdle(IdleStatus idleStatus);

    boolean isReadSuspended();

    boolean isReaderIdle();

    boolean isSecured();

    boolean isWriteSuspended();

    boolean isWriterIdle();

    ReadFuture read();

    Object removeAttribute(Object obj);

    boolean removeAttribute(Object obj, Object obj2);

    boolean replaceAttribute(Object obj, Object obj2, Object obj3);

    void resumeRead();

    void resumeWrite();

    @Deprecated
    Object setAttachment(Object obj);

    Object setAttribute(Object obj);

    Object setAttribute(Object obj, Object obj2);

    Object setAttributeIfAbsent(Object obj);

    Object setAttributeIfAbsent(Object obj, Object obj2);

    void setCurrentWriteRequest(WriteRequest writeRequest);

    void suspendRead();

    void suspendWrite();

    void updateThroughput(long j, boolean z);

    WriteFuture write(Object obj);

    WriteFuture write(Object obj, SocketAddress socketAddress);
}
