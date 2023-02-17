package org.apache.http.impl.p013io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.ConnectionClosedException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.p014io.BufferInfo;
import org.apache.http.util.Args;

@NotThreadSafe
/* renamed from: org.apache.http.impl.io.ContentLengthInputStreamHC4 */
public class ContentLengthInputStreamHC4 extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private boolean closed = false;
    private final long contentLength;

    /* renamed from: in */
    private SessionInputBuffer f181in = null;
    private long pos = 0;

    public ContentLengthInputStreamHC4(SessionInputBuffer in, long contentLength2) {
        this.f181in = (SessionInputBuffer) Args.notNull(in, "Session input buffer");
        this.contentLength = Args.notNegative(contentLength2, "Content length");
    }

    public void close() throws IOException {
        if (!this.closed) {
            try {
                if (this.pos < this.contentLength) {
                    do {
                    } while (read(new byte[2048]) >= 0);
                }
            } finally {
                this.closed = true;
            }
        }
    }

    public int available() throws IOException {
        if (this.f181in instanceof BufferInfo) {
            return Math.min(this.f181in.length(), (int) (this.contentLength - this.pos));
        }
        return 0;
    }

    public int read() throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        } else if (this.pos >= this.contentLength) {
            return -1;
        } else {
            int b = this.f181in.read();
            if (b != -1) {
                this.pos++;
                return b;
            } else if (this.pos >= this.contentLength) {
                return b;
            } else {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.pos);
            }
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        } else if (this.pos >= this.contentLength) {
            return -1;
        } else {
            int chunk = len;
            if (this.pos + ((long) len) > this.contentLength) {
                chunk = (int) (this.contentLength - this.pos);
            }
            int count = this.f181in.read(b, off, chunk);
            if (count == -1 && this.pos < this.contentLength) {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.pos);
            } else if (count <= 0) {
                return count;
            } else {
                this.pos += (long) count;
                return count;
            }
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public long skip(long n) throws IOException {
        int l;
        if (n <= 0) {
            return 0;
        }
        byte[] buffer = new byte[2048];
        long remaining = Math.min(n, this.contentLength - this.pos);
        long count = 0;
        while (remaining > 0 && (l = read(buffer, 0, (int) Math.min(2048, remaining))) != -1) {
            count += (long) l;
            remaining -= (long) l;
        }
        return count;
    }
}
