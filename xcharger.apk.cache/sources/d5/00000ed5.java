package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/* loaded from: classes.dex */
class ExtendedByteArrayOutputStream extends ByteArrayOutputStream {
    final WebSocketNetworkModule webSocketNetworkModule;
    final WebSocketSecureNetworkModule webSocketSecureNetworkModule;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExtendedByteArrayOutputStream(WebSocketNetworkModule module) {
        this.webSocketNetworkModule = module;
        this.webSocketSecureNetworkModule = null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExtendedByteArrayOutputStream(WebSocketSecureNetworkModule module) {
        this.webSocketNetworkModule = null;
        this.webSocketSecureNetworkModule = module;
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        ByteBuffer byteBuffer;
        synchronized (this) {
            byteBuffer = ByteBuffer.wrap(toByteArray());
            reset();
        }
        WebSocketFrame frame = new WebSocketFrame((byte) 2, true, byteBuffer.array());
        byte[] rawFrame = frame.encodeFrame();
        getSocketOutputStream().write(rawFrame);
        getSocketOutputStream().flush();
    }

    OutputStream getSocketOutputStream() throws IOException {
        if (this.webSocketNetworkModule != null) {
            return this.webSocketNetworkModule.getSocketOutputStream();
        }
        if (this.webSocketSecureNetworkModule != null) {
            return this.webSocketSecureNetworkModule.getSocketOutputStream();
        }
        return null;
    }
}