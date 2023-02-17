package org.apache.http.impl.conn;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;

@NotThreadSafe
/* loaded from: classes.dex */
class LoggingManagedHttpClientConnection extends DefaultManagedHttpClientConnection {
    private static final String HEADER_TAG = "Headers";
    private static final String TAG = "HttpClient";
    private final WireHC4 wire;

    public LoggingManagedHttpClientConnection(String id, int buffersize, int fragmentSizeHint, CharsetDecoder chardecoder, CharsetEncoder charencoder, MessageConstraints constraints, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, HttpMessageWriterFactory<HttpRequest> requestWriterFactory, HttpMessageParserFactory<HttpResponse> responseParserFactory) {
        super(id, buffersize, fragmentSizeHint, chardecoder, charencoder, constraints, incomingContentStrategy, outgoingContentStrategy, requestWriterFactory, responseParserFactory);
        this.wire = new WireHC4(id);
    }

    @Override // org.apache.http.impl.BHttpConnectionBase
    public void close() throws IOException {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, String.valueOf(getId()) + ": Close connection");
        }
        super.close();
    }

    @Override // org.apache.http.impl.conn.DefaultManagedHttpClientConnection, org.apache.http.impl.BHttpConnectionBase
    public void shutdown() throws IOException {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, String.valueOf(getId()) + ": Shutdown connection");
        }
        super.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.http.impl.BHttpConnectionBase
    public InputStream getSocketInputStream(Socket socket) throws IOException {
        InputStream in = super.getSocketInputStream(socket);
        if (this.wire.enabled()) {
            return new LoggingInputStream(in, this.wire);
        }
        return in;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.http.impl.BHttpConnectionBase
    public OutputStream getSocketOutputStream(Socket socket) throws IOException {
        OutputStream out = super.getSocketOutputStream(socket);
        if (this.wire.enabled()) {
            return new LoggingOutputStream(out, this.wire);
        }
        return out;
    }

    @Override // org.apache.http.impl.DefaultBHttpClientConnection
    protected void onResponseReceived(HttpResponse response) {
        if (response != null && Log.isLoggable(HEADER_TAG, 3)) {
            Log.d(HEADER_TAG, String.valueOf(getId()) + " << " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                Log.d(HEADER_TAG, String.valueOf(getId()) + " << " + header.toString());
            }
        }
    }

    @Override // org.apache.http.impl.DefaultBHttpClientConnection
    protected void onRequestSubmitted(HttpRequest request) {
        if (request != null && Log.isLoggable(HEADER_TAG, 3)) {
            Log.d(HEADER_TAG, String.valueOf(getId()) + " >> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (Header header : headers) {
                Log.d(HEADER_TAG, String.valueOf(getId()) + " >> " + header.toString());
            }
        }
    }
}
