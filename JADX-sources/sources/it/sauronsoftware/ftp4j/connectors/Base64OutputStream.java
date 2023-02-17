package it.sauronsoftware.ftp4j.connectors;

import android.support.v4.view.MotionEventCompat;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

/* loaded from: classes.dex */
class Base64OutputStream extends OutputStream {
    private int buffer;
    private int bytecounter;
    private int linecounter;
    private int linelength;
    private OutputStream outputStream;

    public Base64OutputStream(OutputStream outputStream) {
        this(outputStream, 76);
    }

    public Base64OutputStream(OutputStream outputStream, int wrapAt) {
        this.outputStream = null;
        this.buffer = 0;
        this.bytecounter = 0;
        this.linecounter = 0;
        this.linelength = 0;
        this.outputStream = outputStream;
        this.linelength = wrapAt;
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        int value = (b & MotionEventCompat.ACTION_MASK) << (16 - (this.bytecounter * 8));
        this.buffer |= value;
        this.bytecounter++;
        if (this.bytecounter == 3) {
            commit();
        }
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        commit();
        this.outputStream.close();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void commit() throws IOException {
        if (this.bytecounter > 0) {
            if (this.linelength > 0 && this.linecounter == this.linelength) {
                this.outputStream.write(HttpProxyConstants.CRLF.getBytes());
                this.linecounter = 0;
            }
            char b1 = Base64.chars.charAt((this.buffer << 8) >>> 26);
            char b2 = Base64.chars.charAt((this.buffer << 14) >>> 26);
            char b3 = this.bytecounter < 2 ? Base64.pad : Base64.chars.charAt((this.buffer << 20) >>> 26);
            char b4 = this.bytecounter < 3 ? Base64.pad : Base64.chars.charAt((this.buffer << 26) >>> 26);
            this.outputStream.write(b1);
            this.outputStream.write(b2);
            this.outputStream.write(b3);
            this.outputStream.write(b4);
            this.linecounter += 4;
            this.bytecounter = 0;
            this.buffer = 0;
        }
    }
}
