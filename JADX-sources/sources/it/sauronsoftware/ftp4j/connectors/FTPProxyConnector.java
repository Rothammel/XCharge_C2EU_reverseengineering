package it.sauronsoftware.ftp4j.connectors;

import it.sauronsoftware.ftp4j.FTPCodes;
import it.sauronsoftware.ftp4j.FTPCommunicationChannel;
import it.sauronsoftware.ftp4j.FTPConnector;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPReply;
import java.io.IOException;
import java.net.Socket;

/* loaded from: classes.dex */
public class FTPProxyConnector extends FTPConnector {
    private String proxyHost;
    private String proxyPass;
    private int proxyPort;
    private String proxyUser;
    public int style;
    public static int STYLE_SITE_COMMAND = 0;
    public static int STYLE_OPEN_COMMAND = 1;

    public FTPProxyConnector(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        super(true);
        this.style = STYLE_SITE_COMMAND;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    public FTPProxyConnector(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, "anonymous", "ftp4j");
    }

    public void setStyle(int style) {
        if (style != STYLE_OPEN_COMMAND && style != STYLE_SITE_COMMAND) {
            throw new IllegalArgumentException("Invalid style");
        }
        this.style = style;
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        boolean passwordRequired;
        Socket socket = tcpConnectForCommunicationChannel(this.proxyHost, this.proxyPort);
        FTPCommunicationChannel communication = new FTPCommunicationChannel(socket, "ASCII");
        try {
            FTPReply r = communication.readFTPReply();
            if (r.getCode() != 220) {
                throw new IOException("Invalid proxy response");
            }
            if (this.style == STYLE_SITE_COMMAND) {
                communication.sendFTPCommand(new StringBuffer().append("USER ").append(this.proxyUser).toString());
                try {
                    FTPReply r2 = communication.readFTPReply();
                    switch (r2.getCode()) {
                        case FTPCodes.USER_LOGGED_IN /* 230 */:
                            passwordRequired = false;
                            break;
                        case FTPCodes.USERNAME_OK /* 331 */:
                            passwordRequired = true;
                            break;
                        default:
                            throw new IOException("Proxy authentication failed");
                    }
                    if (passwordRequired) {
                        communication.sendFTPCommand(new StringBuffer().append("PASS ").append(this.proxyPass).toString());
                        try {
                            FTPReply r3 = communication.readFTPReply();
                            if (r3.getCode() != 230) {
                                throw new IOException("Proxy authentication failed");
                            }
                        } catch (FTPIllegalReplyException e) {
                            throw new IOException("Invalid proxy response");
                        }
                    }
                    communication.sendFTPCommand(new StringBuffer().append("SITE ").append(host).append(":").append(port).toString());
                } catch (FTPIllegalReplyException e2) {
                    throw new IOException("Invalid proxy response");
                }
            } else if (this.style == STYLE_OPEN_COMMAND) {
                communication.sendFTPCommand(new StringBuffer().append("OPEN ").append(host).append(":").append(port).toString());
            }
            return socket;
        } catch (FTPIllegalReplyException e3) {
            throw new IOException("Invalid proxy response");
        }
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return tcpConnectForDataTransferChannel(host, port);
    }
}
