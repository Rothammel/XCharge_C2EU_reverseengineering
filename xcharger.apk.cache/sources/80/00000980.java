package net.xcharger.sdk.device;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.plugn.MqttPlugin;

/* loaded from: classes.dex */
public class MessageProxyException extends Exception {
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);
    private String code;
    private String message;

    String getVersion() {
        return "v1.0";
    }

    public MessageProxyException(String code) {
        logger.log(Level.WARNING, code);
        this.code = code;
    }

    public MessageProxyException() {
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return "";
    }
}