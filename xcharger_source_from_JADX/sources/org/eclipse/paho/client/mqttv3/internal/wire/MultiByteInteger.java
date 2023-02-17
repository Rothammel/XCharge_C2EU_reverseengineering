package org.eclipse.paho.client.mqttv3.internal.wire;

public class MultiByteInteger {
    private int length;
    private long value;

    public MultiByteInteger(long value2) {
        this(value2, -1);
    }

    public MultiByteInteger(long value2, int length2) {
        this.value = value2;
        this.length = length2;
    }

    public int getEncodedLength() {
        return this.length;
    }

    public long getValue() {
        return this.value;
    }
}
