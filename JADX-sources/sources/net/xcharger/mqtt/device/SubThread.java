package net.xcharger.mqtt.device;

import net.xcharger.mqtt.MqttKit;
import net.xcharger.sdk.device.MessageProxyException;
import org.eclipse.paho.client.mqttv3.MqttException;

/* loaded from: classes.dex */
public class SubThread extends Thread {
    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        try {
            MqttKit.subscribe();
        } catch (MessageProxyException e) {
            e.printStackTrace();
        } catch (MqttException e2) {
            e2.printStackTrace();
        }
    }
}
