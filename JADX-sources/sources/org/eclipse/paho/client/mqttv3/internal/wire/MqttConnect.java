package org.eclipse.paho.client.mqttv3.internal.wire;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttConnect extends MqttWireMessage {
    public static final String KEY = "Con";
    private int MqttVersion;
    private boolean cleanSession;
    private String clientId;
    private int keepAliveInterval;
    private char[] password;
    private String userName;
    private String willDestination;
    private MqttMessage willMessage;

    public MqttConnect(byte info, byte[] data) throws IOException, MqttException {
        super((byte) 1);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        decodeUTF8(dis);
        dis.readByte();
        dis.readByte();
        this.keepAliveInterval = dis.readUnsignedShort();
        this.clientId = decodeUTF8(dis);
        dis.close();
    }

    public MqttConnect(String clientId, int MqttVersion, boolean cleanSession, int keepAliveInterval, String userName, char[] password, MqttMessage willMessage, String willDestination) {
        super((byte) 1);
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.keepAliveInterval = keepAliveInterval;
        this.userName = userName;
        this.password = password;
        this.willMessage = willMessage;
        this.willDestination = willDestination;
        this.MqttVersion = MqttVersion;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public String toString() {
        String rc = super.toString();
        return String.valueOf(rc) + " clientId " + this.clientId + " keepAliveInterval " + this.keepAliveInterval;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte getMessageInfo() {
        return (byte) 0;
    }

    public boolean isCleanSession() {
        return this.cleanSession;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    protected byte[] getVariableHeader() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            if (this.MqttVersion == 3) {
                encodeUTF8(dos, "MQIsdp");
            } else if (this.MqttVersion == 4) {
                encodeUTF8(dos, "MQTT");
            }
            dos.write(this.MqttVersion);
            byte connectFlags = 0;
            if (this.cleanSession) {
                connectFlags = (byte) 2;
            }
            if (this.willMessage != null) {
                connectFlags = (byte) ((this.willMessage.getQos() << 3) | ((byte) (connectFlags | 4)));
                if (this.willMessage.isRetained()) {
                    connectFlags = (byte) (connectFlags | 32);
                }
            }
            if (this.userName != null) {
                connectFlags = (byte) (connectFlags | AnyoMessage.CMD_RESET_CHARGE);
                if (this.password != null) {
                    connectFlags = (byte) (connectFlags | AnyoMessage.CMD_QUERY_CHARGE_SETTING);
                }
            }
            dos.write(connectFlags);
            dos.writeShort(this.keepAliveInterval);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new MqttException(ioe);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public byte[] getPayload() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            encodeUTF8(dos, this.clientId);
            if (this.willMessage != null) {
                encodeUTF8(dos, this.willDestination);
                dos.writeShort(this.willMessage.getPayload().length);
                dos.write(this.willMessage.getPayload());
            }
            if (this.userName != null) {
                encodeUTF8(dos, this.userName);
                if (this.password != null) {
                    encodeUTF8(dos, new String(this.password));
                }
            }
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public boolean isMessageIdRequired() {
        return false;
    }

    @Override // org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage
    public String getKey() {
        return "Con";
    }
}
