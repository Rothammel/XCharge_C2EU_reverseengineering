package org.apache.mina.handler.stream;

import java.io.IOException;
import java.io.InputStream;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
class IoSessionInputStream extends InputStream {
    private volatile boolean closed;
    private IOException exception;
    private volatile boolean released;
    private final Object mutex = new Object();
    private final IoBuffer buf = IoBuffer.allocate(16);

    public IoSessionInputStream() {
        this.buf.setAutoExpand(true);
        this.buf.limit(0);
    }

    @Override // java.io.InputStream
    public int available() {
        int remaining;
        if (this.released) {
            return 0;
        }
        synchronized (this.mutex) {
            remaining = this.buf.remaining();
        }
        return remaining;
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (!this.closed) {
            synchronized (this.mutex) {
                this.closed = true;
                releaseBuffer();
                this.mutex.notifyAll();
            }
        }
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        int i;
        synchronized (this.mutex) {
            i = !waitForData() ? -1 : this.buf.get() & 255;
        }
        return i;
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes;
        synchronized (this.mutex) {
            if (!waitForData()) {
                readBytes = -1;
            } else {
                if (len > this.buf.remaining()) {
                    readBytes = this.buf.remaining();
                } else {
                    readBytes = len;
                }
                this.buf.get(b, off, readBytes);
            }
        }
        return readBytes;
    }

    private boolean waitForData() throws IOException {
        if (this.released) {
            return false;
        }
        synchronized (this.mutex) {
            while (!this.released && this.buf.remaining() == 0 && this.exception == null) {
                try {
                    this.mutex.wait();
                } catch (InterruptedException e) {
                    IOException ioe = new IOException("Interrupted while waiting for more data");
                    ioe.initCause(e);
                    throw ioe;
                }
            }
        }
        if (this.exception != null) {
            releaseBuffer();
            throw this.exception;
        } else if (this.closed && this.buf.remaining() == 0) {
            releaseBuffer();
            return false;
        } else {
            return true;
        }
    }

    private void releaseBuffer() {
        if (!this.released) {
            this.released = true;
        }
    }

    public void write(IoBuffer src) {
        synchronized (this.mutex) {
            if (!this.closed) {
                if (this.buf.hasRemaining()) {
                    this.buf.compact();
                    this.buf.put(src);
                    this.buf.flip();
                } else {
                    this.buf.clear();
                    this.buf.put(src);
                    this.buf.flip();
                    this.mutex.notifyAll();
                }
            }
        }
    }

    public void throwException(IOException e) {
        synchronized (this.mutex) {
            if (this.exception == null) {
                this.exception = e;
                this.mutex.notifyAll();
            }
        }
    }
}