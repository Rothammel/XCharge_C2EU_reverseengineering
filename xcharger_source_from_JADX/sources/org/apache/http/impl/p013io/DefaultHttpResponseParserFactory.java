package org.apache.http.impl.p013io;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.DefaultHttpResponseFactoryHC4;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicLineParserHC4;
import org.apache.http.message.LineParser;
import org.apache.http.p014io.HttpMessageParserFactory;

@Immutable
/* renamed from: org.apache.http.impl.io.DefaultHttpResponseParserFactory */
public class DefaultHttpResponseParserFactory implements HttpMessageParserFactory<HttpResponse> {
    public static final DefaultHttpResponseParserFactory INSTANCE = new DefaultHttpResponseParserFactory();
    private final LineParser lineParser;
    private final HttpResponseFactory responseFactory;

    public DefaultHttpResponseParserFactory(LineParser lineParser2, HttpResponseFactory responseFactory2) {
        this.lineParser = lineParser2 == null ? BasicLineParserHC4.INSTANCE : lineParser2;
        this.responseFactory = responseFactory2 == null ? DefaultHttpResponseFactoryHC4.INSTANCE : responseFactory2;
    }

    public DefaultHttpResponseParserFactory() {
        this((LineParser) null, (HttpResponseFactory) null);
    }

    public HttpMessageParser create(SessionInputBuffer buffer, MessageConstraints constraints) {
        return new DefaultHttpResponseParser(buffer, this.lineParser, this.responseFactory, constraints);
    }
}
