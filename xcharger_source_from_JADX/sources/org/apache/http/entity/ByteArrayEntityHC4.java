package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.util.Args;

@NotThreadSafe
public class ByteArrayEntityHC4 extends AbstractHttpEntityHC4 implements Cloneable {

    /* renamed from: b */
    private final byte[] f169b;
    @Deprecated
    protected final byte[] content;
    private final int len;
    private final int off;

    public ByteArrayEntityHC4(byte[] b, ContentType contentType) {
        Args.notNull(b, "Source byte array");
        this.content = b;
        this.f169b = b;
        this.off = 0;
        this.len = this.f169b.length;
        if (contentType != null) {
            setContentType(contentType.toString());
        }
    }

    public ByteArrayEntityHC4(byte[] b, int off2, int len2, ContentType contentType) {
        Args.notNull(b, "Source byte array");
        if (off2 < 0 || off2 > b.length || len2 < 0 || off2 + len2 < 0 || off2 + len2 > b.length) {
            throw new IndexOutOfBoundsException("off: " + off2 + " len: " + len2 + " b.length: " + b.length);
        }
        this.content = b;
        this.f169b = b;
        this.off = off2;
        this.len = len2;
        if (contentType != null) {
            setContentType(contentType.toString());
        }
    }

    public ByteArrayEntityHC4(byte[] b) {
        this(b, (ContentType) null);
    }

    public ByteArrayEntityHC4(byte[] b, int off2, int len2) {
        this(b, off2, len2, (ContentType) null);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return (long) this.len;
    }

    public InputStream getContent() {
        return new ByteArrayInputStream(this.f169b, this.off, this.len);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        outstream.write(this.f169b, this.off, this.len);
        outstream.flush();
    }

    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
