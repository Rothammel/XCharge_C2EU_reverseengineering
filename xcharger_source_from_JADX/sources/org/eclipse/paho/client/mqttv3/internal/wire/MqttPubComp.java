package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPubComp extends MqttAck {
    public MqttPubComp(byte info, byte[] data) throws IOException {
        super((byte) 7);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        this.msgId = dis.readUnsignedShort();
        dis.close();
    }

    public MqttPubComp(MqttPublish publish) {
        super((byte) 7);
        this.msgId = publish.getMessageId();
    }

    public MqttPubComp(int msgId) {
        super((byte) 7);
        this.msgId = msgId;
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return encodeMessageId();
    }
}
