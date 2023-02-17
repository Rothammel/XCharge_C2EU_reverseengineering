package org.apache.http.impl;

import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.annotation.Immutable;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class DefaultHttpResponseFactoryHC4 implements HttpResponseFactory {
    public static final DefaultHttpResponseFactoryHC4 INSTANCE = new DefaultHttpResponseFactoryHC4();
    protected final ReasonPhraseCatalog reasonCatalog;

    public DefaultHttpResponseFactoryHC4(ReasonPhraseCatalog catalog) {
        this.reasonCatalog = (ReasonPhraseCatalog) Args.notNull(catalog, "Reason phrase catalog");
    }

    public DefaultHttpResponseFactoryHC4() {
        this(EnglishReasonPhraseCatalogHC4.INSTANCE);
    }

    public HttpResponse newHttpResponse(ProtocolVersion ver, int status, HttpContext context) {
        Args.notNull(ver, "HTTP version");
        Locale loc = determineLocale(context);
        String reason = this.reasonCatalog.getReason(status, loc);
        return new BasicHttpResponse(new BasicStatusLine(ver, status, reason), this.reasonCatalog, loc);
    }

    public HttpResponse newHttpResponse(StatusLine statusline, HttpContext context) {
        Args.notNull(statusline, "Status line");
        return new BasicHttpResponse(statusline, this.reasonCatalog, determineLocale(context));
    }

    protected Locale determineLocale(HttpContext context) {
        return Locale.getDefault();
    }
}
