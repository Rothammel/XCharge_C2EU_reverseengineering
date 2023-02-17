package org.eclipse.paho.client.mqttv3.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class TCPNetworkModule implements NetworkModule {
    private static final String CLASS_NAME = TCPNetworkModule.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private int conTimeout;
    private SocketFactory factory;
    private String host;
    private int port;
    protected Socket socket;

    public TCPNetworkModule(SocketFactory factory2, String host2, int port2, String resourceContext) {
        log.setResourceName(resourceContext);
        this.factory = factory2;
        this.host = host2;
        this.port = port2;
    }

    public void start() throws IOException, MqttException {
        try {
            log.fine(CLASS_NAME, "start", "252", new Object[]{this.host, new Integer(this.port), new Long((long) (this.conTimeout * 1000))});
            SocketAddress sockaddr = new InetSocketAddress(this.host, this.port);
            if (this.factory instanceof SSLSocketFactory) {
                Socket tempsocket = new Socket();
                tempsocket.connect(sockaddr, this.conTimeout * 1000);
                this.socket = ((SSLSocketFactory) this.factory).createSocket(tempsocket, this.host, this.port, true);
                return;
            }
            this.socket = this.factory.createSocket();
            this.socket.connect(sockaddr, this.conTimeout * 1000);
        } catch (ConnectException ex) {
            log.fine(CLASS_NAME, "start", "250", (Object[]) null, ex);
            throw new MqttException(32103, ex);
        }
    }

    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    public void stop() throws IOException {
        if (this.socket != null) {
            this.socket.shutdownInput();
            this.socket.close();
        }
    }

    public void setConnectTimeout(int timeout) {
        this.conTimeout = timeout;
    }

    public String getServerURI() {
        return "tcp://" + this.host + ":" + this.port;
    }
}
