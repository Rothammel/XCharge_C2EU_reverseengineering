package org.eclipse.paho.client.mqttv3;

/* loaded from: classes.dex */
public interface IMqttMessageListener {
    void messageArrived(String str, MqttMessage mqttMessage) throws Exception;
}
