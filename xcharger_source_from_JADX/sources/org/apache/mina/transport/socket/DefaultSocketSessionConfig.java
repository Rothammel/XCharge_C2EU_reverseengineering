package org.apache.mina.transport.socket;

import org.apache.mina.core.service.IoService;

public class DefaultSocketSessionConfig extends AbstractSocketSessionConfig {
    private static final boolean DEFAULT_KEEP_ALIVE = false;
    private static final boolean DEFAULT_OOB_INLINE = false;
    private static final boolean DEFAULT_REUSE_ADDRESS = false;
    private static final int DEFAULT_SO_LINGER = -1;
    private static final boolean DEFAULT_TCP_NO_DELAY = false;
    private static final int DEFAULT_TRAFFIC_CLASS = 0;
    private boolean defaultReuseAddress;
    private boolean keepAlive = false;
    private boolean oobInline = false;
    protected IoService parent;
    private int receiveBufferSize = -1;
    private boolean reuseAddress;
    private int sendBufferSize = -1;
    private int soLinger = -1;
    private boolean tcpNoDelay = false;
    private int trafficClass = 0;

    public void init(IoService parent2) {
        this.parent = parent2;
        if (parent2 instanceof SocketAcceptor) {
            this.defaultReuseAddress = true;
        } else {
            this.defaultReuseAddress = false;
        }
        this.reuseAddress = this.defaultReuseAddress;
    }

    public boolean isReuseAddress() {
        return this.reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress2) {
        this.reuseAddress = reuseAddress2;
    }

    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize2) {
        this.receiveBufferSize = receiveBufferSize2;
    }

    public int getSendBufferSize() {
        return this.sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize2) {
        this.sendBufferSize = sendBufferSize2;
    }

    public int getTrafficClass() {
        return this.trafficClass;
    }

    public void setTrafficClass(int trafficClass2) {
        this.trafficClass = trafficClass2;
    }

    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(boolean keepAlive2) {
        this.keepAlive = keepAlive2;
    }

    public boolean isOobInline() {
        return this.oobInline;
    }

    public void setOobInline(boolean oobInline2) {
        this.oobInline = oobInline2;
    }

    public int getSoLinger() {
        return this.soLinger;
    }

    public void setSoLinger(int soLinger2) {
        this.soLinger = soLinger2;
    }

    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay2) {
        this.tcpNoDelay = tcpNoDelay2;
    }

    /* access modifiers changed from: protected */
    public boolean isKeepAliveChanged() {
        return this.keepAlive;
    }

    /* access modifiers changed from: protected */
    public boolean isOobInlineChanged() {
        return this.oobInline;
    }

    /* access modifiers changed from: protected */
    public boolean isReceiveBufferSizeChanged() {
        return this.receiveBufferSize != -1;
    }

    /* access modifiers changed from: protected */
    public boolean isReuseAddressChanged() {
        return this.reuseAddress != this.defaultReuseAddress;
    }

    /* access modifiers changed from: protected */
    public boolean isSendBufferSizeChanged() {
        return this.sendBufferSize != -1;
    }

    /* access modifiers changed from: protected */
    public boolean isSoLingerChanged() {
        return this.soLinger != -1;
    }

    /* access modifiers changed from: protected */
    public boolean isTcpNoDelayChanged() {
        return this.tcpNoDelay;
    }

    /* access modifiers changed from: protected */
    public boolean isTrafficClassChanged() {
        return this.trafficClass != 0;
    }
}
