package net.xcharger.mqtt.conn;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;

/* loaded from: classes.dex */
public class ConnectFactory {
    public MqttClient conn(String clientid, String credential) throws Exception {
        return new MqttConnect().getMqttConnect(clientid, credential);
    }

    public boolean stop() {
        return MqttConnect.doStop();
    }

    public boolean disConnect() throws Exception {
        return MqttConnect.clearClient();
    }

    public MqttAsyncClient asyncConn(String clientid, String credential) throws Exception {
        return new MqttConnect().mqttAsyncConnect(clientid, credential);
    }

    public MqttClient getConn() {
        new MqttConnect();
        return MqttConnect.getClient();
    }
}