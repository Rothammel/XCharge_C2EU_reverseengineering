package p010it.sauronsoftware.ftp4j.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.commons.lang3.CharEncoding;
import p010it.sauronsoftware.ftp4j.FTPConnector;

/* renamed from: it.sauronsoftware.ftp4j.connectors.SOCKS5Connector */
public class SOCKS5Connector extends FTPConnector {
    private String socks5host;
    private String socks5pass;
    private int socks5port;
    private String socks5user;

    public SOCKS5Connector(String socks5host2, int socks5port2, String socks5user2, String socks5pass2) {
        this.socks5host = socks5host2;
        this.socks5port = socks5port2;
        this.socks5user = socks5user2;
        this.socks5pass = socks5pass2;
    }

    public SOCKS5Connector(String socks5host2, int socks5port2) {
        this(socks5host2, socks5port2, (String) null, (String) null);
    }

    private Socket socksConnect(String host, int port, boolean forDataTransfer) throws IOException {
        boolean authentication = (this.socks5user == null || this.socks5pass == null) ? false : true;
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        if (forDataTransfer) {
            try {
                socket = tcpConnectForDataTransferChannel(this.socks5host, this.socks5port);
            } catch (IOException e) {
                throw e;
            } catch (Throwable th) {
                if (0 == 0) {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th2) {
                        }
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                }
                throw th;
            }
        } else {
            socket = tcpConnectForCommunicationChannel(this.socks5host, this.socks5port);
        }
        in = socket.getInputStream();
        out = socket.getOutputStream();
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
            } else if (passLength > 255) {
                throw new IOException("SOCKS5Connector: password too long");
            } else {
                out.write(1);
                out.write(userLength);
                out.write(user);
                out.write(passLength);
                out.write(pass);
                if (read(in) != 1) {
                    throw new IOException("SOCKS5Connector: invalid proxy response");
                } else if (read(in) != 0) {
                    throw new IOException("SOCKS5Connector: authentication failed");
                }
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
                in.skip(1);
                int aux2 = read(in);
                if (aux2 == 1) {
                    in.skip(4);
                } else if (aux2 == 3) {
                    in.skip((long) read(in));
                } else if (aux2 == 4) {
                    in.skip(16);
                } else {
                    throw new IOException("SOCKS5Connector: invalid proxy response");
                }
                in.skip(2);
                if (1 == 0) {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th3) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable th4) {
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Throwable th5) {
                        }
                    }
                }
                return socket;
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
        throw e;
    }

    private int read(InputStream in) throws IOException {
        int aux = in.read();
        if (aux >= 0) {
            return aux;
        }
        throw new IOException("SOCKS5Connector: connection closed by the proxy");
    }

    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        return socksConnect(host, port, false);
    }

    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return socksConnect(host, port, true);
    }
}
