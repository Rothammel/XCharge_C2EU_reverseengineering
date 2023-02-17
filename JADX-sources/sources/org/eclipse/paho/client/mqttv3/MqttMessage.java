package org.eclipse.paho.client.mqttv3;

/* loaded from: classes.dex */
public class MqttMessage {
    private int messageId;
    private byte[] payload;
    private boolean mutable = true;
    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;

    public static void validateQos(int qos) {
        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException();
        }
    }

    public MqttMessage() {
        setPayload(new byte[0]);
    }

    public MqttMessage(byte[] payload) {
        setPayload(payload);
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void clearPayload() {
        checkMutable();
        this.payload = new byte[0];
    }

    public void setPayload(byte[] payload) {
        checkMutable();
        if (payload == null) {
            throw new NullPointerException();
        }
        this.payload = payload;
    }

    public boolean isRetained() {
        return this.retained;
    }

    public void setRetained(boolean retained) {
        checkMutable();
        this.retained = retained;
    }

    public int getQos() {
        return this.qos;
    }

    public void setQos(int qos) {
        checkMutable();
        validateQos(qos);
        this.qos = qos;
    }

    public String toString() {
        return new String(this.payload);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    protected void checkMutable() throws IllegalStateException {
        if (!this.mutable) {
            throw new IllegalStateException();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setDuplicate(boolean dup) {
        this.dup = dup;
    }

    public boolean isDuplicate() {
        return this.dup;
    }

    public void setId(int messageId) {
        this.messageId = messageId;
    }

    public int getId() {
        return this.messageId;
    }
}
