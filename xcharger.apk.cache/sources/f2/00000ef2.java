package org.eclipse.paho.client.mqttv3.internal.wire;

/* loaded from: classes.dex */
public class MultiByteInteger {
    private int length;
    private long value;

    public MultiByteInteger(long value) {
        this(value, -1);
    }

    public MultiByteInteger(long value, int length) {
        this.value = value;
        this.length = length;
    }

    public int getEncodedLength() {
        return this.length;
    }

    public long getValue() {
        return this.value;
    }
}