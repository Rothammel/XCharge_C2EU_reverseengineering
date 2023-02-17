package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.polling.AbstractPollingIoConnector;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.SocketSessionConfig;

public final class NioSocketConnector extends AbstractPollingIoConnector<NioSession, SocketChannel> implements SocketConnector {
    private volatile Selector selector;

    public NioSocketConnector() {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), NioProcessor.class);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketConnector(int processorCount) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), NioProcessor.class, processorCount);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketConnector(IoProcessor<NioSession> processor) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketConnector(Executor executor, IoProcessor<NioSession> processor) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), executor, processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketConnector(Class<? extends IoProcessor<NioSession>> processorClass, int processorCount) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), processorClass, processorCount);
    }

    public NioSocketConnector(Class<? extends IoProcessor<NioSession>> processorClass) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), processorClass);
    }

    /* access modifiers changed from: protected */
    public void init() throws Exception {
        this.selector = Selector.open();
    }

    /* access modifiers changed from: protected */
    public void destroy() throws Exception {
        if (this.selector != null) {
            this.selector.close();
        }
    }

    public TransportMetadata getTransportMetadata() {
        return NioSocketSession.METADATA;
    }

    public SocketSessionConfig getSessionConfig() {
        return (SocketSessionConfig) this.sessionConfig;
    }

    public InetSocketAddress getDefaultRemoteAddress() {
        return (InetSocketAddress) super.getDefaultRemoteAddress();
    }

    public void setDefaultRemoteAddress(InetSocketAddress defaultRemoteAddress) {
        super.setDefaultRemoteAddress(defaultRemoteAddress);
    }

    /* access modifiers changed from: protected */
    public Iterator<SocketChannel> allHandles() {
        return new SocketChannelIterator(this.selector.keys());
    }

    /* access modifiers changed from: protected */
    public boolean connect(SocketChannel handle, SocketAddress remoteAddress) throws Exception {
        return handle.connect(remoteAddress);
    }

    /* access modifiers changed from: protected */
    public AbstractPollingIoConnector<NioSession, SocketChannel>.ConnectionRequest getConnectionRequest(SocketChannel handle) {
        SelectionKey key = handle.keyFor(this.selector);
        if (key == null || !key.isValid()) {
            return null;
        }
        return (AbstractPollingIoConnector.ConnectionRequest) key.attachment();
    }

    /* access modifiers changed from: protected */
    public void close(SocketChannel handle) throws Exception {
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.close();
    }

    /* access modifiers changed from: protected */
    public boolean finishConnect(SocketChannel handle) throws Exception {
        if (!handle.finishConnect()) {
            return false;
        }
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public SocketChannel newHandle(SocketAddress localAddress) throws Exception {
        SocketChannel ch = SocketChannel.open();
        int receiveBufferSize = getSessionConfig().getReceiveBufferSize();
        if (receiveBufferSize > 65535) {
            ch.socket().setReceiveBufferSize(receiveBufferSize);
        }
        if (localAddress != null) {
            try {
                ch.socket().bind(localAddress);
            } catch (IOException ioe) {
                Exception e = new IOException("Error while binding on " + localAddress + StringUtils.f146LF + "original message : " + ioe.getMessage());
                e.initCause(ioe.getCause());
                ch.close();
                throw e;
            }
        }
        ch.configureBlocking(false);
        return ch;
    }

    /* access modifiers changed from: protected */
    public NioSession newSession(IoProcessor<NioSession> processor, SocketChannel handle) {
        return new NioSocketSession(this, processor, handle);
    }

    /* access modifiers changed from: protected */
    public void register(SocketChannel handle, AbstractPollingIoConnector<NioSession, SocketChannel>.ConnectionRequest request) throws Exception {
        handle.register(this.selector, 8, request);
    }

    /* access modifiers changed from: protected */
    public int select(int timeout) throws Exception {
        return this.selector.select((long) timeout);
    }

    /* access modifiers changed from: protected */
    public Iterator<SocketChannel> selectedHandles() {
        return new SocketChannelIterator(this.selector.selectedKeys());
    }

    /* access modifiers changed from: protected */
    public void wakeup() {
        this.selector.wakeup();
    }

    private static class SocketChannelIterator implements Iterator<SocketChannel> {

        /* renamed from: i */
        private final Iterator<SelectionKey> f196i;

        private SocketChannelIterator(Collection<SelectionKey> selectedKeys) {
            this.f196i = selectedKeys.iterator();
        }

        public boolean hasNext() {
            return this.f196i.hasNext();
        }

        public SocketChannel next() {
            return (SocketChannel) this.f196i.next().channel();
        }

        public void remove() {
            this.f196i.remove();
        }
    }
}
