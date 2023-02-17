package org.java_websocket.server;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.java_websocket.SSLSocketChannel2;

public class CustomSSLWebSocketServerFactory extends DefaultSSLWebSocketServerFactory {
    private final String[] enabledCiphersuites;
    private final String[] enabledProtocols;

    public CustomSSLWebSocketServerFactory(SSLContext sslContext, String[] enabledProtocols2, String[] enabledCiphersuites2) {
        this(sslContext, Executors.newSingleThreadScheduledExecutor(), enabledProtocols2, enabledCiphersuites2);
    }

    public CustomSSLWebSocketServerFactory(SSLContext sslContext, ExecutorService executerService, String[] enabledProtocols2, String[] enabledCiphersuites2) {
        super(sslContext, executerService);
        this.enabledProtocols = enabledProtocols2;
        this.enabledCiphersuites = enabledCiphersuites2;
    }

    public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
        SSLEngine e = this.sslcontext.createSSLEngine();
        if (this.enabledProtocols != null) {
            e.setEnabledProtocols(this.enabledProtocols);
        }
        if (this.enabledCiphersuites != null) {
            e.setEnabledCipherSuites(this.enabledCiphersuites);
        }
        e.setUseClientMode(false);
        return new SSLSocketChannel2(channel, e, this.exec, key);
    }
}
