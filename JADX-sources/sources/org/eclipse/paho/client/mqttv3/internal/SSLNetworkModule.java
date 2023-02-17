package org.eclipse.paho.client.mqttv3.internal;

import java.io.IOException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class SSLNetworkModule extends TCPNetworkModule {
    private static final String CLASS_NAME = SSLNetworkModule.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private String[] enabledCiphers;
    private int handshakeTimeoutSecs;
    private String host;
    private HostnameVerifier hostnameVerifier;
    private int port;

    public SSLNetworkModule(SSLSocketFactory factory, String host, int port, String resourceContext) {
        super(factory, host, port, resourceContext);
        this.host = host;
        this.port = port;
        log.setResourceName(resourceContext);
    }

    public String[] getEnabledCiphers() {
        return this.enabledCiphers;
    }

    public void setEnabledCiphers(String[] enabledCiphers) {
        this.enabledCiphers = enabledCiphers;
        if (this.socket != null && enabledCiphers != null) {
            if (log.isLoggable(5)) {
                String ciphers = "";
                for (int i = 0; i < enabledCiphers.length; i++) {
                    if (i > 0) {
                        ciphers = String.valueOf(ciphers) + ",";
                    }
                    ciphers = String.valueOf(ciphers) + enabledCiphers[i];
                }
                log.fine(CLASS_NAME, "setEnabledCiphers", "260", new Object[]{ciphers});
            }
            ((SSLSocket) this.socket).setEnabledCipherSuites(enabledCiphers);
        }
    }

    public void setSSLhandshakeTimeout(int timeout) {
        super.setConnectTimeout(timeout);
        this.handshakeTimeoutSecs = timeout;
    }

    public HostnameVerifier getSSLHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.TCPNetworkModule, org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public void start() throws IOException, MqttException {
        super.start();
        setEnabledCiphers(this.enabledCiphers);
        int soTimeout = this.socket.getSoTimeout();
        this.socket.setSoTimeout(this.handshakeTimeoutSecs * 1000);
        ((SSLSocket) this.socket).startHandshake();
        if (this.hostnameVerifier != null) {
            SSLSession session = ((SSLSocket) this.socket).getSession();
            this.hostnameVerifier.verify(this.host, session);
        }
        this.socket.setSoTimeout(soTimeout);
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.TCPNetworkModule, org.eclipse.paho.client.mqttv3.internal.NetworkModule
    public String getServerURI() {
        return "ssl://" + this.host + ":" + this.port;
    }
}
