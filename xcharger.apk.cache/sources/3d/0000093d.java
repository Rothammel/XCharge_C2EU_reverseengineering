package it.sauronsoftware.ftp4j;

import java.net.Socket;

/* loaded from: classes.dex */
interface FTPDataTransferConnectionProvider {
    void dispose();

    Socket openDataTransferConnection() throws FTPDataTransferException;
}