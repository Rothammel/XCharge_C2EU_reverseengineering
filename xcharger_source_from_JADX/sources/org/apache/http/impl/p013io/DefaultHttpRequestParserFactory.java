package org.apache.http.impl.p013io;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.DefaultHttpRequestFactoryHC4;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicLineParserHC4;
import org.apache.http.message.LineParser;
import org.apache.http.p014io.HttpMessageParserFactory;

@Immutable
/* renamed from: org.apache.http.impl.io.DefaultHttpRequestParserFactory */
public class DefaultHttpRequestParserFactory implements HttpMessageParserFactory<HttpRequest> {
    public static final DefaultHttpRequestParserFactory INSTANCE = new DefaultHttpRequestParserFactory();
    private final LineParser lineParser;
    private final HttpRequestFactory requestFactory;

    public DefaultHttpRequestParserFactory(LineParser lineParser2, HttpRequestFactory requestFactory2) {
        this.lineParser = lineParser2 == null ? BasicLineParserHC4.INSTANCE : lineParser2;
        this.requestFactory = requestFactory2 == null ? DefaultHttpRequestFactoryHC4.INSTANCE : requestFactory2;
    }

    public DefaultHttpRequestParserFactory() {
        this((LineParser) null, (HttpRequestFactory) null);
    }

    public HttpMessageParser create(SessionInputBuffer buffer, MessageConstraints constraints) {
        return new DefaultHttpRequestParser(buffer, this.lineParser, this.requestFactory, constraints);
    }
}
