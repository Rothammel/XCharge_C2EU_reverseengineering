package it.sauronsoftware.ftp4j.connectors;

import it.sauronsoftware.ftp4j.FTPConnector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.CharEncoding;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

/* loaded from: classes.dex */
public class HTTPTunnelConnector extends FTPConnector {
    private String proxyHost;
    private String proxyPass;
    private int proxyPort;
    private String proxyUser;

    public HTTPTunnelConnector(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    public HTTPTunnelConnector(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, null);
    }

    private Socket httpConnect(String host, int port, boolean forDataTransfer) throws IOException {
        Socket socket;
        byte[] CRLF = HttpProxyConstants.CRLF.getBytes(CharEncoding.UTF_8);
        String connect = new StringBuffer().append("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.1").toString();
        String hostHeader = new StringBuffer().append("Host: ").append(host).append(":").append(port).toString();
        boolean connected = false;
        Socket socket2 = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            try {
                if (forDataTransfer) {
                    socket = tcpConnectForDataTransferChannel(this.proxyHost, this.proxyPort);
                } else {
                    socket = tcpConnectForCommunicationChannel(this.proxyHost, this.proxyPort);
                }
                in = socket2.getInputStream();
                out = socket2.getOutputStream();
                out.write(connect.getBytes(CharEncoding.UTF_8));
                out.write(CRLF);
                out.write(hostHeader.getBytes(CharEncoding.UTF_8));
                out.write(CRLF);
                if (this.proxyUser != null && this.proxyPass != null) {
                    String header = new StringBuffer().append("Proxy-Authorization: Basic ").append(Base64.encode(new StringBuffer().append(this.proxyUser).append(":").append(this.proxyPass).toString())).toString();
                    out.write(header.getBytes(CharEncoding.UTF_8));
                    out.write(CRLF);
                }
                out.write(CRLF);
                out.flush();
                ArrayList responseLines = new ArrayList();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                for (String line = reader.readLine(); line != null && line.length() > 0; line = reader.readLine()) {
                    responseLines.add(line);
                }
                int size = responseLines.size();
                if (size < 1) {
                    throw new IOException("HTTPTunnelConnector: invalid proxy response");
                }
                String response = (String) responseLines.get(0);
                if (response.startsWith("HTTP/") && response.length() >= 12) {
                    String code = response.substring(9, 12);
                    if (!"200".equals(code)) {
                        StringBuffer msg = new StringBuffer();
                        msg.append("HTTPTunnelConnector: connection failed\r\n");
                        msg.append("Response received from the proxy:\r\n");
                        for (int i = 0; i < size; i++) {
                            String line2 = (String) responseLines.get(i);
                            msg.append(line2);
                            msg.append(HttpProxyConstants.CRLF);
                        }
                        throw new IOException(msg.toString());
                    }
                    connected = true;
                    return socket2;
                }
                throw new IOException("HTTPTunnelConnector: invalid proxy response");
            } catch (IOException e) {
                throw e;
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

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForCommunicationChannel(String host, int port) throws IOException {
        return httpConnect(host, port, false);
    }

    @Override // it.sauronsoftware.ftp4j.FTPConnector
    public Socket connectForDataTransferChannel(String host, int port) throws IOException {
        return httpConnect(host, port, true);
    }
}