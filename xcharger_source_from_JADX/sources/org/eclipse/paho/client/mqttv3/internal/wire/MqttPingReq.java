package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPingReq extends MqttWireMessage {
    public static final String KEY = "Ping";

    public MqttPingReq() {
        super((byte) 12);
    }

    public MqttPingReq(byte info, byte[] variableHeader) throws IOException {
        super((byte) 12);
    }

    public boolean isMessageIdRequired() {
        return false;
    }

    /* access modifiers changed from: protected */
    public byte[] getVariableHeader() throws MqttException {
        return new byte[0];
    }

    /* access modifiers changed from: protected */
    public byte getMessageInfo() {
        return 0;
    }

    public String getKey() {
        return "Ping";
    }
}
