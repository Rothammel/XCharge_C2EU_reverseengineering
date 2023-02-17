package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

/* loaded from: classes.dex */
public class MqttPubComp extends MqttAck {
    public MqttPubComp(byte info, byte[] data) throws IOException {
        super((byte) 7);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
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

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte[] getVariableHeader() throws MqttException {
        return encodeMessageId();
    }
}