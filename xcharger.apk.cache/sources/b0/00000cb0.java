package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.TruncatedChunkException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.BufferInfo;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
/* loaded from: classes.dex */
public class ChunkedInputStreamHC4 extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private static final int CHUNK_CRLF = 3;
    private static final int CHUNK_DATA = 2;
    private static final int CHUNK_LEN = 1;
    private int chunkSize;
    private final SessionInputBuffer in;
    private boolean eof = false;
    private boolean closed = false;
    private Header[] footers = new Header[0];
    private int pos = 0;
    private final CharArrayBuffer buffer = new CharArrayBuffer(16);
    private int state = 1;

    public ChunkedInputStreamHC4(SessionInputBuffer in) {
        this.in = (SessionInputBuffer) Args.notNull(in, "Session input buffer");
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        if (this.in instanceof BufferInfo) {
            int len = this.in.length();
            return Math.min(len, this.chunkSize - this.pos);
        }
        return 0;
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (this.eof) {
            return -1;
        }
        if (this.state != 2) {
            nextChunk();
            if (this.eof) {
                return -1;
            }
        }
        int b = this.in.read();
        if (b != -1) {
            this.pos++;
            if (this.pos >= this.chunkSize) {
                this.state = 3;
                return b;
            }
            return b;
        }
        return b;
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (this.eof) {
            return -1;
        }
        if (this.state != 2) {
            nextChunk();
            if (this.eof) {
                return -1;
            }
        }
        int bytesRead = this.in.read(b, off, Math.min(len, this.chunkSize - this.pos));
        if (bytesRead != -1) {
            this.pos += bytesRead;
            if (this.pos >= this.chunkSize) {
                this.state = 3;
                return bytesRead;
            }
            return bytesRead;
        }
        this.eof = true;
        throw new TruncatedChunkException("Truncated chunk ( expected size: " + this.chunkSize + "; actual size: " + this.pos + ")");
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    private void nextChunk() throws IOException {
        this.chunkSize = getChunkSize();
        if (this.chunkSize < 0) {
            throw new MalformedChunkCodingException("Negative chunk size");
        }
        this.state = 2;
        this.pos = 0;
        if (this.chunkSize == 0) {
            this.eof = true;
            parseTrailerHeaders();
        }
    }

    private int getChunkSize() throws IOException {
        int st = this.state;
        switch (st) {
            case 1:
                break;
            case 2:
            default:
                throw new IllegalStateException("Inconsistent codec state");
            case 3:
                this.buffer.clear();
                int bytesRead1 = this.in.readLine(this.buffer);
                if (bytesRead1 != -1) {
                    if (!this.buffer.isEmpty()) {
                        throw new MalformedChunkCodingException("Unexpected content at the end of chunk");
                    }
                    this.state = 1;
                    break;
                } else {
                    return 0;
                }
        }
        this.buffer.clear();
        int bytesRead2 = this.in.readLine(this.buffer);
        if (bytesRead2 != -1) {
            int separator = this.buffer.indexOf(59);
            if (separator < 0) {
                separator = this.buffer.length();
            }
            try {
                return Integer.parseInt(this.buffer.substringTrimmed(0, separator), 16);
            } catch (NumberFormatException e) {
                throw new MalformedChunkCodingException("Bad chunk header");
            }
        }
        return 0;
    }

    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParserHC4.parseHeaders(this.in, -1, -1, null);
        } catch (HttpException e) {
            MalformedChunkCodingException malformedChunkCodingException = new MalformedChunkCodingException("Invalid footer: " + e.getMessage());
            malformedChunkCodingException.initCause(e);
            throw malformedChunkCodingException;
        }
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.closed) {
            try {
                if (!this.eof) {
                    byte[] buff = new byte[2048];
                    do {
                    } while (read(buff) >= 0);
                }
            } finally {
                this.eof = true;
                this.closed = true;
            }
        }
    }

    public Header[] getFooters() {
        return (Header[]) this.footers.clone();
    }
}