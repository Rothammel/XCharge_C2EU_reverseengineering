package p010it.sauronsoftware.ftp4j;

/* renamed from: it.sauronsoftware.ftp4j.FTPDataTransferListener */
public interface FTPDataTransferListener {
    void aborted();

    void completed();

    void failed();

    void started();

    void transferred(int i);
}
