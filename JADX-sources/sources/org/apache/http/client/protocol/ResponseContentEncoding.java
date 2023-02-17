package org.apache.http.client.protocol;

import java.io.IOException;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;

@Immutable
/* loaded from: classes.dex */
public class ResponseContentEncoding implements HttpResponseInterceptor {
    public static final String UNCOMPRESSED = "http.client.response.uncompressed";

    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        Header ceheader;
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.getContentLength() != 0 && (ceheader = entity.getContentEncoding()) != null) {
            HeaderElement[] codecs = ceheader.getElements();
            boolean uncompressed = false;
            if (codecs.length != 0) {
                HeaderElement codec = codecs[0];
                String codecname = codec.getName().toLowerCase(Locale.ENGLISH);
                if ("gzip".equals(codecname) || "x-gzip".equals(codecname)) {
                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                    uncompressed = true;
                } else if ("deflate".equals(codecname)) {
                    response.setEntity(new DeflateDecompressingEntity(response.getEntity()));
                    uncompressed = true;
                } else if (!"identity".equals(codecname)) {
                    throw new HttpException("Unsupported Content-Coding: " + codec.getName());
                } else {
                    return;
                }
            }
            if (uncompressed) {
                response.removeHeaders("Content-Length");
                response.removeHeaders("Content-Encoding");
                response.removeHeaders("Content-MD5");
            }
        }
    }
}
