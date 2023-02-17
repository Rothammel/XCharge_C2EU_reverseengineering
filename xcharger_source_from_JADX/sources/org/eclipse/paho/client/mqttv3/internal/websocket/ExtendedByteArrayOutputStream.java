package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class ExtendedByteArrayOutputStream extends ByteArrayOutputStream {
    final WebSocketNetworkModule webSocketNetworkModule;
    final WebSocketSecureNetworkModule webSocketSecureNetworkModule;

    ExtendedByteArrayOutputStream(WebSocketNetworkModule module) {
        this.webSocketNetworkModule = module;
        this.webSocketSecureNetworkModule = null;
    }

    ExtendedByteArrayOutputStream(WebSocketSecureNetworkModule module) {
        this.webSocketNetworkModule = null;
        this.webSocketSecureNetworkModule = module;
    }

    public void flush() throws IOException {
        ByteBuffer byteBuffer;
        synchronized (this) {
            byteBuffer = ByteBuffer.wrap(toByteArray());
            reset();
        }
        getSocketOutputStream().write(new WebSocketFrame((byte) 2, true, byteBuffer.array()).encodeFrame());
        getSocketOutputStream().flush();
    }

    /* access modifiers changed from: package-private */
    public OutputStream getSocketOutputStream() throws IOException {
        if (this.webSocketNetworkModule != null) {
            return this.webSocketNetworkModule.getSocketOutputStream();
        }
        if (this.webSocketSecureNetworkModule != null) {
            return this.webSocketSecureNetworkModule.getSocketOutputStream();
        }
        return null;
    }
}
