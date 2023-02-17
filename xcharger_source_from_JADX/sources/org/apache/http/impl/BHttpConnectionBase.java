package org.apache.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpMessage;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.BasicHttpEntityHC4;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.entity.LaxContentLengthStrategyHC4;
import org.apache.http.impl.entity.StrictContentLengthStrategyHC4;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.p013io.ChunkedInputStreamHC4;
import org.apache.http.impl.p013io.ChunkedOutputStreamHC4;
import org.apache.http.impl.p013io.ContentLengthInputStreamHC4;
import org.apache.http.impl.p013io.ContentLengthOutputStreamHC4;
import org.apache.http.impl.p013io.IdentityInputStreamHC4;
import org.apache.http.impl.p013io.IdentityOutputStreamHC4;
import org.apache.http.impl.p013io.SessionInputBufferImpl;
import org.apache.http.impl.p013io.SessionOutputBufferImpl;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.NetUtils;

@NotThreadSafe
public class BHttpConnectionBase implements HttpConnection, HttpInetConnection {
    private final HttpConnectionMetricsImpl connMetrics;
    private final SessionInputBufferImpl inbuffer;
    private final ContentLengthStrategy incomingContentStrategy;
    private volatile boolean open;
    private final SessionOutputBufferImpl outbuffer;
    private final ContentLengthStrategy outgoingContentStrategy;
    private volatile Socket socket;

    protected BHttpConnectionBase(int buffersize, int fragmentSizeHint, CharsetDecoder chardecoder, CharsetEncoder charencoder, MessageConstraints constraints, ContentLengthStrategy incomingContentStrategy2, ContentLengthStrategy outgoingContentStrategy2) {
        Args.positive(buffersize, "Buffer size");
        HttpTransportMetricsImpl inTransportMetrics = new HttpTransportMetricsImpl();
        HttpTransportMetricsImpl outTransportMetrics = new HttpTransportMetricsImpl();
        this.inbuffer = new SessionInputBufferImpl(inTransportMetrics, buffersize, -1, constraints != null ? constraints : MessageConstraints.DEFAULT, chardecoder);
        this.outbuffer = new SessionOutputBufferImpl(outTransportMetrics, buffersize, fragmentSizeHint, charencoder);
        this.connMetrics = new HttpConnectionMetricsImpl(inTransportMetrics, outTransportMetrics);
        this.incomingContentStrategy = incomingContentStrategy2 == null ? LaxContentLengthStrategyHC4.INSTANCE : incomingContentStrategy2;
        this.outgoingContentStrategy = outgoingContentStrategy2 == null ? StrictContentLengthStrategyHC4.INSTANCE : outgoingContentStrategy2;
    }

    /* access modifiers changed from: protected */
    public void ensureOpen() throws IOException {
        Asserts.check(this.open, "Connection is not open");
        if (!this.inbuffer.isBound()) {
            this.inbuffer.bind(getSocketInputStream(this.socket));
        }
        if (!this.outbuffer.isBound()) {
            this.outbuffer.bind(getSocketOutputStream(this.socket));
        }
    }

    /* access modifiers changed from: protected */
    public InputStream getSocketInputStream(Socket socket2) throws IOException {
        return socket2.getInputStream();
    }

    /* access modifiers changed from: protected */
    public OutputStream getSocketOutputStream(Socket socket2) throws IOException {
        return socket2.getOutputStream();
    }

    /* access modifiers changed from: protected */
    public void bind(Socket socket2) throws IOException {
        Args.notNull(socket2, "Socket");
        this.socket = socket2;
        this.open = true;
        this.inbuffer.bind((InputStream) null);
        this.outbuffer.bind((OutputStream) null);
    }

    /* access modifiers changed from: protected */
    public SessionInputBuffer getSessionInputBuffer() {
        return this.inbuffer;
    }

    /* access modifiers changed from: protected */
    public SessionOutputBuffer getSessionOutputBuffer() {
        return this.outbuffer;
    }

    /* access modifiers changed from: protected */
    public void doFlush() throws IOException {
        this.outbuffer.flush();
    }

    public boolean isOpen() {
        return this.open;
    }

    /* access modifiers changed from: protected */
    public Socket getSocket() {
        return this.socket;
    }

    /* access modifiers changed from: protected */
    public OutputStream createOutputStream(long len, SessionOutputBuffer outbuffer2) {
        if (len == -2) {
            return new ChunkedOutputStreamHC4(2048, outbuffer2);
        }
        if (len == -1) {
            return new IdentityOutputStreamHC4(outbuffer2);
        }
        return new ContentLengthOutputStreamHC4(outbuffer2, len);
    }

