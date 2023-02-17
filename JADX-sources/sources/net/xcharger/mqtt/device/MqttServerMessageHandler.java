package net.xcharger.mqtt.device;

import net.xcharger.sdk.device.MessageHandler;

/* loaded from: classes.dex */
public class MqttServerMessageHandler implements MessageHandler {
    private static final MqttServerMessageHandler handler = new MqttServerMessageHandler();

    public static MqttServerMessageHandler getHandler() {
        return handler;
    }

    @Override // net.xcharger.sdk.device.MessageHandler
    public void onMessage(String messageName, String version, String srcId, String data) {
        System.out.println("msg messageName= " + messageName + " msg version  " + version + " data= " + data);
    }

    @Override // net.xcharger.sdk.device.MessageHandler
    public void onDisconnected() {
    }
}
