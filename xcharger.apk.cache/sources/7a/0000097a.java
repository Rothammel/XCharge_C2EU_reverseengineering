package net.xcharger.mqtt.plugn;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.MqttKit;
import net.xcharger.mqtt.conn.MqttConnect;
import net.xcharger.mqtt.receive.MqttReceiver;
import net.xcharger.sdk.device.MessageHandler;
import net.xcharger.sdk.device.MessageProxyException;
import org.eclipse.paho.client.mqttv3.MqttClient;

/* loaded from: classes.dex */
public class MqttPlugin implements IPlugin {
    private static MqttClient client;
    private static String clientId;
    private static String credential;
    private static MessageHandler handler;
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);
    private static boolean running = false;

    @Override // net.xcharger.mqtt.plugn.IPlugin
    public boolean start() {
        try {
            logger.log(Level.INFO, "======start running==" + running);
            if (doStart()) {
                running = true;
            }
        } catch (Exception e) {
            running = false;
            logger.log(Level.WARNING, "====start=====" + e.getMessage());
        }
        return running;
    }

    @Override // net.xcharger.mqtt.plugn.IPlugin
    public boolean stop() {
        logger.log(Level.INFO, "====stop=====" + running);
        try {
            if (running) {
                MqttConnect.doStop();
                running = false;
            }
        } catch (Exception e) {
            running = false;
            new MessageProxyException(e.toString());
        }
        return running;
    }

    public MqttPlugin(String srcId, String credential2, MessageHandler handler2) {
        clientId = srcId;
        handler = handler2;
        credential = credential2;
    }

    private boolean doStart() throws Exception {
        try {
            logger.log(Level.INFO, "mqtt plugin start");
            logger.log(Level.INFO, "clientId ＝＝＝＝＝＝" + clientId);
            client = new MqttConnect().getMqttConnect(clientId, credential);
            running = client.isConnected();
            if (client.isConnected()) {
                logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " subscribe setCallback setOnConnect");
                MqttKit.subscribe();
                setCallback();
                MqttKit.onConnect();
            }
            logger.log(Level.INFO, "client.url=" + client.getServerURI());
            logger.log(Level.INFO, "client.isConnected=" + client.isConnected());
        } catch (Exception e) {
            running = false;
            logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " error " + e);
        }
        return running;
    }

    public void setCallback() {
        MqttReceiver mqttReceiver = new MqttReceiver(client, handler, credential);
        client.setCallback(mqttReceiver);
    }

    public void disConnect() {
        try {
            logger.log(Level.INFO, "stop=========" + running);
            running = false;
            MqttConnect.clearClient();
        } catch (Exception e) {
            logger.log(Level.WARNING, "stop== error=======" + e.getMessage());
        }
    }

    public static MqttClient getClient() {
        return MqttConnect.getClient();
    }

    public static void forceToClearClient() {
        MqttConnect.forceToClearStatus();
        client = null;
        running = false;
    }
}