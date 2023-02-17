package net.xcharger.sdk.device;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.plugn.MqttPlugin;

public class MessageProxyException extends Exception {
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);
    private String code;
    private String message;

    /* access modifiers changed from: package-private */
    public String getVersion() {
        return "v1.0";
    }

    public MessageProxyException(String code2) {
        logger.log(Level.WARNING, code2);
        this.code = code2;
    }

    public MessageProxyException() {
    }

    public String getMessage() {
        return "";
    }
}
