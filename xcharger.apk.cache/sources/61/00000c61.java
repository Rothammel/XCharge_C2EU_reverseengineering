package org.apache.http.impl.conn;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.DefaultHttpResponseFactoryHC4;
import org.apache.http.impl.io.AbstractMessageParserHC4;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.LineParser;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
/* loaded from: classes.dex */
public class DefaultHttpResponseParser extends AbstractMessageParserHC4<HttpResponse> {
    private static final String TAG = "HttpClient";
    private final CharArrayBuffer lineBuf;
    private final HttpResponseFactory responseFactory;

    @Deprecated
    public DefaultHttpResponseParser(SessionInputBuffer buffer, LineParser parser, HttpResponseFactory responseFactory, HttpParams params) {
        super(buffer, parser, params);
        Args.notNull(responseFactory, "Response factory");
        this.responseFactory = responseFactory;
        this.lineBuf = new CharArrayBuffer(128);
    }

    public DefaultHttpResponseParser(SessionInputBuffer buffer, LineParser lineParser, HttpResponseFactory responseFactory, MessageConstraints constraints) {
        super(buffer, lineParser, constraints);
        this.responseFactory = responseFactory == null ? DefaultHttpResponseFactoryHC4.INSTANCE : responseFactory;
        this.lineBuf = new CharArrayBuffer(128);
    }

    public DefaultHttpResponseParser(SessionInputBuffer buffer, MessageConstraints constraints) {
        this(buffer, (LineParser) null, (HttpResponseFactory) null, constraints);
    }

    public DefaultHttpResponseParser(SessionInputBuffer buffer) {
        this(buffer, (LineParser) null, (HttpResponseFactory) null, MessageConstraints.DEFAULT);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0051, code lost:
        throw new org.apache.http.ProtocolException("The server failed to respond with a valid HTTP response");
     */
    @Override // org.apache.http.impl.io.AbstractMessageParserHC4
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public org.apache.http.HttpResponse parseHead(org.apache.http.io.SessionInputBuffer r9) throws java.io.IOException, org.apache.http.HttpException {
        /*
            r8 = this;
            r7 = -1
            r0 = 0
            r1 = 0
        L3:
            org.apache.http.util.CharArrayBuffer r4 = r8.lineBuf
            r4.clear()
            org.apache.http.util.CharArrayBuffer r4 = r8.lineBuf
            int r2 = r9.readLine(r4)
            if (r2 != r7) goto L1a
            if (r0 != 0) goto L1a
            org.apache.http.NoHttpResponseException r4 = new org.apache.http.NoHttpResponseException
            java.lang.String r5 = "The target server failed to respond"
            r4.<init>(r5)
            throw r4
        L1a:
            org.apache.http.message.ParserCursor r1 = new org.apache.http.message.ParserCursor
            r4 = 0
            org.apache.http.util.CharArrayBuffer r5 = r8.lineBuf
            int r5 = r5.length()
            r1.<init>(r4, r5)
            org.apache.http.message.LineParser r4 = r8.lineParser
            org.apache.http.util.CharArrayBuffer r5 = r8.lineBuf
            boolean r4 = r4.hasProtocolVersion(r5, r1)
            if (r4 == 0) goto L40
            org.apache.http.message.LineParser r4 = r8.lineParser
            org.apache.http.util.CharArrayBuffer r5 = r8.lineBuf
            org.apache.http.StatusLine r3 = r4.parseStatusLine(r5, r1)
            org.apache.http.HttpResponseFactory r4 = r8.responseFactory
            r5 = 0
            org.apache.http.HttpResponse r4 = r4.newHttpResponse(r3, r5)
            return r4
        L40:
            if (r2 == r7) goto L4a
            org.apache.http.util.CharArrayBuffer r4 = r8.lineBuf
            boolean r4 = r8.reject(r4, r0)
            if (r4 == 0) goto L52
        L4a:
            org.apache.http.ProtocolException r4 = new org.apache.http.ProtocolException
            java.lang.String r5 = "The server failed to respond with a valid HTTP response"
            r4.<init>(r5)
            throw r4
        L52:
            java.lang.String r4 = "HttpClient"
            r5 = 3
            boolean r4 = android.util.Log.isLoggable(r4, r5)
            if (r4 == 0) goto L75
            java.lang.String r4 = "HttpClient"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r6 = "Garbage in response: "
            r5.<init>(r6)
            org.apache.http.util.CharArrayBuffer r6 = r8.lineBuf
            java.lang.String r6 = r6.toString()
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
        L75:
            int r0 = r0 + 1
            goto L3
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(org.apache.http.io.SessionInputBuffer):org.apache.http.HttpResponse");
    }

    protected boolean reject(CharArrayBuffer line, int count) {
        return false;
    }
}