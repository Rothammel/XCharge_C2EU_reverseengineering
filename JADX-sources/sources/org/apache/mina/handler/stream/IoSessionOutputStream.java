package org.apache.mina.handler.stream;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
class IoSessionOutputStream extends OutputStream {
    private WriteFuture lastWriteFuture;
    private final IoSession session;

    public IoSessionOutputStream(IoSession session) {
        this.session = session;
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            flush();
        } finally {
            this.session.closeNow().awaitUninterruptibly();
        }
    }

    private void checkClosed() throws IOException {
        if (!this.session.isConnected()) {
            throw new IOException("The session has been closed.");
        }
    }

    private synchronized void write(IoBuffer buf) throws IOException {
        checkClosed();
        WriteFuture future = this.session.write(buf);
        this.lastWriteFuture = future;
    }

    @Override // java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        write(IoBuffer.wrap((byte[]) b.clone(), off, len));
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.put((byte) b);
        buf.flip();
        write(buf);
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public synchronized void flush() throws IOException {
        if (this.lastWriteFuture != null) {
            this.lastWriteFuture.awaitUninterruptibly();
            if (!this.lastWriteFuture.isWritten()) {
                throw new IOException("The bytes could not be written to the session");
            }
        }
    }
}
