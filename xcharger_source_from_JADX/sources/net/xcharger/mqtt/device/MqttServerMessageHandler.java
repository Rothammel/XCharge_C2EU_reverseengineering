package net.xcharger.mqtt.device;

import net.xcharger.sdk.device.MessageHandler;

public class MqttServerMessageHandler implements MessageHandler {
    private static final MqttServerMessageHandler handler = new MqttServerMessageHandler();

    public static MqttServerMessageHandler getHandler() {
        return handler;
    }

    public void onMessage(String messageName, String version, String srcId, String data) {
        System.out.println("msg messageName= " + messageName + " msg version  " + version + " data= " + data);
    }

    public void onDisconnected() {
    }
}
