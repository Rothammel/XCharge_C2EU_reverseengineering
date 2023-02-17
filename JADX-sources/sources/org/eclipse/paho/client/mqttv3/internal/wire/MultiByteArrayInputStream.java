package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes.dex */
public class MultiByteArrayInputStream extends InputStream {
    private byte[] bytesA;
    private byte[] bytesB;
    private int lengthA;
    private int lengthB;
    private int offsetA;
    private int offsetB;
    private int pos = 0;

    public MultiByteArrayInputStream(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB) {
        this.bytesA = bytesA;
        this.bytesB = bytesB;
        this.offsetA = offsetA;
        this.offsetB = offsetB;
        this.lengthA = lengthA;
        this.lengthB = lengthB;
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        int result;
        if (this.pos < this.lengthA) {
            result = this.bytesA[this.offsetA + this.pos];
        } else if (this.pos < this.lengthA + this.lengthB) {
            result = this.bytesB[(this.offsetB + this.pos) - this.lengthA];
        } else {
            return -1;
        }
        if (result < 0) {
            result += 256;
        }
        this.pos++;
        return result;
    }
}
