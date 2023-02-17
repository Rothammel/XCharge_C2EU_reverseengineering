package it.sauronsoftware.ftp4j.connectors;

import it.sauronsoftware.ftp4j.FTPConnector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class SOCKS5Connector extends FTPConnector {
    private String socks5host;
    private String socks5pass;
    private int socks5port;
    private String socks5user;

    public SOCKS5Connector(String socks5host, int socks5port, String socks5user, String socks5pass) {
        this.socks5host = socks5host;
        this.socks5port = socks5port;
        this.socks5user = socks5user;
        this.socks5pass = socks5pass;
    }

    public SOCKS5Connector(String socks5host, int socks5port) {
        this(socks5host, socks5port, null, null);
    }

    private Socket socksConnect(String host, int port, boolean forDataTransfer) throws IOException {
        Socket socket;
        boolean authentication = (this.socks5user == null || this.socks5pass == null) ? false : true;
        boolean connected = false;
        Socket socket2 = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            try {
                if (forDataTransfer) {
                    socket = tcpConnectForDataTransferChannel(this.socks5host, this.socks5port);
                } else {
                    socket = tcpConnectForCommunicationChannel(this.socks5host, this.socks5port);
                }
                in = socket2.getInputStream();
                out = socket2.getOutputStream();
                out.write(5);
                if (authentication) {
                    out.write(1);
                    out.write(2);
                } else {
                    out.write(1);
                    out.write(0);
                }
                if (read(in) != 5) {
                    throw new IOException("SOCKS5Connector: invalid proxy response");
                }
                int aux = read(in);
                if (authentication) {
                    if (aux != 2) {
                        throw new IOException("SOCKS5Connector: proxy doesn't support username/password authentication method");
                    }
                    byte[] user = this.socks5user.getBytes(CharEncoding.UTF_8);
                    byte[] pass = this.socks5pass.getBytes(CharEncoding.UTF_8);
                    int userLength = user.length;
                    int passLength = pass.length;
                    if (userLength > 255) {
                        throw new IOException("SOCKS5Connector: username too long");
                    }
                    if (passLength > 255) {
                        throw new IOException("SOCKS5Connector: password too long");
                    }
                    out.write(1);
                    out.write(userLength);
                    out.write(user);
                    out.write(passLength);
                    out.write(pass);
                    if (read(in) != 1) {
                        throw new IOException("SOCKS5Connector: invalid proxy response");
                    }
                    if (read(in) != 0) {
                        throw new IOException("SOCKS5Connector: authentication failed");
                    }
                } else if (aux != 0) {
                    throw new IOException("SOCKS5Connector: proxy requires authentication");
                }
                out.write(5);
                out.write(1);
                out.write(0);
                out.write(3);
                byte[] domain = host.getBytes(CharEncoding.UTF_8);
                if (domain.length > 255) {
                    throw new IOException("SOCKS5Connector: domain name too long");
                }
                out.write(domain.length);
                out.write(domain);
                out.write(port >> 8);
                out.write(port);
                if (read(in) != 5) {
                    throw new IOException("SOCKS5Connector: invalid proxy response");
                }
                switch (read(in)) {
                    case 0:
                        in.skip(1L);
                        int aux2 = read(in);
                        if (aux2 == 1) {
                            in.skip(4L);
                        } else if (aux2 == 3) {
                            in.skip(read(in));
                        } else if (aux2 == 4) {
                            in.skip(16L);
                        } else {
                            throw new IOException("SOCKS5Connector: invalid proxy response");
                        }
                        in.skip(2L);
                        connected = true;
                        return socket2;
                    case 1:
                        throw new IOException("SOCKS5Connector: general failure");
                    case 2:
                        throw new IOException("SOCKS5Connector: connection not allowed by ruleset");
                    case 3:
                        throw new IOException("SOCKS5Connector: network unreachable");
                    case 4:
                        throw new IOException("SOCKS5Connector: host unreachable");
                    case 5:
                        throw new IOException("SOCKS5Connector: connection refused by destination host");
                    case 6:
                        throw new IOException("SOCKS5Connector: TTL expired");
                    case 7:
                        throw new IOException("SOCKS5Connector: command not supported / protocol error");
                    case 8:
                        throw new IOException("SOCKS5Connector: address type not supported");
                    default:
                        throw new IOException("SOCKS5Connector: invalid proxy response");
                }
            } finally {
                if (!connected) {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable th2) {
                        }
                    }
                    if (socket2 != null) {
                        try {
                            socket2.close();
                        } catch (Throwable th3) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private int read(InputStream in) throws IOException {
        int aux = in.read();
        if (aux < 0) {
            throw new IOException("SOCKS5Connector: connection closed by the proxy");
        }
        return aux;
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        return socksConnect(host, port, false);
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return socksConnect(host, port, true);
    }
}
