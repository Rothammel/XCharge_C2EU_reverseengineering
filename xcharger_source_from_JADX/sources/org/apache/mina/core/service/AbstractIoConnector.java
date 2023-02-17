package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;

public abstract class AbstractIoConnector extends AbstractIoService implements IoConnector {
    private long connectTimeoutCheckInterval = 50;
    private long connectTimeoutInMillis = 60000;
    private SocketAddress defaultLocalAddress;
    private SocketAddress defaultRemoteAddress;

    /* access modifiers changed from: protected */
    public abstract ConnectFuture connect0(SocketAddress socketAddress, SocketAddress socketAddress2, IoSessionInitializer<? extends ConnectFuture> ioSessionInitializer);

    protected AbstractIoConnector(IoSessionConfig sessionConfig, Executor executor) {
        super(sessionConfig, executor);
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

    public final int getConnectTimeout() {
        return ((int) this.connectTimeoutInMillis) / 1000;
    }

    public final long getConnectTimeoutMillis() {
        return this.connectTimeoutInMillis;
    }

    public final void setConnectTimeout(int connectTimeout) {
        setConnectTimeoutMillis(((long) connectTimeout) * 1000);
    }

    public final void setConnectTimeoutMillis(long connectTimeoutInMillis2) {
        if (connectTimeoutInMillis2 <= this.connectTimeoutCheckInterval) {
            this.connectTimeoutCheckInterval = connectTimeoutInMillis2;
        }
        this.connectTimeoutInMillis = connectTimeoutInMillis2;
    }

    public SocketAddress getDefaultRemoteAddress() {
        return this.defaultRemoteAddress;
    }

    public final void setDefaultLocalAddress(SocketAddress localAddress) {
        this.defaultLocalAddress = localAddress;
    }

    public final SocketAddress getDefaultLocalAddress() {
        return this.defaultLocalAddress;
    }

    public final void setDefaultRemoteAddress(SocketAddress defaultRemoteAddress2) {
        if (defaultRemoteAddress2 == null) {
            throw new IllegalArgumentException("defaultRemoteAddress");
        } else if (!getTransportMetadata().getAddressType().isAssignableFrom(defaultRemoteAddress2.getClass())) {
            throw new IllegalArgumentException("defaultRemoteAddress type: " + defaultRemoteAddress2.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        } else {
            this.defaultRemoteAddress = defaultRemoteAddress2;
        }
    }

    public final ConnectFuture connect() {
        SocketAddress defaultRemoteAddress2 = getDefaultRemoteAddress();
        if (defaultRemoteAddress2 != null) {
            return connect(defaultRemoteAddress2, (SocketAddress) null, (IoSessionInitializer<? extends ConnectFuture>) null);
        }
        throw new IllegalStateException("defaultRemoteAddress is not set.");
    }

    public ConnectFuture connect(IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        SocketAddress defaultRemoteAddress2 = getDefaultRemoteAddress();
        if (defaultRemoteAddress2 != null) {
            return connect(defaultRemoteAddress2, (SocketAddress) null, sessionInitializer);
        }
        throw new IllegalStateException("defaultRemoteAddress is not set.");
    }

    public final ConnectFuture connect(SocketAddress remoteAddress) {
        return connect(remoteAddress, (SocketAddress) null, (IoSessionInitializer<? extends ConnectFuture>) null);
    }

    public ConnectFuture connect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        return connect(remoteAddress, (SocketAddress) null, sessionInitializer);
    }

    public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return connect(remoteAddress, localAddress, (IoSessionInitializer<? extends ConnectFuture>) null);
    }

    public final ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
        if (isDisposing()) {
            throw new IllegalStateException("The connector is being disposed.");
        } else if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress");
        } else if (!getTransportMetadata().getAddressType().isAssignableFrom(remoteAddress.getClass())) {
            throw new IllegalArgumentException("remoteAddress type: " + remoteAddress.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        } else if (localAddress == null || getTransportMetadata().getAddressType().isAssignableFrom(localAddress.getClass())) {
            if (getHandler() == null) {
                if (getSessionConfig().isUseReadOperation()) {
                    setHandler(new IoHandler() {
                        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
                        }

                        public void messageReceived(IoSession session, Object message) throws Exception {
                        }

                        public void messageSent(IoSession session, Object message) throws Exception {
                        }

                        public void sessionClosed(IoSession session) throws Exception {
                        }

                        public void sessionCreated(IoSession session) throws Exception {
                        }

                        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
                        }

                        public void sessionOpened(IoSession session) throws Exception {
                        }

                        public void inputClosed(IoSession session) throws Exception {
                        }
                    });
                } else {
                    throw new IllegalStateException("handler is not set.");
                }
            }
            return connect0(remoteAddress, localAddress, sessionInitializer);
        } else {
            throw new IllegalArgumentException("localAddress type: " + localAddress.getClass() + " (expected: " + getTransportMetadata().getAddressType() + ")");
        }
    }

    /* access modifiers changed from: protected */
    public final void finishSessionInitialization0(final IoSession session, IoFuture future) {
        future.addListener(new IoFutureListener<ConnectFuture>() {
            public void operationComplete(ConnectFuture future) {
                if (future.isCanceled()) {
                    session.closeNow();
                }
            }
        });
    }

    public String toString() {
        TransportMetadata m = getTransportMetadata();
        return '(' + m.getProviderName() + TokenParser.f168SP + m.getName() + " connector: " + "managedSessionCount: " + getManagedSessionCount() + ')';
    }
}
