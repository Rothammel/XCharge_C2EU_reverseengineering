package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

/* loaded from: classes.dex */
public class DeflateDecompressingEntity extends DecompressingEntity {
    @Override // org.apache.http.client.entity.DecompressingEntity, org.apache.http.entity.HttpEntityWrapperHC4
    public /* bridge */ /* synthetic */ InputStream getContent() throws IOException {
        return super.getContent();
    }

    @Override // org.apache.http.client.entity.DecompressingEntity, org.apache.http.entity.HttpEntityWrapperHC4
    public /* bridge */ /* synthetic */ void writeTo(OutputStream outputStream) throws IOException {
        super.writeTo(outputStream);
    }

    public DeflateDecompressingEntity(HttpEntity entity) {
        super(entity);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.apache.http.client.entity.DecompressingEntity
    public InputStream decorate(InputStream wrapped) throws IOException {
        return new DeflateInputStream(wrapped);
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public Header getContentEncoding() {
        return null;
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public long getContentLength() {
        return -1L;
    }
}
