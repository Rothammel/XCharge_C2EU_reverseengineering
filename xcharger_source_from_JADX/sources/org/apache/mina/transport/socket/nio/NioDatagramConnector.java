package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Collections;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.polling.AbstractPollingIoConnector;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.DatagramConnector;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;

public final class NioDatagramConnector extends AbstractPollingIoConnector<NioSession, DatagramChannel> implements DatagramConnector {
    public NioDatagramConnector() {
        super((IoSessionConfig) new DefaultDatagramSessionConfig(), NioProcessor.class);
    }

    public NioDatagramConnector(int processorCount) {
        super((IoSessionConfig) new DefaultDatagramSessionConfig(), NioProcessor.class, processorCount);
    }

    public NioDatagramConnector(IoProcessor<NioSession> processor) {
        super((IoSessionConfig) new DefaultDatagramSessionConfig(), processor);
    }

    public NioDatagramConnector(Class<? extends IoProcessor<NioSession>> processorClass, int processorCount) {
        super((IoSessionConfig) new DefaultDatagramSessionConfig(), processorClass, processorCount);
    }

    public NioDatagramConnector(Class<? extends IoProcessor<NioSession>> processorClass) {
        super((IoSessionConfig) new DefaultDatagramSessionConfig(), processorClass);
    }

    public TransportMetadata getTransportMetadata() {
        return NioDatagramSession.METADATA;
    }

    public DatagramSessionConfig getSessionConfig() {
        return (DatagramSessionConfig) this.sessionConfig;
    }

    public InetSocketAddress getDefaultRemoteAddress() {
        return (InetSocketAddress) super.getDefaultRemoteAddress();
    }

    public void setDefaultRemoteAddress(InetSocketAddress defaultRemoteAddress) {
        super.setDefaultRemoteAddress(defaultRemoteAddress);
    }

    /* access modifiers changed from: protected */
    public void init() throws Exception {
    }

    /* access modifiers changed from: protected */
    public DatagramChannel newHandle(SocketAddress localAddress) throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        if (localAddress != null) {
            try {
                ch.socket().bind(localAddress);
                setDefaultLocalAddress(localAddress);
            } catch (IOException ioe) {
                Exception e = new IOException("Error while binding on " + localAddress + StringUtils.f146LF + "original message : " + ioe.getMessage());
                e.initCause(ioe.getCause());
                ch.close();
                throw e;
            } catch (Exception e2) {
                ch.close();
                throw e2;
            }
        }
        return ch;
    }

    /* access modifiers changed from: protected */
    public boolean connect(DatagramChannel handle, SocketAddress remoteAddress) throws Exception {
        handle.connect(remoteAddress);
        return true;
    }

    /* access modifiers changed from: protected */
    public NioSession newSession(IoProcessor<NioSession> processor, DatagramChannel handle) {
        NioSession session = new NioDatagramSession(this, handle, processor);
        session.getConfig().setAll(getSessionConfig());
        return session;
    }

    /* access modifiers changed from: protected */
    public void close(DatagramChannel handle) throws Exception {
        handle.disconnect();
        handle.close();
    }

    /* access modifiers changed from: protected */
    public Iterator<DatagramChannel> allHandles() {
        return Collections.EMPTY_LIST.iterator();
    }

    /* access modifiers changed from: protected */
    public AbstractPollingIoConnector<NioSession, DatagramChannel>.ConnectionRequest getConnectionRequest(DatagramChannel handle) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void destroy() throws Exception {
    }

    /* access modifiers changed from: protected */
    public boolean finishConnect(DatagramChannel handle) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void register(DatagramChannel handle, AbstractPollingIoConnector<NioSession, DatagramChannel>.ConnectionRequest connectionRequest) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public int select(int timeout) throws Exception {
        return 0;
    }

    /* access modifiers changed from: protected */
    public Iterator<DatagramChannel> selectedHandles() {
        return Collections.EMPTY_LIST.iterator();
    }

    /* access modifiers changed from: protected */
    public void wakeup() {
    }
}
