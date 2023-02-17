package it.sauronsoftware.ftp4j;

/* loaded from: classes.dex */
public interface FTPCommunicationListener {
    void received(String str);

    void sent(String str);
}
