package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapperHC4;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.Args;

/* loaded from: classes.dex */
public class GzipCompressingEntity extends HttpEntityWrapperHC4 {
    private static final String GZIP_CODEC = "gzip";

    public GzipCompressingEntity(HttpEntity entity) {
        super(entity);
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public Header getContentEncoding() {
        return new BasicHeader("Content-Encoding", GZIP_CODEC);
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public long getContentLength() {
        return -1L;
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public boolean isChunked() {
        return true;
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public void writeTo(OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        GZIPOutputStream gzip = new GZIPOutputStream(outstream);
        this.wrappedEntity.writeTo(gzip);
        gzip.close();
    }
}