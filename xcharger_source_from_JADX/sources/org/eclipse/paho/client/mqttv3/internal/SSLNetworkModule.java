package org.eclipse.paho.client.mqttv3.internal;

import java.io.IOException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class SSLNetworkModule extends TCPNetworkModule {
    private static final String CLASS_NAME = SSLNetworkModule.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private String[] enabledCiphers;
    private int handshakeTimeoutSecs;
    private String host;
    private HostnameVerifier hostnameVerifier;
    private int port;

    public SSLNetworkModule(SSLSocketFactory factory, String host2, int port2, String resourceContext) {
        super(factory, host2, port2, resourceContext);
        this.host = host2;
        this.port = port2;
        log.setResourceName(resourceContext);
    }

    public String[] getEnabledCiphers() {
        return this.enabledCiphers;
    }

    public void setEnabledCiphers(String[] enabledCiphers2) {
        this.enabledCiphers = enabledCiphers2;
        if (this.socket != null && enabledCiphers2 != null) {
            if (log.isLoggable(5)) {
                String ciphers = "";
                for (int i = 0; i < enabledCiphers2.length; i++) {
                    if (i > 0) {
                        ciphers = String.valueOf(ciphers) + ",";
                    }
                    ciphers = String.valueOf(ciphers) + enabledCiphers2[i];
                }
                log.fine(CLASS_NAME, "setEnabledCiphers", "260", new Object[]{ciphers});
            }
            ((SSLSocket) this.socket).setEnabledCipherSuites(enabledCiphers2);
        }
    }

    public void setSSLhandshakeTimeout(int timeout) {
        super.setConnectTimeout(timeout);
        this.handshakeTimeoutSecs = timeout;
    }

    public HostnameVerifier getSSLHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setSSLHostnameVerifier(HostnameVerifier hostnameVerifier2) {
        this.hostnameVerifier = hostnameVerifier2;
    }

    public void start() throws IOException, MqttException {
        super.start();
        setEnabledCiphers(this.enabledCiphers);
        int soTimeout = this.socket.getSoTimeout();
        this.socket.setSoTimeout(this.handshakeTimeoutSecs * 1000);
        ((SSLSocket) this.socket).startHandshake();
        if (this.hostnameVerifier != null) {
            this.hostnameVerifier.verify(this.host, ((SSLSocket) this.socket).getSession());
        }
        this.socket.setSoTimeout(soTimeout);
    }

    public String getServerURI() {
        return "ssl://" + this.host + ":" + this.port;
    }
}
