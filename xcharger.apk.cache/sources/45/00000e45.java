package org.apache.mina.transport.socket;

/* loaded from: classes.dex */
public class DefaultDatagramSessionConfig extends AbstractDatagramSessionConfig {
    private static final boolean DEFAULT_BROADCAST = false;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = -1;
    private static final boolean DEFAULT_REUSE_ADDRESS = false;
    private static final int DEFAULT_SEND_BUFFER_SIZE = -1;
    private static final int DEFAULT_TRAFFIC_CLASS = 0;
    private boolean broadcast = false;
    private boolean reuseAddress = false;
    private int receiveBufferSize = -1;
    private int sendBufferSize = -1;
    private int trafficClass = 0;

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public boolean isBroadcast() {
        return this.broadcast;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public boolean isReuseAddress() {
        return this.reuseAddress;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getSendBufferSize() {
        return this.sendBufferSize;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getTrafficClass() {
        return this.trafficClass;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setTrafficClass(int trafficClass) {
        this.trafficClass = trafficClass;
    }

    @Override // org.apache.mina.transport.socket.AbstractDatagramSessionConfig
    protected boolean isBroadcastChanged() {
        return this.broadcast;
    }

    @Override // org.apache.mina.transport.socket.AbstractDatagramSessionConfig
    protected boolean isReceiveBufferSizeChanged() {
        return this.receiveBufferSize != -1;
    }

    @Override // org.apache.mina.transport.socket.AbstractDatagramSessionConfig
    protected boolean isReuseAddressChanged() {
        return this.reuseAddress;
    }

    @Override // org.apache.mina.transport.socket.AbstractDatagramSessionConfig
    protected boolean isSendBufferSizeChanged() {
        return this.sendBufferSize != -1;
    }

    @Override // org.apache.mina.transport.socket.AbstractDatagramSessionConfig
    protected boolean isTrafficClassChanged() {
        return this.trafficClass != 0;
    }
}