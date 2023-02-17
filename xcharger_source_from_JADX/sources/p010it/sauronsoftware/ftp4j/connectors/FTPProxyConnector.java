package p010it.sauronsoftware.ftp4j.connectors;

import java.io.IOException;
import java.net.Socket;
import p010it.sauronsoftware.ftp4j.FTPCodes;
import p010it.sauronsoftware.ftp4j.FTPCommunicationChannel;
import p010it.sauronsoftware.ftp4j.FTPConnector;
import p010it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/* renamed from: it.sauronsoftware.ftp4j.connectors.FTPProxyConnector */
public class FTPProxyConnector extends FTPConnector {
    public static int STYLE_OPEN_COMMAND = 1;
    public static int STYLE_SITE_COMMAND = 0;
    private String proxyHost;
    private String proxyPass;
    private int proxyPort;
    private String proxyUser;
    public int style;

    public FTPProxyConnector(String proxyHost2, int proxyPort2, String proxyUser2, String proxyPass2) {
        super(true);
        this.style = STYLE_SITE_COMMAND;
        this.proxyHost = proxyHost2;
        this.proxyPort = proxyPort2;
        this.proxyUser = proxyUser2;
        this.proxyPass = proxyPass2;
    }

    public FTPProxyConnector(String proxyHost2, int proxyPort2) {
        this(proxyHost2, proxyPort2, "anonymous", "ftp4j");
    }

    public void setStyle(int style2) {
        if (style2 == STYLE_OPEN_COMMAND || style2 == STYLE_SITE_COMMAND) {
            this.style = style2;
            return;
        }
        throw new IllegalArgumentException("Invalid style");
    }

    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        boolean passwordRequired;
        Socket socket = tcpConnectForCommunicationChannel(this.proxyHost, this.proxyPort);
        FTPCommunicationChannel communication = new FTPCommunicationChannel(socket, "ASCII");
        try {
            if (communication.readFTPReply().getCode() != 220) {
                throw new IOException("Invalid proxy response");
            }
            if (this.style == STYLE_SITE_COMMAND) {
                communication.sendFTPCommand(new StringBuffer().append("USER ").append(this.proxyUser).toString());
                try {
                    switch (communication.readFTPReply().getCode()) {
                        case FTPCodes.USER_LOGGED_IN:
                            passwordRequired = false;
                            break;
                        case FTPCodes.USERNAME_OK:
                            passwordRequired = true;
                            break;
                        default:
                            throw new IOException("Proxy authentication failed");
                    }
                    if (passwordRequired) {
                        communication.sendFTPCommand(new StringBuffer().append("PASS ").append(this.proxyPass).toString());
                        try {
                            if (communication.readFTPReply().getCode() != 230) {
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

    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return tcpConnectForDataTransferChannel(host, port);
    }
}
