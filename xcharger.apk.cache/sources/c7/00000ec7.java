package org.eclipse.paho.client.mqttv3.internal;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/* loaded from: classes.dex */
public class ExceptionHelper {
    public static MqttException createMqttException(int reasonCode) {
        return (reasonCode == 4 || reasonCode == 5) ? new MqttSecurityException(reasonCode) : new MqttException(reasonCode);
    }

    public static MqttException createMqttException(Throwable cause) {
        return cause.getClass().getName().equals("java.security.GeneralSecurityException") ? new MqttSecurityException(cause) : new MqttException(cause);
    }

    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private ExceptionHelper() {
    }
}