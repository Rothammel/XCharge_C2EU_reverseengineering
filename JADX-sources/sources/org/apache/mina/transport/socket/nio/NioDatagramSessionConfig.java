package org.apache.mina.transport.socket.nio;

import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;

/* loaded from: classes.dex */
class NioDatagramSessionConfig extends AbstractDatagramSessionConfig {
    private final DatagramChannel channel;

    /* JADX INFO: Access modifiers changed from: package-private */
    public NioDatagramSessionConfig(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getReceiveBufferSize() {
        try {
            return this.channel.socket().getReceiveBufferSize();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setReceiveBufferSize(int receiveBufferSize) {
        try {
            this.channel.socket().setReceiveBufferSize(receiveBufferSize);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public boolean isBroadcast() {
        try {
            return this.channel.socket().getBroadcast();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setBroadcast(boolean broadcast) {
        try {
            this.channel.socket().setBroadcast(broadcast);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getSendBufferSize() {
        try {
            return this.channel.socket().getSendBufferSize();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setSendBufferSize(int sendBufferSize) {
        try {
            this.channel.socket().setSendBufferSize(sendBufferSize);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public boolean isReuseAddress() {
        try {
            return this.channel.socket().getReuseAddress();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setReuseAddress(boolean reuseAddress) {
        try {
            this.channel.socket().setReuseAddress(reuseAddress);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public int getTrafficClass() {
        try {
            return this.channel.socket().getTrafficClass();
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setTrafficClass(int trafficClass) {
        try {
            this.channel.socket().setTrafficClass(trafficClass);
        } catch (SocketException e) {
            throw new RuntimeIoException(e);
        }
    }
}
