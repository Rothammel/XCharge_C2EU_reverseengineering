package org.apache.mina.transport.socket.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.transport.socket.DatagramSessionConfig;

/* loaded from: classes.dex */
class NioDatagramSession extends NioSession {
    static final TransportMetadata METADATA = new DefaultTransportMetadata("nio", "datagram", true, false, InetSocketAddress.class, DatagramSessionConfig.class, IoBuffer.class);
    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;

    /* JADX INFO: Access modifiers changed from: package-private */
    public NioDatagramSession(IoService service, DatagramChannel channel, IoProcessor<NioSession> processor, SocketAddress remoteAddress) {
        super(processor, service, channel);
        this.config = new NioDatagramSessionConfig(channel);
        this.config.setAll(service.getSessionConfig());
        this.remoteAddress = (InetSocketAddress) remoteAddress;
        this.localAddress = (InetSocketAddress) channel.socket().getLocalSocketAddress();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public NioDatagramSession(IoService service, DatagramChannel channel, IoProcessor<NioSession> processor) {
        this(service, channel, processor, channel.socket().getRemoteSocketAddress());
    }

    @Override // org.apache.mina.core.session.AbstractIoSession, org.apache.mina.core.session.IoSession
    public DatagramSessionConfig getConfig() {
        return (DatagramSessionConfig) this.config;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.apache.mina.transport.socket.nio.NioSession
    public DatagramChannel getChannel() {
        return (DatagramChannel) this.channel;
    }

    @Override // org.apache.mina.core.session.IoSession
    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }

    @Override // org.apache.mina.core.session.IoSession
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override // org.apache.mina.core.session.IoSession
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override // org.apache.mina.core.session.AbstractIoSession, org.apache.mina.core.session.IoSession
    public InetSocketAddress getServiceAddress() {
        return (InetSocketAddress) super.getServiceAddress();
    }
}
