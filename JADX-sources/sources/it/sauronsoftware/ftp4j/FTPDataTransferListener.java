package it.sauronsoftware.ftp4j;

/* loaded from: classes.dex */
public interface FTPDataTransferListener {
    void aborted();

    void completed();

    void failed();

    void started();

    void transferred(int i);
}
