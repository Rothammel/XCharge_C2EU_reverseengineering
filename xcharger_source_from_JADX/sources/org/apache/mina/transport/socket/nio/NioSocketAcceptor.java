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
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketAcceptor;

public final class NioSocketAcceptor extends AbstractPollingIoAcceptor<NioSession, ServerSocketChannel> implements SocketAcceptor {
    private volatile Selector selector;
    private volatile SelectorProvider selectorProvider = null;

    public NioSocketAcceptor() {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), NioProcessor.class);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(int processorCount) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), NioProcessor.class, processorCount);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(IoProcessor<NioSession> processor) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(Executor executor, IoProcessor<NioSession> processor) {
        super((IoSessionConfig) new DefaultSocketSessionConfig(), executor, processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public NioSocketAcceptor(int processorCount, SelectorProvider selectorProvider2) {
        super(new DefaultSocketSessionConfig(), NioProcessor.class, processorCount, selectorProvider2);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
        this.selectorProvider = selectorProvider2;
    }

    /* access modifiers changed from: protected */
    public void init() throws Exception {
        this.selector = Selector.open();
    }

    /* access modifiers changed from: protected */
    public void init(SelectorProvider selectorProvider2) throws Exception {
        this.selectorProvider = selectorProvider2;
        if (selectorProvider2 == null) {
            this.selector = Selector.open();
        } else {
            this.selector = selectorProvider2.openSelector();
        }
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

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) super.getLocalAddress();
    }

    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress) super.getDefaultLocalAddress();
    }

    public void setDefaultLocalAddress(InetSocketAddress localAddress) {
        setDefaultLocalAddress(localAddress);
    }

    /* access modifiers changed from: protected */
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

    /* access modifiers changed from: protected */
    public ServerSocketChannel open(SocketAddress localAddress) throws Exception {
        ServerSocketChannel channel;
        if (this.selectorProvider != null) {
            channel = this.selectorProvider.openServerSocketChannel();
        } else {
            channel = ServerSocketChannel.open();
        }
        try {
            channel.configureBlocking(false);
            ServerSocket socket = channel.socket();
            socket.setReuseAddress(isReuseAddress());
            socket.bind(localAddress, getBacklog());
            channel.register(this.selector, 16);
            if (1 == 0) {
                close(channel);
            }
            return channel;
        } catch (IOException ioe) {
            Exception e = new IOException("Error while binding on " + localAddress + StringUtils.f146LF + "original message : " + ioe.getMessage());
            e.initCause(ioe.getCause());
            channel.close();
            throw e;
        } catch (Throwable th) {
            if (0 == 0) {
                close(channel);
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public SocketAddress localAddress(ServerSocketChannel handle) throws Exception {
        return handle.socket().getLocalSocketAddress();
    }

    /* access modifiers changed from: protected */
    public int select() throws Exception {
        return this.selector.select();
    }

    /* access modifiers changed from: protected */
    public Iterator<ServerSocketChannel> selectedHandles() {
        return new ServerSocketChannelIterator(this.selector.selectedKeys());
    }

    /* access modifiers changed from: protected */
    public void close(ServerSocketChannel handle) throws Exception {
        SelectionKey key = handle.keyFor(this.selector);
        if (key != null) {
            key.cancel();
        }
        handle.close();
    }

    /* access modifiers changed from: protected */
    public void wakeup() {
        this.selector.wakeup();
    }

    private static class ServerSocketChannelIterator implements Iterator<ServerSocketChannel> {
        private final Iterator<SelectionKey> iterator;

        private ServerSocketChannelIterator(Collection<SelectionKey> selectedKeys) {
            this.iterator = selectedKeys.iterator();
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public ServerSocketChannel next() {
            SelectionKey key = this.iterator.next();
            if (!key.isValid() || !key.isAcceptable()) {
                return null;
            }
            return (ServerSocketChannel) key.channel();
        }

        public void remove() {
            this.iterator.remove();
        }
    }
}
