package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.polling.AbstractPollingIoAcceptor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketAcceptor;

/* loaded from: classes.dex */
public final class NioSocketAcceptor extends AbstractPollingIoAcceptor<NioSession, ServerSocketChannel> implements SocketAcceptor {
    private volatile Selector selector;
    private volatile SelectorProvider selectorProvider;

    public NioSocketAcceptor() {
        super(new DefaultSocketSessionConfig(), NioProcessor.class);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(int processorCount) {
        super(new DefaultSocketSessionConfig(), NioProcessor.class, processorCount);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(IoProcessor<NioSession> processor) {
        super(new DefaultSocketSessionConfig(), processor);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(Executor executor, IoProcessor<NioSession> processor) {
        super(new DefaultSocketSessionConfig(), executor, processor);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(int processorCount, SelectorProvider selectorProvider) {
        super(new DefaultSocketSessionConfig(), NioProcessor.class, processorCount, selectorProvider);
        this.selectorProvider = null;
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
        this.selectorProvider = selectorProvider;
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected void init() throws Exception {
        this.selector = Selector.open();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected void init(SelectorProvider selectorProvider) throws Exception {
        this.selectorProvider = selectorProvider;
        if (selectorProvider == null) {
            this.selector = Selector.open();
        } else {
            this.selector = selectorProvider.openSelector();
        }
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected void destroy() throws Exception {
        if (this.selector != null) {
            this.selector.close();
        }
    }

    @Override // org.apache.mina.core.service.IoService
    public TransportMetadata getTransportMetadata() {
        return NioSocketSession.METADATA;
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) super.getLocalAddress();
    }

    @Override // org.apache.mina.core.service.AbstractIoAcceptor, org.apache.mina.core.service.IoAcceptor
    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress) super.getDefaultLocalAddress();
    }

    @Override // org.apache.mina.transport.socket.SocketAcceptor
    public void setDefaultLocalAddress(InetSocketAddress localAddress) {
        setDefaultLocalAddress((SocketAddress) localAddress);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    public NioSession accept(IoProcessor<NioSession> processor, ServerSocketChannel handle) throws Exception {
        SocketChannel ch;
        SelectionKey key = null;
        if (handle != null) {
            key = handle.keyFor(this.selector);
        }
        if (key == null || !key.isValid() || !key.isAcceptable() || (ch = handle.accept()) == null) {
            return null;
        }
        return new NioSocketSession(this, processor, ch);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    public ServerSocketChannel open(SocketAddress localAddress) throws Exception {
        ServerSocketChannel channel;
        if (this.selectorProvider != null) {
            channel = this.selectorProvider.openServerSocketChannel();
        } else {
            channel = ServerSocketChannel.open();
        }
        boolean success = false;
        try {
            channel.configureBlocking(false);
            ServerSocket socket = channel.socket();
            socket.setReuseAddress(isReuseAddress());
            try {
                socket.bind(localAddress, getBacklog());
                channel.register(this.selector, 16);
                success = true;
                return channel;
            } catch (IOException ioe) {
                String newMessage = "Error while binding on " + localAddress + StringUtils.LF + "original message : " + ioe.getMessage();
                Exception e = new IOException(newMessage);
                e.initCause(ioe.getCause());
                channel.close();
                throw e;
            }
        } finally {
            if (!success) {
                close(channel);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    public SocketAddress localAddress(ServerSocketChannel handle) throws Exception {
        return handle.socket().getLocalSocketAddress();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected int select() throws Exception {
        return this.selector.select();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected Iterator<ServerSocketChannel> selectedHandles() {
        return new ServerSocketChannelIterator(this.selector.selectedKeys());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    public void close(ServerSocketChannel handle) throws Exception {
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.close();
    }

    @Override // org.apache.mina.core.polling.AbstractPollingIoAcceptor
    protected void wakeup() {
        this.selector.wakeup();
    }

    /* loaded from: classes.dex */
    private static class ServerSocketChannelIterator implements Iterator<ServerSocketChannel> {
        private final Iterator<SelectionKey> iterator;

        private ServerSocketChannelIterator(Collection<SelectionKey> selectedKeys) {
            this.iterator = selectedKeys.iterator();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override // java.util.Iterator
        public ServerSocketChannel next() {
            SelectionKey key = this.iterator.next();
            if (key.isValid() && key.isAcceptable()) {
                return (ServerSocketChannel) key.channel();
            }
            return null;
        }

        @Override // java.util.Iterator
        public void remove() {
            this.iterator.remove();
        }
    }
}