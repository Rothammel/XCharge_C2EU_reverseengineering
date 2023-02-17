package org.apache.http.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.p014io.HttpMessageParserFactory;
import org.apache.http.p014io.HttpMessageWriterFactory;
import org.apache.http.util.Args;

@NotThreadSafe
public class DefaultBHttpServerConnection extends BHttpConnectionBase implements HttpServerConnection {
    private final HttpMessageParser requestParser;
    private final HttpMessageWriter responseWriter;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DefaultBHttpServerConnection(int r10, int r11, java.nio.charset.CharsetDecoder r12, java.nio.charset.CharsetEncoder r13, org.apache.http.config.MessageConstraints r14, org.apache.http.entity.ContentLengthStrategy r15, org.apache.http.entity.ContentLengthStrategy r16, org.apache.http.p014io.HttpMessageParserFactory<org.apache.http.HttpRequest> r17, org.apache.http.p014io.HttpMessageWriterFactory<org.apache.http.HttpResponse> r18) {
        /*
            r9 = this;
            if (r15 == 0) goto L_0x002b
            r7 = r15
        L_0x0003:
            r1 = r9
            r2 = r10
            r3 = r11
            r4 = r12
            r5 = r13
            r6 = r14
            r8 = r16
            r1.<init>(r2, r3, r4, r5, r6, r7, r8)
            if (r17 == 0) goto L_0x002e
        L_0x0010:
            org.apache.http.io.SessionInputBuffer r1 = r9.getSessionInputBuffer()
            r0 = r17
            org.apache.http.io.HttpMessageParser r1 = r0.create(r1, r14)
            r9.requestParser = r1
            if (r18 == 0) goto L_0x0031
        L_0x001e:
            org.apache.http.io.SessionOutputBuffer r1 = r9.getSessionOutputBuffer()
            r0 = r18
            org.apache.http.io.HttpMessageWriter r1 = r0.create(r1)
            r9.responseWriter = r1
            return
        L_0x002b:
            org.apache.http.impl.entity.DisallowIdentityContentLengthStrategy r7 = org.apache.http.impl.entity.DisallowIdentityContentLengthStrategy.INSTANCE
            goto L_0x0003
        L_0x002e:
            org.apache.http.impl.io.DefaultHttpRequestParserFactory r17 = org.apache.http.impl.p013io.DefaultHttpRequestParserFactory.INSTANCE
            goto L_0x0010
        L_0x0031:
            org.apache.http.impl.io.DefaultHttpResponseWriterFactory r18 = org.apache.http.impl.p013io.DefaultHttpResponseWriterFactory.INSTANCE
            goto L_0x001e
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.DefaultBHttpServerConnection.<init>(int, int, java.nio.charset.CharsetDecoder, java.nio.charset.CharsetEncoder, org.apache.http.config.MessageConstraints, org.apache.http.entity.ContentLengthStrategy, org.apache.http.entity.ContentLengthStrategy, org.apache.http.io.HttpMessageParserFactory, org.apache.http.io.HttpMessageWriterFactory):void");
    }

    public DefaultBHttpServerConnection(int buffersize, CharsetDecoder chardecoder, CharsetEncoder charencoder, MessageConstraints constraints) {
        this(buffersize, buffersize, chardecoder, charencoder, constraints, (ContentLengthStrategy) null, (ContentLengthStrategy) null, (HttpMessageParserFactory<HttpRequest>) null, (HttpMessageWriterFactory<HttpResponse>) null);
    }

    public DefaultBHttpServerConnection(int buffersize) {
        this(buffersize, buffersize, (CharsetDecoder) null, (CharsetEncoder) null, (MessageConstraints) null, (ContentLengthStrategy) null, (ContentLengthStrategy) null, (HttpMessageParserFactory<HttpRequest>) null, (HttpMessageWriterFactory<HttpResponse>) null);
    }

    /* access modifiers changed from: protected */
    public void onRequestReceived(HttpRequest request) {
    }

    /* access modifiers changed from: protected */
    public void onResponseSubmitted(HttpResponse response) {
    }

    public void bind(Socket socket) throws IOException {
        super.bind(socket);
    }

    public HttpRequest receiveRequestHeader() throws HttpException, IOException {
        ensureOpen();
        HttpRequest request = this.requestParser.parse();
        onRequestReceived(request);
        incrementRequestCount();
        return request;
    }

    public void receiveRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        ensureOpen();
        request.setEntity(prepareInput(request));
    }

    public void sendResponseHeader(HttpResponse response) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        ensureOpen();
        this.responseWriter.write(response);
        onResponseSubmitted(response);
        if (response.getStatusLine().getStatusCode() >= 200) {
            incrementResponseCount();
        }
    }

    public void sendResponseEntity(HttpResponse response) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        ensureOpen();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            OutputStream outstream = prepareOutput(response);
            entity.writeTo(outstream);
            outstream.close();
        }
    }

    public void flush() throws IOException {
        ensureOpen();
        doFlush();
    }
}
