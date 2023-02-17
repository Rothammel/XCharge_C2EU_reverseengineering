package net.xcharger.mqtt.device;

import net.xcharger.mqtt.MqttKit;
import net.xcharger.sdk.device.MessageProxyException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SubThread extends Thread {
    public void run() {
        try {
            MqttKit.subscribe();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (MessageProxyException e2) {
            e2.printStackTrace();
        }
    }
}
