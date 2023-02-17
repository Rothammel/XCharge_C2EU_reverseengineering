package it.sauronsoftware.ftp4j.connectors;

import it.sauronsoftware.ftp4j.FTPConnector;
import java.io.IOException;
import java.net.Socket;

/* loaded from: classes.dex */
public class DirectConnector extends FTPConnector {
    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        return tcpConnectForCommunicationChannel(host, port);
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return tcpConnectForDataTransferChannel(host, port);
    }
}