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
import org.apache.mina.transport.socket.DatagramConnector;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;

/* loaded from: classes.dex */
public final class NioDatagramConnector extends AbstractPollingIoConnector<NioSession, DatagramChannel> implements DatagramConnector {
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected /* bridge */ /* synthetic */ void register(DatagramChannel datagramChannel, AbstractPollingIoConnector.ConnectionRequest connectionRequest) throws Exception {
        register2(datagramChannel, (AbstractPollingIoConnector<NioSession, DatagramChannel>.ConnectionRequest) connectionRequest);
    }

    public NioDatagramConnector() {
        super(new DefaultDatagramSessionConfig(), NioProcessor.class);
    }

    public NioDatagramConnector(int processorCount) {
        super(new DefaultDatagramSessionConfig(), NioProcessor.class, processorCount);
    }

    public NioDatagramConnector(IoProcessor<NioSession> processor) {
        super(new DefaultDatagramSessionConfig(), processor);
    }

    public NioDatagramConnector(Class<? extends IoProcessor<NioSession>> processorClass, int processorCount) {
        super(new DefaultDatagramSessionConfig(), processorClass, processorCount);
    }

    public NioDatagramConnector(Class<? extends IoProcessor<NioSession>> processorClass) {
        super(new DefaultDatagramSessionConfig(), processorClass);
    }

    @Override // org.apache.mina.core.service.IoService
    public TransportMetadata getTransportMetadata() {
        return NioDatagramSession.METADATA;
    }

    @Override // org.apache.mina.core.service.IoService
    public DatagramSessionConfig getSessionConfig() {
        return (DatagramSessionConfig) this.sessionConfig;
    }

    @Override // org.apache.mina.core.service.AbstractIoConnector, org.apache.mina.core.service.IoConnector
    public InetSocketAddress getDefaultRemoteAddress() {
        return (InetSocketAddress) super.getDefaultRemoteAddress();
    }

    @Override // org.apache.mina.transport.socket.DatagramConnector
    public void setDefaultRemoteAddress(InetSocketAddress defaultRemoteAddress) {
        super.setDefaultRemoteAddress((SocketAddress) defaultRemoteAddress);
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected void init() throws Exception {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public DatagramChannel newHandle(SocketAddress localAddress) throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        try {
            if (localAddress != null) {
                try {
                    ch.socket().bind(localAddress);
                    setDefaultLocalAddress(localAddress);
                } catch (IOException ioe) {
                    String newMessage = "Error while binding on " + localAddress + StringUtils.LF + "original message : " + ioe.getMessage();
                    Exception e = new IOException(newMessage);
                    e.initCause(ioe.getCause());
                    ch.close();
                    throw e;
                }
            }
            return ch;
        } catch (Exception e2) {
            ch.close();
            throw e2;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public boolean connect(DatagramChannel handle, SocketAddress remoteAddress) throws Exception {
        handle.connect(remoteAddress);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public NioSession newSession(IoProcessor<NioSession> processor, DatagramChannel handle) {
        NioSession session = new NioDatagramSession(this, handle, processor);
        session.getConfig().setAll(getSessionConfig());
        return session;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public void close(DatagramChannel handle) throws Exception {
        handle.disconnect();
        handle.close();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected Iterator<DatagramChannel> allHandles() {
        return Collections.EMPTY_LIST.iterator();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public AbstractPollingIoConnector<NioSession, DatagramChannel>.ConnectionRequest getConnectionRequest(DatagramChannel handle) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected void destroy() throws Exception {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    public boolean finishConnect(DatagramChannel handle) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* renamed from: register  reason: avoid collision after fix types in other method */
    protected void register2(DatagramChannel handle, AbstractPollingIoConnector<NioSession, DatagramChannel>.ConnectionRequest request) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected int select(int timeout) throws Exception {
        return 0;
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected Iterator<DatagramChannel> selectedHandles() {
        return Collections.EMPTY_LIST.iterator();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoConnector
    protected void wakeup() {
    }
}
