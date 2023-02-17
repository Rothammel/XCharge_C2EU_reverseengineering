package it.sauronsoftware.ftp4j.connectors;

import android.support.v4.view.MotionEventCompat;
import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes.dex */
class Base64InputStream extends InputStream {
    private int[] buffer;
    private int bufferCounter = 0;
    private boolean eof = false;
    private InputStream inputStream;

    public Base64InputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this.buffer == null || this.bufferCounter == this.buffer.length) {
            if (this.eof) {
                return -1;
            }
            acquire();
            if (this.buffer.length == 0) {
                this.buffer = null;
                return -1;
            }
            this.bufferCounter = 0;
        }
        int[] iArr = this.buffer;
        int i = this.bufferCounter;
        this.bufferCounter = i + 1;
        return iArr[i];
    }

    private void acquire() throws IOException {
        int l;
        char[] four = new char[4];
        int i = 0;
        do {
            int b = this.inputStream.read();
            if (b == -1) {
                if (i != 0) {
                    throw new IOException("Bad base64 stream");
                }
                this.buffer = new int[0];
                this.eof = true;
                return;
            }
            char c = (char) b;
            if (Base64.chars.indexOf(c) != -1 || c == Base64.pad) {
                four[i] = c;
                i++;
                continue;
            } else if (c != '\r' && c != '\n') {
                throw new IOException("Bad base64 stream");
            }
        } while (i < 4);
        boolean padded = false;
        for (int i2 = 0; i2 < 4; i2++) {
            if (four[i2] != Base64.pad) {
                if (padded) {
                    throw new IOException("Bad base64 stream");
                }
            } else if (!padded) {
                padded = true;
            }
        }
        if (four[3] == Base64.pad) {
            if (this.inputStream.read() != -1) {
                throw new IOException("Bad base64 stream");
            }
            this.eof = true;
            if (four[2] == Base64.pad) {
                l = 1;
            } else {
                l = 2;
            }
        } else {
            l = 3;
        }
        int aux = 0;
        for (int i3 = 0; i3 < 4; i3++) {
            if (four[i3] != Base64.pad) {
                aux |= Base64.chars.indexOf(four[i3]) << ((3 - i3) * 6);
            }
        }
        this.buffer = new int[l];
        for (int i4 = 0; i4 < l; i4++) {
            this.buffer[i4] = (aux >>> ((2 - i4) * 8)) & MotionEventCompat.ACTION_MASK;
        }
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.inputStream.close();
    }
}