package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/* loaded from: classes.dex */
public class DeflateInputStream extends InputStream {
    private InputStream sourceStream;

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0032, code lost:
        throw new java.io.IOException("Unable to read the response");
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x006c, code lost:
        r6.unread(r5, 0, r2);
        r10.sourceStream = new org.apache.http.client.entity.DeflateInputStream.DeflateStream(r6, new java.util.zip.Inflater());
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x0029, code lost:
        if (r4 != (-1)) goto L25;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public DeflateInputStream(java.io.InputStream r11) throws java.io.IOException {
        /*
            r10 = this;
            r9 = 1
            r8 = -1
            r10.<init>()
            r7 = 6
            byte[] r5 = new byte[r7]
            java.io.PushbackInputStream r6 = new java.io.PushbackInputStream
            int r7 = r5.length
            r6.<init>(r11, r7)
            int r2 = r6.read(r5)
            if (r2 != r8) goto L1c
            java.io.IOException r7 = new java.io.IOException
            java.lang.String r8 = "Unable to read the response"
            r7.<init>(r8)
            throw r7
        L1c:
            byte[] r0 = new byte[r9]
            java.util.zip.Inflater r3 = new java.util.zip.Inflater
            r3.<init>()
        L23:
            int r4 = r3.inflate(r0)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            if (r4 == 0) goto L49
        L29:
            if (r4 != r8) goto L6c
            java.io.IOException r7 = new java.io.IOException     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            java.lang.String r8 = "Unable to read the response"
            r7.<init>(r8)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            throw r7     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
        L33:
            r1 = move-exception
            r7 = 0
            r6.unread(r5, r7, r2)     // Catch: java.lang.Throwable -> L57
            org.apache.http.client.entity.DeflateInputStream$DeflateStream r7 = new org.apache.http.client.entity.DeflateInputStream$DeflateStream     // Catch: java.lang.Throwable -> L57
            java.util.zip.Inflater r8 = new java.util.zip.Inflater     // Catch: java.lang.Throwable -> L57
            r9 = 1
            r8.<init>(r9)     // Catch: java.lang.Throwable -> L57
            r7.<init>(r6, r8)     // Catch: java.lang.Throwable -> L57
            r10.sourceStream = r7     // Catch: java.lang.Throwable -> L57
            r3.end()
        L48:
            return
        L49:
            boolean r7 = r3.finished()     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            if (r7 == 0) goto L5c
            java.io.IOException r7 = new java.io.IOException     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            java.lang.String r8 = "Unable to read the response"
            r7.<init>(r8)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            throw r7     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
        L57:
            r7 = move-exception
            r3.end()
            throw r7
        L5c:
            boolean r7 = r3.needsDictionary()     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            if (r7 != 0) goto L29
            boolean r7 = r3.needsInput()     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            if (r7 == 0) goto L23
            r3.setInput(r5)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            goto L23
        L6c:
            r7 = 0
            r6.unread(r5, r7, r2)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            org.apache.http.client.entity.DeflateInputStream$DeflateStream r7 = new org.apache.http.client.entity.DeflateInputStream$DeflateStream     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            java.util.zip.Inflater r8 = new java.util.zip.Inflater     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            r8.<init>()     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            r7.<init>(r6, r8)     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            r10.sourceStream = r7     // Catch: java.util.zip.DataFormatException -> L33 java.lang.Throwable -> L57
            r3.end()
            goto L48
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.client.entity.DeflateInputStream.<init>(java.io.InputStream):void");
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        return this.sourceStream.read();
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        return this.sourceStream.read(b);
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        return this.sourceStream.read(b, off, len);
    }

    @Override // java.io.InputStream
    public long skip(long n) throws IOException {
        return this.sourceStream.skip(n);
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return this.sourceStream.available();
    }

    @Override // java.io.InputStream
    public void mark(int readLimit) {
        this.sourceStream.mark(readLimit);
    }

    @Override // java.io.InputStream
    public void reset() throws IOException {
        this.sourceStream.reset();
    }

    @Override // java.io.InputStream
    public boolean markSupported() {
        return this.sourceStream.markSupported();
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.sourceStream.close();
    }

    /* loaded from: classes.dex */
    static class DeflateStream extends InflaterInputStream {
        private boolean closed;

        public DeflateStream(InputStream in, Inflater inflater) {
            super(in, inflater);
            this.closed = false;
        }

        @Override // java.util.zip.InflaterInputStream, java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                this.inf.end();
                super.close();
            }
        }
    }
}