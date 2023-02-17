package org.apache.mina.handler.stream;

import java.io.IOException;
import java.io.InputStream;
import org.apache.mina.core.buffer.IoBuffer;

class IoSessionInputStream extends InputStream {
    private final IoBuffer buf = IoBuffer.allocate(16);
    private volatile boolean closed;
    private IOException exception;
    private final Object mutex = new Object();
    private volatile boolean released;

    public IoSessionInputStream() {
        this.buf.setAutoExpand(true);
        this.buf.limit(0);
    }

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

    public void close() {
        if (!this.closed) {
            synchronized (this.mutex) {
                this.closed = true;
                releaseBuffer();
                this.mutex.notifyAll();
            }
        }
    }

    public int read() throws IOException {
        byte b;
        synchronized (this.mutex) {
            if (!waitForData()) {
                b = -1;
            } else {
                b = this.buf.get() & 255;
            }
        }
        return b;
    }

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
        } else if (!this.closed || this.buf.remaining() != 0) {
            return true;
        } else {
            releaseBuffer();
            return false;
        }
    }

    private void releaseBuffer() {
        if (!this.released) {
            this.released = true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(org.apache.mina.core.buffer.IoBuffer r3) {
        /*
            r2 = this;
            java.lang.Object r1 = r2.mutex
            monitor-enter(r1)
            boolean r0 = r2.closed     // Catch:{ all -> 0x0022 }
            if (r0 == 0) goto L_0x0009
            monitor-exit(r1)     // Catch:{ all -> 0x0022 }
        L_0x0008:
            return
        L_0x0009:
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            boolean r0 = r0.hasRemaining()     // Catch:{ all -> 0x0022 }
            if (r0 == 0) goto L_0x0025
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.compact()     // Catch:{ all -> 0x0022 }
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.put((org.apache.mina.core.buffer.IoBuffer) r3)     // Catch:{ all -> 0x0022 }
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.flip()     // Catch:{ all -> 0x0022 }
        L_0x0020:
            monitor-exit(r1)     // Catch:{ all -> 0x0022 }
            goto L_0x0008
        L_0x0022:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0022 }
            throw r0
        L_0x0025:
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.clear()     // Catch:{ all -> 0x0022 }
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.put((org.apache.mina.core.buffer.IoBuffer) r3)     // Catch:{ all -> 0x0022 }
            org.apache.mina.core.buffer.IoBuffer r0 = r2.buf     // Catch:{ all -> 0x0022 }
            r0.flip()     // Catch:{ all -> 0x0022 }
            java.lang.Object r0 = r2.mutex     // Catch:{ all -> 0x0022 }
            r0.notifyAll()     // Catch:{ all -> 0x0022 }
            goto L_0x0020
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.handler.stream.IoSessionInputStream.write(org.apache.mina.core.buffer.IoBuffer):void");
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
