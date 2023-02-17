package net.xcharger.mqtt;

import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharge.sdk.server.MessageEncoder;
import net.xcharger.mqtt.conn.ConnectOptions;
import net.xcharger.mqtt.conn.MqttConnect;
import net.xcharger.mqtt.core.Const;
import net.xcharger.mqtt.plugn.MqttPlugin;
import net.xcharger.sdk.device.MessageHandler;
import net.xcharger.sdk.device.MessageProxyException;
import net.xcharger.util.Identities;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttKit {
    private static MqttConnect connect;
    private static MessageHandler handler;
    private static MqttPlugin plugin;
    private static String srcId;
    private static MqttClient client = null;
    static final String className = MqttKit.class.getName();
    private static Logger logger = Logger.getLogger(className);
    public static final MessageEncoder encoder = new MessageEncoder();

    public static void publish(String topic, String data) throws Exception {
        try {
            System.out.println("publish message strart");
            client = getConnect();
            if (client != null && client.isConnected()) {
                client.publish(topic, data.getBytes(), Const.Qos, false);
            }
            System.out.println("publish message end");
        } catch (Exception e) {
            logger.log(Level.WARNING, " publish message error " + e.getMessage());
        }
    }

    public static void publish(String messageName, String version, String data) throws Exception {
        String topic;
        try {
            client = getConnect();
            if (TextUtils.isEmpty(Const.upTopic)) {
                topic = String.valueOf(Const.device_message_from) + srcId;
            } else {
                topic = Const.upTopic;
            }
            long id = Identities.uuid2();
            new ConnectOptions();
            MqttMessage msgs = ConnectOptions.msg();
            msgs.setRetained(false);
            StringBuffer sb = new StringBuffer();
            sb.append(messageName).append(StringUtils.SPACE).append(version).append(StringUtils.SPACE).append(id).append(StringUtils.LF).append(data);
            logger.log(Level.INFO, "publish topic=  " + topic);
            logger.log(Level.INFO, "publish message " + sb.toString());
            if (Const.binaryMode) {
                byte[] bytes = encoder.encode(srcId, messageName, version, data);
                msgs.setPayload(bytes);
            } else {
                msgs.setPayload(sb.toString().getBytes(Const.DEFAULT_ENCODING));
            }
            if (client != null && client.isConnected()) {
                client.publish(topic, msgs);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, " publish message error " + e.getMessage());
        }
    }

    public static void getMsg() {
    }

    public static void subscribe() throws MqttException, MessageProxyException {
        String topic;
        client = getConnect();
        if (TextUtils.isEmpty(Const.downTopic)) {
            topic = String.valueOf(Const.cloud_message_to) + srcId;
        } else {
            topic = Const.downTopic;
        }
        logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " subscribe " + topic);
        client.subscribe(topic, Const.Qos);
    }

    public static void subscribe(String topic) throws MqttException, MessageProxyException {
        try {
            client = getConnect();
            logger.log(Level.INFO, "subscribe== " + topic);
            client.subscribe(topic, Const.Qos);
        } catch (Exception e) {
            logger.log(Level.INFO, "subscribe error: " + topic);
        }
    }

    public static void onConnect() throws Exception {
        logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " onConnect ReportNetworkStatus " + Const.VERSION + " {online:true}");
        publish(XCloudMessage.ReportNetworkStatus, Const.VERSION, "{online:true}");
    }

    public static void disConnect() throws Exception {
        logger.log(Level.INFO, "disConnect ReportNetworkStatus " + Const.VERSION + "{online:false}");
        publish(XCloudMessage.ReportNetworkStatus, Const.VERSION, "{online:false}");
    }

    public static MqttClient getConnect() throws MessageProxyException {
        try {
            client = MqttConnect.getClient();
            if (client == null) {
                throw new MessageProxyException("  mqtt connect is  not existence ");
            }
            if (!client.isConnected()) {
                throw new MessageProxyException("  mqtt connect is  not connect ");
            }
            if (client != null) {
                srcId = client.getClientId();
            }
            return client;
        } catch (Exception e) {
            logger.log(Level.WARNING, "  mqtt connect error : " + e.getMessage());
            throw new MessageProxyException("  mqtt connect error   " + e);
        }
    }
}
