package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.InputStream;

public class MultiByteArrayInputStream extends InputStream {
    private byte[] bytesA;
    private byte[] bytesB;
    private int lengthA;
    private int lengthB;
    private int offsetA;
    private int offsetB;
    private int pos = 0;

    public MultiByteArrayInputStream(byte[] bytesA2, int offsetA2, int lengthA2, byte[] bytesB2, int offsetB2, int lengthB2) {
        this.bytesA = bytesA2;
        this.bytesB = bytesB2;
        this.offsetA = offsetA2;
        this.offsetB = offsetB2;
        this.lengthA = lengthA2;
        this.lengthB = lengthB2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: byte} */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() throws java.io.IOException {
        /*
            r4 = this;
            r0 = -1
            int r1 = r4.pos
            int r2 = r4.lengthA
            if (r1 >= r2) goto L_0x001c
            byte[] r1 = r4.bytesA
            int r2 = r4.offsetA
            int r3 = r4.pos
            int r2 = r2 + r3
            byte r0 = r1[r2]
        L_0x0010:
            if (r0 >= 0) goto L_0x0014
            int r0 = r0 + 256
        L_0x0014:
            int r1 = r4.pos
            int r1 = r1 + 1
            r4.pos = r1
            r1 = r0
        L_0x001b:
            return r1
        L_0x001c:
            int r1 = r4.pos
            int r2 = r4.lengthA
            int r3 = r4.lengthB
            int r2 = r2 + r3
            if (r1 >= r2) goto L_0x0032
            byte[] r1 = r4.bytesB
            int r2 = r4.offsetB
            int r3 = r4.pos
            int r2 = r2 + r3
            int r3 = r4.lengthA
            int r2 = r2 - r3
            byte r0 = r1[r2]
            goto L_0x0010
        L_0x0032:
            r1 = -1
            goto L_0x001b
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.wire.MultiByteArrayInputStream.read():int");
    }
}
