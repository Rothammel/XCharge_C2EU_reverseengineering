package org.eclipse.paho.client.mqttv3;

public class MqttMessage {
    private boolean dup = false;
    private int messageId;
    private boolean mutable = true;
    private byte[] payload;
    private int qos = 1;
    private boolean retained = false;

    public static void validateQos(int qos2) {
        if (qos2 < 0 || qos2 > 2) {
            throw new IllegalArgumentException();
        }
    }

    public MqttMessage() {
        setPayload(new byte[0]);
    }

    public MqttMessage(byte[] payload2) {
        setPayload(payload2);
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void clearPayload() {
        checkMutable();
        this.payload = new byte[0];
    }

    public void setPayload(byte[] payload2) {
        checkMutable();
        if (payload2 == null) {
            throw new NullPointerException();
        }
        this.payload = payload2;
    }

    public boolean isRetained() {
        return this.retained;
    }

    public void setRetained(boolean retained2) {
        checkMutable();
        this.retained = retained2;
    }

    public int getQos() {
        return this.qos;
    }

    public void setQos(int qos2) {
        checkMutable();
        validateQos(qos2);
        this.qos = qos2;
    }

    public String toString() {
        return new String(this.payload);
    }

    /* access modifiers changed from: protected */
    public void setMutable(boolean mutable2) {
        this.mutable = mutable2;
    }

    /* access modifiers changed from: protected */
    public void checkMutable() throws IllegalStateException {
        if (!this.mutable) {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: protected */
    public void setDuplicate(boolean dup2) {
        this.dup = dup2;
    }

    public boolean isDuplicate() {
        return this.dup;
    }

    public void setId(int messageId2) {
        this.messageId = messageId2;
    }

    public int getId() {
        return this.messageId;
    }
}
