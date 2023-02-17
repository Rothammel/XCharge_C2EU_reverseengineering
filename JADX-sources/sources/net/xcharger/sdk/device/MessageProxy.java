package net.xcharger.sdk.device;

import org.eclipse.paho.client.mqttv3.MqttException;

/* loaded from: classes.dex */
public interface MessageProxy {
    public static final String version = "v1.0";

    boolean connect(String str, String str2, MessageHandler messageHandler) throws MessageProxyException, MqttException;

    void disconnect() throws MessageProxyException;

    void forceToClearClient();

    long generateSessionId();

    boolean isConnected();

    void sendMessage(String str, String str2, String str3) throws Exception;
}
