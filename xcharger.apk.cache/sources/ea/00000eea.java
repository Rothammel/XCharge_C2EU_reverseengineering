package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.lang3.CharEncoding;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttPublish extends MqttPersistableWireMessage {
    private byte[] encodedPayload;
    private MqttMessage message;
    private String topicName;

    public MqttPublish(String name, MqttMessage message) {
        super((byte) 3);
        this.encodedPayload = null;
        this.topicName = name;
        this.message = message;
    }

    public MqttPublish(byte info, byte[] data) throws MqttException, IOException {
        super((byte) 3);
        this.encodedPayload = null;
        this.message = new MqttReceivedMessage();
        this.message.setQos((info >> 1) & 3);
        if ((info & 1) == 1) {
            this.message.setRetained(true);
        }
        if ((info & 8) == 8) {
            ((MqttReceivedMessage) this.message).setDuplicate(true);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CountingInputStream counter = new CountingInputStream(bais);
        DataInputStream dis = new DataInputStream(counter);
        this.topicName = decodeUTF8(dis);
        if (this.message.getQos() > 0) {
            this.msgId = dis.readUnsignedShort();
        }
        byte[] payload = new byte[data.length - counter.getCounter()];
        dis.readFully(payload);
        dis.close();
        this.message.setPayload(payload);
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public String toString() {
        String string;
        StringBuffer hex = new StringBuffer();
        byte[] payload = this.message.getPayload();
        int limit = Math.min(payload.length, 20);
        for (int i = 0; i < limit; i++) {
            byte b = payload[i];
            String ch = Integer.toHexString(b);
            if (ch.length() == 1) {
                ch = "0" + ch;
            }
            hex.append(ch);
        }
        try {
            string = new String(payload, 0, limit, CharEncoding.UTF_8);
        } catch (Exception e) {
            string = "?";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(" qos:").append(this.message.getQos());
        if (this.message.getQos() > 0) {
            sb.append(" msgId:").append(this.msgId);
        }
        sb.append(" retained:").append(this.message.isRetained());
        sb.append(" dup:").append(this.duplicate);
        sb.append(" topic:\"").append(this.topicName).append("\"");
        sb.append(" payload:[hex:").append(hex);
        sb.append(" utf8:\"").append(string).append("\"");
        sb.append(" length:").append(payload.length).append("]");
        return sb.toString();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte getMessageInfo() {
        byte info = (byte) (this.message.getQos() << 1);
        if (this.message.isRetained()) {
            info = (byte) (info | 1);
        }
        if (this.message.isDuplicate() || this.duplicate) {
            return (byte) (info | 8);
        }
        return info;
    }

    public String getTopicName() {
        return this.topicName;
    }

    public MqttMessage getMessage() {
        return this.message;
    }

    protected static byte[] encodePayload(MqttMessage message) {
        return message.getPayload();
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public byte[] getPayload() throws MqttException {
        if (this.encodedPayload == null) {
            this.encodedPayload = encodePayload(this.message);
        }
        return this.encodedPayload;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttPersistableWireMessage, org.eclipse.paho.client.mqttv3.MqttPersistable
    public int getPayloadLength() {
        try {
            int length = getPayload().length;
            return length;
        } catch (MqttException e) {
            return 0;
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public void setMessageId(int msgId) {
        super.setMessageId(msgId);
        if (this.message instanceof MqttReceivedMessage) {
            ((MqttReceivedMessage) this.message).setMessageId(msgId);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte[] getVariableHeader() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            encodeUTF8(dos, this.topicName);
            if (this.message.getQos() > 0) {
                dos.writeShort(this.msgId);
            }
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public boolean isMessageIdRequired() {
        return true;
    }
}