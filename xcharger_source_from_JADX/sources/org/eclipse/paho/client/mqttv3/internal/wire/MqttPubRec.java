package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPubRec extends MqttAck {
    public MqttPubRec(byte info, byte[] data) throws IOException {
        super((byte) 5);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        this.msgId = dis.readUnsignedShort();
        dis.close();
    }

    public MqttPubRec(MqttPublish publish) {
        super((byte) 5);
        this.msgId = publish.getMessageId();
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return encodeMessageId();
    }
}
