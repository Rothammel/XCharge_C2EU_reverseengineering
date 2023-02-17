package p010it.sauronsoftware.ftp4j;

import java.net.Socket;

/* renamed from: it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider */
interface FTPDataTransferConnectionProvider {
    void dispose();

    Socket openDataTransferConnection() throws FTPDataTransferException;
}
