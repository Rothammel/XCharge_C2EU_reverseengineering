package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapperHC4;
import org.apache.http.util.Args;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class DecompressingEntity extends HttpEntityWrapperHC4 {
    private static final int BUFFER_SIZE = 2048;
    private InputStream content;

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract InputStream decorate(InputStream inputStream) throws IOException;

    public DecompressingEntity(HttpEntity wrapped) {
        super(wrapped);
    }

    private InputStream getDecompressingStream() throws IOException {
        InputStream in = this.wrappedEntity.getContent();
        return new LazyDecompressingInputStream(in, this);
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public InputStream getContent() throws IOException {
        if (this.wrappedEntity.isStreaming()) {
            if (this.content == null) {
                this.content = getDecompressingStream();
            }
            return this.content;
        }
        return getDecompressingStream();
    }

    @Override // org.apache.http.entity.HttpEntityWrapperHC4
    public void writeTo(OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        InputStream instream = getContent();
        try {
            byte[] buffer = new byte[2048];
            while (true) {
                int l = instream.read(buffer);
                if (l != -1) {
                    outstream.write(buffer, 0, l);
                } else {
                    return;
                }
            }
        } finally {
            instream.close();
        }
    }
}
