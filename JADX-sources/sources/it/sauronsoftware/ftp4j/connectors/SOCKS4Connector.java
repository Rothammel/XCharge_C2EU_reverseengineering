package it.sauronsoftware.ftp4j.connectors;

import com.google.zxing.pdf417.PDF417Common;
import it.sauronsoftware.ftp4j.FTPConnector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class SOCKS4Connector extends FTPConnector {
    private String socks4host;
    private int socks4port;
    private String socks4user;

    public SOCKS4Connector(String socks4host, int socks4port, String socks4user) {
        this.socks4host = socks4host;
        this.socks4port = socks4port;
        this.socks4user = socks4user;
    }

    public SOCKS4Connector(String socks4host, int socks4port) {
        this(socks4host, socks4port, null);
    }

    private Socket socksConnect(String host, int port, boolean forDataTransfer) throws IOException {
        byte[] address;
        Socket socket;
        boolean socks4a = false;
        try {
            address = InetAddress.getByName(host).getAddress();
        } catch (Exception e) {
            socks4a = true;
            address = new byte[]{0, 0, 0, 1};
        }
        boolean connected = false;
        Socket socket2 = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            try {
                if (forDataTransfer) {
                    socket = tcpConnectForDataTransferChannel(this.socks4host, this.socks4port);
                } else {
                    socket = tcpConnectForCommunicationChannel(this.socks4host, this.socks4port);
                }
                in = socket2.getInputStream();
                out = socket2.getOutputStream();
                out.write(4);
                out.write(1);
                out.write(port >> 8);
                out.write(port);
                out.write(address);
                if (this.socks4user != null) {
                    out.write(this.socks4user.getBytes(CharEncoding.UTF_8));
                }
                out.write(0);
                if (socks4a) {
                    out.write(host.getBytes(CharEncoding.UTF_8));
                    out.write(0);
                }
                int aux = read(in);
                if (aux != 0) {
                    throw new IOException("SOCKS4Connector: invalid proxy response");
                }
                int aux2 = read(in);
                switch (aux2) {
                    case PDF417Common.MAX_ROWS_IN_BARCODE /* 90 */:
                        in.skip(6L);
                        connected = true;
                        return socket2;
                    case 91:
                        throw new IOException("SOCKS4Connector: connection refused/failed");
                    case 92:
                        throw new IOException("SOCKS4Connector: cannot validate the user");
                    case 93:
                        throw new IOException("SOCKS4Connector: invalid user");
                    default:
                        throw new IOException("SOCKS4Connector: invalid proxy response");
                }
            } catch (IOException e2) {
                throw e2;
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
    }

    private int read(InputStream in) throws IOException {
        int aux = in.read();
        if (aux < 0) {
            throw new IOException("SOCKS4Connector: connection closed by the proxy");
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
