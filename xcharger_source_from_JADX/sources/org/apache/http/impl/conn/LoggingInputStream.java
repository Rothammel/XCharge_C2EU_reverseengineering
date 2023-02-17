package org.apache.http.impl.conn;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
class LoggingInputStream extends InputStream {

    /* renamed from: in */
    private final InputStream f177in;
    private final WireHC4 wire;

    public LoggingInputStream(InputStream in, WireHC4 wire2) {
        this.f177in = in;
        this.wire = wire2;
    }

    public int read() throws IOException {
        try {
            int b = this.f177in.read();
            if (b == -1) {
                this.wire.input("end of stream");
            } else {
                this.wire.input(b);
            }
            return b;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    public int read(byte[] b) throws IOException {
        try {
            int bytesRead = this.f177in.read(b);
            if (bytesRead == -1) {
                this.wire.input("end of stream");
            } else if (bytesRead > 0) {
                this.wire.input(b, 0, bytesRead);
            }
            return bytesRead;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int bytesRead = this.f177in.read(b, off, len);
            if (bytesRead == -1) {
                this.wire.input("end of stream");
            } else if (bytesRead > 0) {
                this.wire.input(b, off, bytesRead);
            }
            return bytesRead;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    public long skip(long n) throws IOException {
        try {
            return super.skip(n);
        } catch (IOException ex) {
            this.wire.input("[skip] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    public int available() throws IOException {
        try {
            return this.f177in.available();
        } catch (IOException ex) {
            this.wire.input("[available] I/O error : " + ex.getMessage());
            throw ex;
        }
    }

    public void mark(int readlimit) {
        super.mark(readlimit);
    }

    public void reset() throws IOException {
        super.reset();
    }

    public boolean markSupported() {
        return false;
    }

    public void close() throws IOException {
        try {
            this.f177in.close();
        } catch (IOException ex) {
            this.wire.input("[close] I/O error: " + ex.getMessage());
            throw ex;
        }
    }
}
