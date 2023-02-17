package org.apache.mina.proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultConnectFuture;
import org.apache.mina.core.service.AbstractIoConnector;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.proxy.filter.ProxyFilter;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.session.ProxyIoSessionInitializer;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.SocketSessionConfig;

/* loaded from: classes.dex */
public class ProxyConnector extends AbstractIoConnector {
    private static final TransportMetadata METADATA = new DefaultTransportMetadata("proxy", "proxyconnector", false, true, InetSocketAddress.class, SocketSessionConfig.class, IoBuffer.class, FileRegion.class);
    private SocketConnector connector;
    private DefaultConnectFuture future;
    private final ProxyFilter proxyFilter;
    private ProxyIoSession proxyIoSession;

    public ProxyConnector() {
        super(new DefaultSocketSessionConfig(), null);
        this.connector = null;
        this.proxyFilter = new ProxyFilter();
    }

    public ProxyConnector(SocketConnector connector) {
        this(connector, new DefaultSocketSessionConfig(), null);
    }

    public ProxyConnector(SocketConnector connector, IoSessionConfig config, Executor executor) {
        super(config, executor);
        this.connector = null;
        this.proxyFilter = new ProxyFilter();
        setConnector(connector);
    }

    @Override // org.apache.mina.core.service.IoService
    public IoSessionConfig getSessionConfig() {
        return this.connector.getSessionConfig();
    }

    public ProxyIoSession getProxyIoSession() {
        return this.proxyIoSession;
    }

    public void setProxyIoSession(ProxyIoSession proxyIoSession) {
        if (proxyIoSession == null) {
            throw new IllegalArgumentException("proxySession object cannot be null");
        }
        if (proxyIoSession.getProxyAddress() == null) {
            throw new IllegalArgumentException("proxySession.proxyAddress cannot be null");
        }
        proxyIoSession.setConnector(this);
        setDefaultRemoteAddress(proxyIoSession.getProxyAddress());
        this.proxyIoSession = proxyIoSession;
    }

    @Override // org.apache.mina.core.service.AbstractIoConnector
    protected ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        if (!this.proxyIoSession.isReconnectionNeeded()) {
            IoHandler handler = getHandler();
            if (!(handler instanceof AbstractProxyIoHandler)) {
                throw new IllegalArgumentException("IoHandler must be an instance of AbstractProxyIoHandler");
            }
            this.connector.setHandler(handler);
            this.future = new DefaultConnectFuture();
        }
        ConnectFuture conFuture = this.connector.connect(this.proxyIoSession.getProxyAddress(), new ProxyIoSessionInitializer(sessionInitializer, this.proxyIoSession));
        return ((this.proxyIoSession.getRequest() instanceof SocksProxyRequest) || this.proxyIoSession.isReconnectionNeeded()) ? conFuture : this.future;
    }

    public void cancelConnectFuture() {
        this.future.cancel();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ConnectFuture fireConnected(IoSession session) {
        this.future.setSession(session);
        return this.future;
    }

    public final SocketConnector getConnector() {
        return this.connector;
    }

    private void setConnector(SocketConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("connector cannot be null");
        }
        this.connector = connector;
        String className = ProxyFilter.class.getName();
        if (connector.getFilterChain().contains(className)) {
            connector.getFilterChain().remove(className);
        }
        connector.getFilterChain().addFirst(className, this.proxyFilter);
    }

    @Override // org.apache.mina.core.service.AbstractIoService
    protected void dispose0() throws Exception {
        if (this.connector != null) {
            this.connector.dispose();
        }
    }

    @Override // org.apache.mina.core.service.IoService
    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }
}
