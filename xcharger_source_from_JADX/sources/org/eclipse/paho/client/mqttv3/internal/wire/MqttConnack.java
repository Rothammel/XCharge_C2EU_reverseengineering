package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttConnack extends MqttAck {
    public static final String KEY = "Con";
    private int returnCode;
    private boolean sessionPresent;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MqttConnack(byte info, byte[] variableHeader) throws IOException {
        super((byte) 2);
        boolean z = true;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(variableHeader));
        this.sessionPresent = (dis.readUnsignedByte() & 1) != 1 ? false : z;
        this.returnCode = dis.readUnsignedByte();
        dis.close();
    }

    public int getReturnCode() {
        return this.returnCode;
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return new byte[0];
    }

    public boolean isMessageIdRequired() {
        return false;
    }

    public String getKey() {
        return "Con";
    }

    public String toString() {
        return String.valueOf(super.toString()) + " session present:" + this.sessionPresent + " return code: " + this.returnCode;
    }

    public boolean getSessionPresent() {
        return this.sessionPresent;
    }
}
