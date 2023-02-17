package org.apache.mina.transport.socket;

import org.apache.mina.core.service.IoService;

/* loaded from: classes.dex */
public class DefaultSocketSessionConfig extends AbstractSocketSessionConfig {
    private static final boolean DEFAULT_KEEP_ALIVE = false;
    private static final boolean DEFAULT_OOB_INLINE = false;
    private static final boolean DEFAULT_REUSE_ADDRESS = false;
    private static final int DEFAULT_SO_LINGER = -1;
    private static final boolean DEFAULT_TCP_NO_DELAY = false;
    private static final int DEFAULT_TRAFFIC_CLASS = 0;
    private boolean defaultReuseAddress;
    protected IoService parent;
    private boolean reuseAddress;
    private int receiveBufferSize = -1;
    private int sendBufferSize = -1;
    private int trafficClass = 0;
    private boolean keepAlive = false;
    private boolean oobInline = false;
    private int soLinger = -1;
    private boolean tcpNoDelay = false;

    public void init(IoService parent) {
        this.parent = parent;
        if (parent instanceof SocketAcceptor) {
            this.defaultReuseAddress = true;
        } else {
            this.defaultReuseAddress = false;
        }
        this.reuseAddress = this.defaultReuseAddress;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public boolean isReuseAddress() {
        return this.reuseAddress;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public int getSendBufferSize() {
        return this.sendBufferSize;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public int getTrafficClass() {
        return this.trafficClass;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setTrafficClass(int trafficClass) {
        this.trafficClass = trafficClass;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public boolean isOobInline() {
        return this.oobInline;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setOobInline(boolean oobInline) {
        this.oobInline = oobInline;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public int getSoLinger() {
        return this.soLinger;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setSoLinger(int soLinger) {
        this.soLinger = soLinger;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    @Override // org.apache.mina.transport.socket.SocketSessionConfig
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isKeepAliveChanged() {
        return this.keepAlive;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isOobInlineChanged() {
        return this.oobInline;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isReceiveBufferSizeChanged() {
        return this.receiveBufferSize != -1;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isReuseAddressChanged() {
        return this.reuseAddress != this.defaultReuseAddress;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isSendBufferSizeChanged() {
        return this.sendBufferSize != -1;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isSoLingerChanged() {
        return this.soLinger != -1;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isTcpNoDelayChanged() {
        return this.tcpNoDelay;
    }

    @Override // org.apache.mina.transport.socket.AbstractSocketSessionConfig
    protected boolean isTrafficClassChanged() {
        return this.trafficClass != 0;
    }
}
