package net.xcharger.mqtt.receive;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.MqttKit;
import net.xcharger.mqtt.conn.ConnectOptions;
import net.xcharger.mqtt.conn.MqttConnect;
import net.xcharger.mqtt.core.Const;
import net.xcharger.mqtt.device.MqttServerMessageHandler;
import net.xcharger.sdk.device.MessageHandler;
import net.xcharger.sdk.device.MessageProxyException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class MqttReceiver implements MqttCallback {
    static final String className = MqttReceiver.class.getName();
    private static Logger logger = Logger.getLogger(className);
    private IMqttClient client;
    private String clientId;
    private boolean connected;
    private String credential;
    private String data;
    private MessageHandler handler;
    private String messageName;
    private long messageid;
    private ReceivedMsg receivedMsg;
    private boolean reportConnectionLoss = true;
    private ScheduledExecutorService scheduler;
    private String version;

    public MqttReceiver(IMqttClient mqttClient, MessageHandler handler, String credential) {
        this.connected = false;
        this.handler = handler;
        this.connected = true;
        this.clientId = mqttClient.getClientId();
        this.client = mqttClient;
        this.credential = credential;
    }

    public MqttReceiver(IMqttAsyncClient mqttClient, MqttServerMessageHandler handler) {
        this.connected = false;
        this.handler = handler;
        this.connected = true;
        this.clientId = mqttClient.getClientId();
    }

    public final boolean isReportConnectionLoss() {
        return this.reportConnectionLoss;
    }

    public final void setReportConnectionLoss(boolean reportConnectionLoss) {
        this.reportConnectionLoss = reportConnectionLoss;
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
    public void connectionLost(Throwable cause) {
        logger.log(Level.INFO, "connectionLost=======");
        synchronized (this) {
            try {
                MqttConnect.clearClient();
            } catch (Exception e) {
                logger.log(Level.WARNING, "connectionLost error=======" + e.getMessage());
            }
            this.handler.onDisconnected();
            this.connected = false;
            notifyAll();
        }
        logger.log(Level.INFO, "ConnectionLost: clientId=" + this.clientId + " cause=" + cause);
        logger.log(Level.INFO, "connect lost");
        logger.log(Level.WARNING, "connect lost");
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        if (Const.binaryMode) {
            try {
                synchronized (this.handler) {
                    String[] result = MqttKit.encoder.decode(this.clientId, message.getPayload());
                    if (result.length == 3) {
                        this.messageName = result[0];
                        this.version = result[1];
                        this.data = result[2];
                    }
                    logger.log(Level.INFO, "收到消息     :" + this.messageName + StringUtils.SPACE + this.version + StringUtils.SPACE + this.clientId + StringUtils.SPACE + this.data);
                }
                this.handler.onMessage(this.messageName, this.version, this.clientId, this.data);
            } catch (Exception e) {
                new MessageProxyException("  消息处理异常   " + e.toString());
            }
        } else {
            try {
                String msg = new String(message.getPayload(), Const.DEFAULT_ENCODING);
                logger.log(Level.INFO, "收到消息     :" + msg);
                synchronized (this.handler) {
                    int i = msg.indexOf(StringUtils.LF);
                    if (i > 0) {
                        this.data = msg.substring(i + 1, msg.length()).replaceFirst(StringUtils.LF, "");
                        String[] msgs = msg.substring(0, i).split(StringUtils.SPACE);
                        if (msgs.length == 3) {
                            this.messageName = msgs[0];
                            this.messageid = Long.parseLong(msgs[2]);
                            this.version = msgs[1];
                        }
                    }
                }
                this.receivedMsg = new ReceivedMsg();
                this.receivedMsg.getReceivedMessagesInCopy();
                if (this.receivedMsg.checkSize()) {
                    this.receivedMsg.delete();
                }
                if (!this.receivedMsg.getMessageByMessageId(this.messageid)) {
                    this.handler.onMessage(this.messageName, this.version, this.clientId, this.data);
                    ReceivedMsg.receivedMessages.add(new ReceivedMsg(null, this.messageid, null, null, null, null));
                }
                logger.log(Level.INFO, "缓存大小！！！！＝＝＝＝" + this.receivedMsg.receivedMessageCount());
            } catch (Exception e2) {
                new MessageProxyException("  消息处理异常   " + e2.toString());
            }
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.log(Level.INFO, "token client id" + token.getClient().getClientId());
        logger.log(Level.INFO, "getMessageId" + token.getMessageId());
    }

    private void startReconnect() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(new Runnable() { // from class: net.xcharger.mqtt.receive.MqttReceiver.1
            @Override // java.lang.Runnable
            public void run() {
                if (!MqttReceiver.this.client.isConnected()) {
                    MqttReceiver.logger.log(Level.INFO, "------------------Reconnect");
                    try {
                        MqttReceiver.this.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0L, 10000L, TimeUnit.MILLISECONDS);
    }

    public void connect() throws Exception {
        new ConnectOptions();
        MqttConnectOptions opts = ConnectOptions.connectOptions(this.clientId, this.credential);
        this.client.connect(opts);
        if (this.client.isConnected()) {
        }
    }
}