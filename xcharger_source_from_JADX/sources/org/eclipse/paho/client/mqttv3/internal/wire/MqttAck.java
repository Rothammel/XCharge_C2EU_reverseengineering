package org.eclipse.paho.client.mqttv3.internal.wire;

public abstract class MqttAck extends MqttWireMessage {
    public MqttAck(byte type) {
        super(type);
    }

    /* access modifiers changed from: protected */
    public byte getMessageInfo() {
        return 0;
    }

    public String toString() {
        return String.valueOf(super.toString()) + " msgId " + this.msgId;
    }
}