    /* access modifiers changed from: protected */
    public OutputStream prepareOutput(HttpMessage message) throws HttpException {
        return createOutputStream(this.outgoingContentStrategy.determineLength(message), this.outbuffer);
    }

    /* access modifiers changed from: protected */
    public InputStream createInputStream(long len, SessionInputBuffer inbuffer2) {
        if (len == -2) {
            return new ChunkedInputStreamHC4(inbuffer2);
        }
        if (len == -1) {
            return new IdentityInputStreamHC4(inbuffer2);
        }
        return new ContentLengthInputStreamHC4(inbuffer2, len);
    }

    /* access modifiers changed from: protected */
    public HttpEntity prepareInput(HttpMessage message) throws HttpException {
        BasicHttpEntityHC4 entity = new BasicHttpEntityHC4();
        long len = this.incomingContentStrategy.determineLength(message);
        InputStream instream = createInputStream(len, this.inbuffer);
        if (len == -2) {
            entity.setChunked(true);
            entity.setContentLength(-1);
            entity.setContent(instream);
        } else if (len == -1) {
            entity.setChunked(false);
            entity.setContentLength(-1);
            entity.setContent(instream);
        } else {
            entity.setChunked(false);
            entity.setContentLength(len);
            entity.setContent(instream);
        }
        Header contentTypeHeader = message.getFirstHeader("Content-Type");
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader);
        }
        Header contentEncodingHeader = message.getFirstHeader("Content-Encoding");
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader);
        }
        return entity;
    }

    public InetAddress getLocalAddress() {
        if (this.socket != null) {
            return this.socket.getLocalAddress();
        }
        return null;
    }

    public int getLocalPort() {
        if (this.socket != null) {
            return this.socket.getLocalPort();
        }
        return -1;
    }

    public InetAddress getRemoteAddress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        }
        return null;
    }

    public int getRemotePort() {
        if (this.socket != null) {
            return this.socket.getPort();
        }
        return -1;
    }

    public void setSocketTimeout(int timeout) {
        if (this.socket != null) {
            try {
                this.socket.setSoTimeout(timeout);
            } catch (SocketException e) {
            }
        }
    }

    public int getSocketTimeout() {
        if (this.socket == null) {
            return -1;
        }
        try {
            return this.socket.getSoTimeout();
        } catch (SocketException e) {
            return -1;
        }
    }

    public void shutdown() throws IOException {
        this.open = false;
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    public void close() throws IOException {
        if (this.open) {
            this.open = false;
            Socket sock = this.socket;
            try {
                this.inbuffer.clear();
                this.outbuffer.flush();
                try {
                    sock.shutdownOutput();
                } catch (IOException e) {
                }
                try {
                    sock.shutdownInput();
                } catch (IOException | UnsupportedOperationException e2) {
                }
            } finally {
                sock.close();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private int fillInputBuffer(int timeout) throws IOException {
        int oldtimeout = this.socket.getSoTimeout();
        try {
            this.socket.setSoTimeout(timeout);
            int fillBuffer = this.inbuffer.fillBuffer();
            this.socket.setSoTimeout(oldtimeout);
            return fillBuffer;
        } catch (Throwable th) {
            this.socket.setSoTimeout(oldtimeout);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean awaitInput(int timeout) throws IOException {
        if (this.inbuffer.hasBufferedData()) {
            return true;
        }
        fillInputBuffer(timeout);
        return this.inbuffer.hasBufferedData();
    }

    public boolean isStale() {
        if (!isOpen()) {
            return true;
        }
        try {
            if (fillInputBuffer(1) >= 0) {
                return false;
            }
            return true;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException e2) {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void incrementRequestCount() {
        this.connMetrics.incrementRequestCount();
    }

    /* access modifiers changed from: protected */
    public void incrementResponseCount() {
        this.connMetrics.incrementResponseCount();
    }

    public HttpConnectionMetrics getMetrics() {
        return this.connMetrics;
    }

    public String toString() {
        if (this.socket == null) {
            return "[Not bound]";
        }
        StringBuilder buffer = new StringBuilder();
        SocketAddress remoteAddress = this.socket.getRemoteSocketAddress();
        SocketAddress localAddress = this.socket.getLocalSocketAddress();
        if (!(remoteAddress == null || localAddress == null)) {
            NetUtils.formatAddress(buffer, localAddress);
            buffer.append("<->");
            NetUtils.formatAddress(buffer, remoteAddress);
        }
        return buffer.toString();
    }
}
