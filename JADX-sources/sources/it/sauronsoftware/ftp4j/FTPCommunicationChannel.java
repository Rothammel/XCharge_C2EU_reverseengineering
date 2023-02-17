package it.sauronsoftware.ftp4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.SSLSocketFactory;

/* loaded from: classes.dex */
public class FTPCommunicationChannel {
    private String charsetName;
    private ArrayList communicationListeners = new ArrayList();
    private Socket connection;
    private NVTASCIIReader reader;
    private NVTASCIIWriter writer;

    public FTPCommunicationChannel(Socket connection, String charsetName) throws IOException {
        this.connection = null;
        this.charsetName = null;
        this.reader = null;
        this.writer = null;
        this.connection = connection;
        this.charsetName = charsetName;
        InputStream inStream = connection.getInputStream();
        OutputStream outStream = connection.getOutputStream();
        this.reader = new NVTASCIIReader(inStream, charsetName);
        this.writer = new NVTASCIIWriter(outStream, charsetName);
    }

    public void addCommunicationListener(FTPCommunicationListener listener) {
        this.communicationListeners.add(listener);
    }

    public void removeCommunicationListener(FTPCommunicationListener listener) {
        this.communicationListeners.remove(listener);
    }

    public void close() {
        try {
            this.connection.close();
        } catch (Exception e) {
        }
    }

    public FTPCommunicationListener[] getCommunicationListeners() {
        int size = this.communicationListeners.size();
        FTPCommunicationListener[] ret = new FTPCommunicationListener[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (FTPCommunicationListener) this.communicationListeners.get(i);
        }
        return ret;
    }

    private String read() throws IOException {
        String line = this.reader.readLine();
        if (line == null) {
            throw new IOException("FTPConnection closed");
        }
        Iterator iter = this.communicationListeners.iterator();
        while (iter.hasNext()) {
            FTPCommunicationListener l = (FTPCommunicationListener) iter.next();
            l.received(line);
        }
        return line;
    }

    public void sendFTPCommand(String command) throws IOException {
        this.writer.writeLine(command);
        Iterator iter = this.communicationListeners.iterator();
        while (iter.hasNext()) {
            FTPCommunicationListener l = (FTPCommunicationListener) iter.next();
            l.sent(command);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:31:0x0068, code lost:
        r9 = r7.size();
        r5 = new java.lang.String[r9];
        r3 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x006f, code lost:
        if (r3 >= r9) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0071, code lost:
        r5[r3] = (java.lang.String) r7.get(r3);
        r3 = r3 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x0097, code lost:
        return new it.sauronsoftware.ftp4j.FTPReply(r1, r5);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public it.sauronsoftware.ftp4j.FTPReply readFTPReply() throws java.io.IOException, it.sauronsoftware.ftp4j.FTPIllegalReplyException {
        /*
            r14 = this;
            r13 = 3
            r1 = 0
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
        L7:
            java.lang.String r10 = r14.read()
            java.lang.String r11 = r10.trim()
            int r11 = r11.length()
            if (r11 == 0) goto L7
            java.lang.String r11 = "\n"
            boolean r11 = r10.startsWith(r11)
            if (r11 == 0) goto L22
            r11 = 1
            java.lang.String r10 = r10.substring(r11)
        L22:
            int r4 = r10.length()
            if (r1 != 0) goto L30
            if (r4 >= r13) goto L30
            it.sauronsoftware.ftp4j.FTPIllegalReplyException r11 = new it.sauronsoftware.ftp4j.FTPIllegalReplyException
            r11.<init>()
            throw r11
        L30:
            r11 = 0
            r12 = 3
            java.lang.String r11 = r10.substring(r11, r12)     // Catch: java.lang.Exception -> L46
            int r0 = java.lang.Integer.parseInt(r11)     // Catch: java.lang.Exception -> L46
        L3a:
            if (r1 == 0) goto L51
            if (r0 == 0) goto L51
            if (r0 == r1) goto L51
            it.sauronsoftware.ftp4j.FTPIllegalReplyException r11 = new it.sauronsoftware.ftp4j.FTPIllegalReplyException
            r11.<init>()
            throw r11
        L46:
            r2 = move-exception
            if (r1 != 0) goto L4f
            it.sauronsoftware.ftp4j.FTPIllegalReplyException r11 = new it.sauronsoftware.ftp4j.FTPIllegalReplyException
            r11.<init>()
            throw r11
        L4f:
            r0 = 0
            goto L3a
        L51:
            if (r1 != 0) goto L54
            r1 = r0
        L54:
            if (r0 <= 0) goto L8d
            if (r4 <= r13) goto L86
            char r8 = r10.charAt(r13)
            r11 = 4
            java.lang.String r6 = r10.substring(r11, r4)
            r7.add(r6)
            r11 = 32
            if (r8 != r11) goto L7c
        L68:
            int r9 = r7.size()
            java.lang.String[] r5 = new java.lang.String[r9]
            r3 = 0
        L6f:
            if (r3 >= r9) goto L92
            java.lang.Object r11 = r7.get(r3)
            java.lang.String r11 = (java.lang.String) r11
            r5[r3] = r11
            int r3 = r3 + 1
            goto L6f
        L7c:
            r11 = 45
            if (r8 == r11) goto L7
            it.sauronsoftware.ftp4j.FTPIllegalReplyException r11 = new it.sauronsoftware.ftp4j.FTPIllegalReplyException
            r11.<init>()
            throw r11
        L86:
            if (r4 == r13) goto L68
            r7.add(r10)
            goto L7
        L8d:
            r7.add(r10)
            goto L7
        L92:
            it.sauronsoftware.ftp4j.FTPReply r11 = new it.sauronsoftware.ftp4j.FTPReply
            r11.<init>(r1, r5)
            return r11
        */
        throw new UnsupportedOperationException("Method not decompiled: it.sauronsoftware.ftp4j.FTPCommunicationChannel.readFTPReply():it.sauronsoftware.ftp4j.FTPReply");
    }

    public void changeCharset(String charsetName) throws IOException {
        this.charsetName = charsetName;
        this.reader.changeCharset(charsetName);
        this.writer.changeCharset(charsetName);
    }

    public void ssl(SSLSocketFactory sslSocketFactory) throws IOException {
        String host = this.connection.getInetAddress().getHostName();
        int port = this.connection.getPort();
        this.connection = sslSocketFactory.createSocket(this.connection, host, port, true);
        InputStream inStream = this.connection.getInputStream();
        OutputStream outStream = this.connection.getOutputStream();
        this.reader = new NVTASCIIReader(inStream, this.charsetName);
        this.writer = new NVTASCIIWriter(outStream, this.charsetName);
    }
}
