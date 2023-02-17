package org.eclipse.paho.client.mqttv3.internal.wire;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttReceivedMessage extends MqttMessage {
    public void setMessageId(int msgId) {
        super.setId(msgId);
    }

    public int getMessageId() {
        return super.getId();
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttMessage
    public void setDuplicate(boolean value) {
        super.setDuplicate(value);
    }
}