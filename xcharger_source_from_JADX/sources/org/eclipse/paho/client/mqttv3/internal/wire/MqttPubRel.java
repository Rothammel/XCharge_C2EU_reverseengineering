package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPubRel extends MqttPersistableWireMessage {
    public MqttPubRel(MqttPubRec pubRec) {
        super((byte) 6);
        setMessageId(pubRec.getMessageId());
    }

    public MqttPubRel(byte info, byte[] data) throws IOException {
        super((byte) 6);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        this.msgId = dis.readUnsignedShort();
        dis.close();
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return encodeMessageId();
    }

    /* access modifiers changed from: protected */
    public byte getMessageInfo() {
        return (byte) ((this.duplicate ? 8 : 0) | 2);
    }

    public String toString() {
        return String.valueOf(super.toString()) + " msgId " + this.msgId;
    }
}
