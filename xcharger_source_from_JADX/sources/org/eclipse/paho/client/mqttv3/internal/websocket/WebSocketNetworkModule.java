package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import javax.net.SocketFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.TCPNetworkModule;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class WebSocketNetworkModule extends TCPNetworkModule {
    private static final String CLASS_NAME = WebSocketNetworkModule.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private String host;
    private ByteArrayOutputStream outputStream = new ExtendedByteArrayOutputStream(this);
    private PipedInputStream pipedInputStream;
    private int port;
    ByteBuffer recievedPayload;
    private String uri;
    private WebSocketReceiver webSocketReceiver;

    public WebSocketNetworkModule(SocketFactory factory, String uri2, String host2, int port2, String resourceContext) {
        super(factory, host2, port2, resourceContext);
        this.uri = uri2;
        this.host = host2;
        this.port = port2;
        this.pipedInputStream = new PipedInputStream();
        log.setResourceName(resourceContext);
    }

    public void start() throws IOException, MqttException {
        super.start();
        new WebSocketHandshake(getSocketInputStream(), getSocketOutputStream(), this.uri, this.host, this.port).execute();
        this.webSocketReceiver = new WebSocketReceiver(getSocketInputStream(), this.pipedInputStream);
        this.webSocketReceiver.start("webSocketReceiver");
    }

    /* access modifiers changed from: package-private */
    public OutputStream getSocketOutputStream() throws IOException {
        return super.getOutputStream();
    }

    /* access modifiers changed from: package-private */
    public InputStream getSocketInputStream() throws IOException {
        return super.getInputStream();
    }

    public InputStream getInputStream() throws IOException {
        return this.pipedInputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    public void stop() throws IOException {
        getSocketOutputStream().write(new WebSocketFrame((byte) 8, true, "1000".getBytes()).encodeFrame());
        getSocketOutputStream().flush();
        if (this.webSocketReceiver != null) {
            this.webSocketReceiver.stop();
        }
        super.stop();
    }

    public String getServerURI() {
        return "ws://" + this.host + ":" + this.port;
    }
}
