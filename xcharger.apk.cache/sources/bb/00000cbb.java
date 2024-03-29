package org.apache.http.impl.io;

import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicLineFormatterHC4;
import org.apache.http.message.LineFormatter;

@Immutable
/* loaded from: classes.dex */
public class DefaultHttpResponseWriterFactory implements HttpMessageWriterFactory<HttpResponse> {
    public static final DefaultHttpResponseWriterFactory INSTANCE = new DefaultHttpResponseWriterFactory();
    private final LineFormatter lineFormatter;

    public DefaultHttpResponseWriterFactory(LineFormatter lineFormatter) {
        this.lineFormatter = lineFormatter == null ? BasicLineFormatterHC4.INSTANCE : lineFormatter;
    }

    public DefaultHttpResponseWriterFactory() {
        this(null);
    }

    @Override // org.apache.http.io.HttpMessageWriterFactory
    public HttpMessageWriter create(SessionOutputBuffer buffer) {
        return new DefaultHttpResponseWriter(buffer, this.lineFormatter);
    }
}