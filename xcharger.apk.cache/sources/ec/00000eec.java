package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

/* loaded from: classes.dex */
public class MqttSuback extends MqttAck {
    private int[] grantedQos;

    public MqttSuback(byte info, byte[] data) throws IOException {
        super((byte) 9);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        this.msgId = dis.readUnsignedShort();
        int index = 0;
        this.grantedQos = new int[data.length - 2];
        for (int qos = dis.read(); qos != -1; qos = dis.read()) {
            this.grantedQos[index] = qos;
            index++;
        }
        dis.close();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte[] getVariableHeader() throws MqttException {
        return new byte[0];
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttAck, org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString()).append(" granted Qos");
        for (int i = 0; i < this.grantedQos.length; i++) {
            sb.append(StringUtils.SPACE).append(this.grantedQos[i]);
        }
        return sb.toString();
    }

    public int[] getGrantedQos() {
        return this.grantedQos;
    }
}