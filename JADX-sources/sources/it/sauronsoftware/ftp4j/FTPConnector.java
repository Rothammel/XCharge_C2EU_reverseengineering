package it.sauronsoftware.ftp4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/* loaded from: classes.dex */
public abstract class FTPConnector {
    protected int closeTimeout;
    private Socket connectingCommunicationChannelSocket;
    protected int connectionTimeout;
    protected int readTimeout;
    private boolean useSuggestedAddressForDataConnections;

    public abstract Socket connectForCommunicationChannel(String str, int i) throws IOException;

    public abstract Socket connectForDataTransferChannel(String str, int i) throws IOException;

    /* JADX INFO: Access modifiers changed from: protected */
    public FTPConnector(boolean useSuggestedAddressForDataConnectionsDefValue) {
        this.connectionTimeout = 10;
        this.readTimeout = 10;
        this.closeTimeout = 10;
        String sysprop = System.getProperty(FTPKeys.PASSIVE_DT_USE_SUGGESTED_ADDRESS);
        if ("true".equalsIgnoreCase(sysprop) || "yes".equalsIgnoreCase(sysprop) || "1".equals(sysprop)) {
            this.useSuggestedAddressForDataConnections = true;
        } else if ("false".equalsIgnoreCase(sysprop) || "no".equalsIgnoreCase(sysprop) || "0".equals(sysprop)) {
            this.useSuggestedAddressForDataConnections = false;
        } else {
            this.useSuggestedAddressForDataConnections = useSuggestedAddressForDataConnectionsDefValue;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public FTPConnector() {
        this(false);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setCloseTimeout(int closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public void setUseSuggestedAddressForDataConnections(boolean value) {
        this.useSuggestedAddressForDataConnections = value;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean getUseSuggestedAddressForDataConnections() {
        return this.useSuggestedAddressForDataConnections;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Socket tcpConnectForCommunicationChannel(String host, int port) throws IOException {
        try {
            this.connectingCommunicationChannelSocket = new Socket();
            this.connectingCommunicationChannelSocket.setKeepAlive(true);
            this.connectingCommunicationChannelSocket.setSoTimeout(this.readTimeout * 1000);
            this.connectingCommunicationChannelSocket.setSoLinger(true, this.closeTimeout);
            this.connectingCommunicationChannelSocket.connect(new InetSocketAddress(host, port), this.connectionTimeout * 1000);
            return this.connectingCommunicationChannelSocket;
        } finally {
            this.connectingCommunicationChannelSocket = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Socket tcpConnectForDataTransferChannel(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(this.readTimeout * 1000);
        socket.setSoLinger(true, this.closeTimeout);
        socket.setReceiveBufferSize(524288);
        socket.setSendBufferSize(524288);
        socket.connect(new InetSocketAddress(host, port), this.connectionTimeout * 1000);
        return socket;
    }

    public void abortConnectForCommunicationChannel() {
        if (this.connectingCommunicationChannelSocket != null) {
            try {
                this.connectingCommunicationChannelSocket.close();
            } catch (Throwable th) {
            }
        }
    }
}
