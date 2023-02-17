package org.apache.mina.transport.socket;

public class DefaultDatagramSessionConfig extends AbstractDatagramSessionConfig {
    private static final boolean DEFAULT_BROADCAST = false;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = -1;
    private static final boolean DEFAULT_REUSE_ADDRESS = false;
    private static final int DEFAULT_SEND_BUFFER_SIZE = -1;
    private static final int DEFAULT_TRAFFIC_CLASS = 0;
    private boolean broadcast = false;
    private int receiveBufferSize = -1;
    private boolean reuseAddress = false;
    private int sendBufferSize = -1;
    private int trafficClass = 0;

    public boolean isBroadcast() {
        return this.broadcast;
    }

    public void setBroadcast(boolean broadcast2) {
        this.broadcast = broadcast2;
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

    /* access modifiers changed from: protected */
    public boolean isBroadcastChanged() {
        return this.broadcast;
    }

    /* access modifiers changed from: protected */
    public boolean isReceiveBufferSizeChanged() {
        return this.receiveBufferSize != -1;
    }

    /* access modifiers changed from: protected */
    public boolean isReuseAddressChanged() {
        return this.reuseAddress;
    }

    /* access modifiers changed from: protected */
    public boolean isSendBufferSizeChanged() {
        return this.sendBufferSize != -1;
    }

    /* access modifiers changed from: protected */
    public boolean isTrafficClassChanged() {
        return this.trafficClass != 0;
    }
}
