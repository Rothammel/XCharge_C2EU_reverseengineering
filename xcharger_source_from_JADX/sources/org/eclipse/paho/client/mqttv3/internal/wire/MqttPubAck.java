package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPubAck extends MqttAck {
    public MqttPubAck(byte info, byte[] data) throws IOException {
        super((byte) 4);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        this.msgId = dis.readUnsignedShort();
        dis.close();
    }

    public MqttPubAck(MqttPublish publish) {
        super((byte) 4);
        this.msgId = publish.getMessageId();
    }

    public MqttPubAck(int messageId) {
        super((byte) 4);
        this.msgId = messageId;
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return encodeMessageId();
    }
}
