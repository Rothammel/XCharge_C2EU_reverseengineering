package net.xcharger.mqtt.device;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.MqttKit;
import net.xcharger.mqtt.plugn.MqttPlugin;
import net.xcharger.sdk.device.MessageHandler;
import net.xcharger.sdk.device.MessageProxy;
import net.xcharger.sdk.device.MessageProxyException;
import net.xcharger.util.Identities;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttMessageProxy implements MessageProxy {
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);
    private static MqttPlugin plugin;

    public long generateSessionId() {
        return Identities.uuid2();
    }

    public boolean connect(String srcId, String credential, MessageHandler handler) throws MessageProxyException, MqttException {
        plugin = new MqttPlugin(srcId, credential, handler);
        try {
            return plugin.start();
        } catch (Exception e) {
            logger.log(Level.WARNING, " MqttMessageProxy connect error ");
            return false;
        }
    }

    public void sendMessage(String messageName, String version, String data) throws Exception {
        MqttKit.publish(messageName, version, data);
    }

    public void disconnect() throws MessageProxyException {
        if (plugin != null) {
            plugin.stop();
        }
    }

    public boolean isConnected() {
        MqttClient client;
        if (plugin == null || (client = MqttPlugin.getClient()) == null) {
            return false;
        }
        return client.isConnected();
    }

    public MqttClient getClient() throws MessageProxyException {
        if (plugin != null) {
            return MqttPlugin.getClient();
        }
        return null;
    }

    public void connectCloes() throws MessageProxyException {
        if (plugin != null) {
            plugin.disConnect();
        }
    }

    public void forceToClearClient() {
        MqttPlugin.forceToClearClient();
    }
}
