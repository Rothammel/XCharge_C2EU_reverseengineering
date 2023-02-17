package p010it.sauronsoftware.ftp4j.connectors;

import java.io.IOException;
import java.net.Socket;
import p010it.sauronsoftware.ftp4j.FTPConnector;

/* renamed from: it.sauronsoftware.ftp4j.connectors.DirectConnector */
public class DirectConnector extends FTPConnector {
    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        return tcpConnectForCommunicationChannel(host, port);
    }

    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return tcpConnectForDataTransferChannel(host, port);
    }
}
