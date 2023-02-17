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

/* loaded from: classes.dex */
public class TCPNetworkModule implements NetworkModule {
    private static final String CLASS_NAME = TCPNetworkModule.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private int conTimeout;
    private SocketFactory factory;
    private String host;
    private int port;
    protected Socket socket;

    public TCPNetworkModule(SocketFactory factory, String host, int port, String resourceContext) {
        log.setResourceName(resourceContext);
        this.factory = factory;
        this.host = host;
        this.port = port;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public void start() throws IOException, MqttException {
        try {
            log.fine(CLASS_NAME, "start", "252", new Object[]{this.host, new Integer(this.port), new Long(this.conTimeout * 1000)});
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
            log.fine(CLASS_NAME, "start", "250", null, ex);
            throw new MqttException(32103, ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public void stop() throws IOException {
        if (this.socket != null) {
            this.socket.shutdownInput();
            this.socket.close();
        }
    }

    public void setConnectTimeout(int timeout) {
        this.conTimeout = timeout;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public String getServerURI() {
        return "tcp://" + this.host + ":" + this.port;
    }
}
