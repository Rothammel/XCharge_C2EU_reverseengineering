package org.apache.http.impl.p013io;

import org.apache.http.HttpRequest;
import org.apache.http.annotation.Immutable;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicLineFormatterHC4;
import org.apache.http.message.LineFormatter;
import org.apache.http.p014io.HttpMessageWriterFactory;

@Immutable
/* renamed from: org.apache.http.impl.io.DefaultHttpRequestWriterFactory */
public class DefaultHttpRequestWriterFactory implements HttpMessageWriterFactory<HttpRequest> {
    public static final DefaultHttpRequestWriterFactory INSTANCE = new DefaultHttpRequestWriterFactory();
    private final LineFormatter lineFormatter;

    public DefaultHttpRequestWriterFactory(LineFormatter lineFormatter2) {
        this.lineFormatter = lineFormatter2 == null ? BasicLineFormatterHC4.INSTANCE : lineFormatter2;
    }

    public DefaultHttpRequestWriterFactory() {
        this((LineFormatter) null);
    }

    public HttpMessageWriter create(SessionOutputBuffer buffer) {
        return new DefaultHttpRequestWriter(buffer, this.lineFormatter);
    }
}