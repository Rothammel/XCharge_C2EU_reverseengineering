package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttSubscribe extends MqttWireMessage {
    private int count;
    private String[] names;
    private int[] qos;

    public MqttSubscribe(byte info, byte[] data) throws IOException {
        super((byte) 8);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        this.msgId = dis.readUnsignedShort();
        this.count = 0;
        this.names = new String[10];
        this.qos = new int[10];
        boolean end = false;
        while (!end) {
            try {
                this.names[this.count] = decodeUTF8(dis);
                int[] iArr = this.qos;
                int i = this.count;
                this.count = i + 1;
                iArr[i] = dis.readByte();
            } catch (Exception e) {
                end = true;
            }
        }
        dis.close();
    }

    public MqttSubscribe(String[] names, int[] qos) {
        super((byte) 8);
        this.names = names;
        this.qos = qos;
        if (names.length != qos.length) {
            throw new IllegalArgumentException();
        }
        this.count = names.length;
        for (int i : qos) {
            MqttMessage.validateQos(i);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(" names:[");
        for (int i = 0; i < this.count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(this.names[i]).append("\"");
        }
        sb.append("] qos:[");
        for (int i2 = 0; i2 < this.count; i2++) {
            if (i2 > 0) {
                sb.append(", ");
            }
            sb.append(this.qos[i2]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte getMessageInfo() {
        return (byte) ((this.duplicate ? 8 : 0) | 2);
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte[] getVariableHeader() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(this.msgId);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public byte[] getPayload() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < this.names.length; i++) {
                encodeUTF8(dos, this.names[i]);
                dos.writeByte(this.qos[i]);
            }
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public boolean isRetryable() {
        return true;
    }
}
