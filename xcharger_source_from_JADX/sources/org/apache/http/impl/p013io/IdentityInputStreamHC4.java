package org.apache.http.impl.p013io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.p014io.BufferInfo;
import org.apache.http.util.Args;

@NotThreadSafe
/* renamed from: org.apache.http.impl.io.IdentityInputStreamHC4 */
public class IdentityInputStreamHC4 extends InputStream {
    private boolean closed = false;

    /* renamed from: in */
    private final SessionInputBuffer f182in;

    public IdentityInputStreamHC4(SessionInputBuffer in) {
        this.f182in = (SessionInputBuffer) Args.notNull(in, "Session input buffer");
    }

    public int available() throws IOException {
        if (this.f182in instanceof BufferInfo) {
            return this.f182in.length();
        }
        return 0;
    }

    public void close() throws IOException {
        this.closed = true;
    }

    public int read() throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.f182in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.f182in.read(b, off, len);
    }
}
