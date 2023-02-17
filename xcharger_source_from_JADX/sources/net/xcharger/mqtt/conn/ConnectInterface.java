package net.xcharger.mqtt.conn;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;

public interface ConnectInterface {
    IMqttAsyncClient mqttAsyncConnect(String str, String str2) throws Exception;

    MqttClient mqttConnect(String str, String str2) throws Exception;
}
