package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.concurrent.Executor;
import org.apache.http.conn.ssl.TokenParser;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;

/* loaded from: classes.dex */
public abstract class AbstractIoConnector extends AbstractIoService implements IoConnector {
    private long connectTimeoutCheckInterval;
    private long connectTimeoutInMillis;
    private SocketAddress defaultLocalAddress;
    private SocketAddress defaultRemoteAddress;

    protected abstract ConnectFuture connect0(SocketAddress socketAddress, SocketAddress socketAddress2, IoSessionInitializer<? extends ConnectFuture> ioSessionInitializer);

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractIoConnector(IoSessionConfig sessionConfig, Executor executor) {
        super(sessionConfig, executor);
        this.connectTimeoutCheckInterval = 50L;
        this.connectTimeoutInMillis = 60000L;
    }

    public long getConnectTimeoutCheckInterval() {
        return this.connectTimeoutCheckInterval;
    }

    public void setConnectTimeoutCheckInterval(long minimumConnectTimeout) {
        if (getConnectTimeoutMillis() < minimumConnectTimeout) {
            this.connectTimeoutInMillis = minimumConnectTimeout;
        }
        this.connectTimeoutCheckInterval = minimumConnectTimeout;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final int getConnectTimeout() {
        return ((int) this.connectTimeoutInMillis) / 1000;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final long getConnectTimeoutMillis() {
        return this.connectTimeoutInMillis;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final void setConnectTimeout(int connectTimeout) {
        setConnectTimeoutMillis(connectTimeout * 1000);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final void setConnectTimeoutMillis(long connectTimeoutInMillis) {
        if (connectTimeoutInMillis <= this.connectTimeoutCheckInterval) {
            this.connectTimeoutCheckInterval = connectTimeoutInMillis;
        }
        this.connectTimeoutInMillis = connectTimeoutInMillis;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public SocketAddress getDefaultRemoteAddress() {
        return this.defaultRemoteAddress;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final void setDefaultLocalAddress(SocketAddress localAddress) {
        this.defaultLocalAddress = localAddress;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final SocketAddress getDefaultLocalAddress() {
        return this.defaultLocalAddress;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final void setDefaultRemoteAddress(SocketAddress defaultRemoteAddress) {
        if (defaultRemoteAddress == null) {
            throw new IllegalArgumentException("defaultRemoteAddress");
        }
        if (!getTransportMetadata().getAddressType().isAssignableFrom(defaultRemoteAddress.getClass())) {
            throw new IllegalArgumentException("defaultRemoteAddress type: " + defaultRemoteAddress.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        }
        this.defaultRemoteAddress = defaultRemoteAddress;
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final ConnectFuture connect() {
        SocketAddress defaultRemoteAddress = getDefaultRemoteAddress();
        if (defaultRemoteAddress == null) {
            throw new IllegalStateException("defaultRemoteAddress is not set.");
        }
        return connect(defaultRemoteAddress, null, null);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public ConnectFuture connect(IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        SocketAddress defaultRemoteAddress = getDefaultRemoteAddress();
        if (defaultRemoteAddress == null) {
            throw new IllegalStateException("defaultRemoteAddress is not set.");
        }
        return connect(defaultRemoteAddress, null, sessionInitializer);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final ConnectFuture connect(SocketAddress remoteAddress) {
        return connect(remoteAddress, null, null);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public ConnectFuture connect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        return connect(remoteAddress, null, sessionInitializer);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return connect(remoteAddress, localAddress, null);
    }

    @Override // org.apache.mina.core.service.IoConnector
    public final ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        if (isDisposing()) {
            throw new IllegalStateException("The connector is being disposed.");
        }
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress");
        }
        if (!getTransportMetadata().getAddressType().isAssignableFrom(remoteAddress.getClass())) {
            throw new IllegalArgumentException("remoteAddress type: " + remoteAddress.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        }
        if (localAddress != null && !getTransportMetadata().getAddressType().isAssignableFrom(localAddress.getClass())) {
            throw new IllegalArgumentException("localAddress type: " + localAddress.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        }
        if (getHandler() == null) {
            if (getSessionConfig().isUseReadOperation()) {
                setHandler(new IoHandler() { // from class: org.apache.mina.core.service.AbstractIoConnector.1
                    @Override // org.apache.mina.core.service.IoHandler
                    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void messageReceived(IoSession session, Object message) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void messageSent(IoSession session, Object message) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void sessionClosed(IoSession session) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void sessionCreated(IoSession session) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void sessionOpened(IoSession session) throws Exception {
                    }

                    @Override // org.apache.mina.core.service.IoHandler
                    public void inputClosed(IoSession session) throws Exception {
                    }
                });
            } else {
                throw new IllegalStateException("handler is not set.");
            }
        }
        return connect0(remoteAddress, localAddress, sessionInitializer);
    }

    @Override // org.apache.mina.core.service.AbstractIoService
    protected final void finishSessionInitialization0(final IoSession session, IoFuture future) {
        future.addListener(new IoFutureListener<ConnectFuture>() { // from class: org.apache.mina.core.service.AbstractIoConnector.2
            @Override // org.apache.mina.core.future.IoFutureListener
            public void operationComplete(ConnectFuture future2) {
                if (future2.isCanceled()) {
                    session.closeNow();
                }
            }
        });
    }

    public String toString() {
        TransportMetadata m = getTransportMetadata();
        return '(' + m.getProviderName() + TokenParser.SP + m.getName() + " connector: managedSessionCount: " + getManagedSessionCount() + ')';
    }
}
