package net.xcharger.mqtt.receive;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.conn.ConnectOptions;
import net.xcharger.mqtt.conn.MqttConnect;
import net.xcharger.mqtt.device.MqttServerMessageHandler;
import net.xcharger.sdk.device.MessageHandler;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

public class MqttReceiver implements MqttCallback {
    static final String className = MqttReceiver.class.getName();
    /* access modifiers changed from: private */
    public static Logger logger = Logger.getLogger(className);
    /* access modifiers changed from: private */
    public IMqttClient client;
    private String clientId;
    private boolean connected = false;
    private String credential;
    private String data;
    private MessageHandler handler;
    private String messageName;
    private long messageid;
    private ReceivedMsg receivedMsg;
    private boolean reportConnectionLoss = true;
    private ScheduledExecutorService scheduler;
    private String version;

    public MqttReceiver(IMqttClient mqttClient, MessageHandler handler2, String credential2) {
        this.handler = handler2;
        this.connected = true;
        this.clientId = mqttClient.getClientId();
        this.client = mqttClient;
        this.credential = credential2;
    }

    public MqttReceiver(IMqttAsyncClient mqttClient, MqttServerMessageHandler handler2) {
        this.handler = handler2;
        this.connected = true;
        this.clientId = mqttClient.getClientId();
    }

    public final boolean isReportConnectionLoss() {
        return this.reportConnectionLoss;
    }

    public final void setReportConnectionLoss(boolean reportConnectionLoss2) {
        this.reportConnectionLoss = reportConnectionLoss2;
    }

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

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public synchronized void messageArrived(java.lang.String r16, org.eclipse.paho.client.mqttv3.MqttMessage r17) throws java.lang.Exception {
        /*
            r15 = this;
            monitor-enter(r15)
            boolean r0 = net.xcharger.mqtt.core.Const.binaryMode     // Catch:{ all -> 0x0090 }
            if (r0 == 0) goto L_0x0093
            net.xcharger.sdk.device.MessageHandler r1 = r15.handler     // Catch:{ Exception -> 0x0076 }
            monitor-enter(r1)     // Catch:{ Exception -> 0x0076 }
            byte[] r10 = r17.getPayload()     // Catch:{ all -> 0x0073 }
            net.xcharge.sdk.server.MessageEncoder r0 = net.xcharger.mqtt.MqttKit.encoder     // Catch:{ all -> 0x0073 }
            java.lang.String r2 = r15.clientId     // Catch:{ all -> 0x0073 }
            java.lang.String[] r13 = r0.decode(r2, r10)     // Catch:{ all -> 0x0073 }
            int r0 = r13.length     // Catch:{ all -> 0x0073 }
            r2 = 3
            if (r0 != r2) goto L_0x0027
            r0 = 0
            r0 = r13[r0]     // Catch:{ all -> 0x0073 }
            r15.messageName = r0     // Catch:{ all -> 0x0073 }
            r0 = 1
            r0 = r13[r0]     // Catch:{ all -> 0x0073 }
            r15.version = r0     // Catch:{ all -> 0x0073 }
            r0 = 2
            r0 = r13[r0]     // Catch:{ all -> 0x0073 }
            r15.data = r0     // Catch:{ all -> 0x0073 }
        L_0x0027:
            java.util.logging.Logger r0 = logger     // Catch:{ all -> 0x0073 }
            java.util.logging.Level r2 = java.util.logging.Level.INFO     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = "收到消息     :"
            r3.<init>(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = r15.messageName     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = " "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = r15.version     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = " "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = r15.clientId     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = " "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r4 = r15.data     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0073 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0073 }
            r0.log(r2, r3)     // Catch:{ all -> 0x0073 }
            monitor-exit(r1)     // Catch:{ all -> 0x0073 }
            net.xcharger.sdk.device.MessageHandler r0 = r15.handler     // Catch:{ Exception -> 0x0076 }
            java.lang.String r1 = r15.messageName     // Catch:{ Exception -> 0x0076 }
            java.lang.String r2 = r15.version     // Catch:{ Exception -> 0x0076 }
            java.lang.String r3 = r15.clientId     // Catch:{ Exception -> 0x0076 }
            java.lang.String r4 = r15.data     // Catch:{ Exception -> 0x0076 }
            r0.onMessage(r1, r2, r3, r4)     // Catch:{ Exception -> 0x0076 }
        L_0x0071:
            monitor-exit(r15)
            return
        L_0x0073:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0073 }
            throw r0     // Catch:{ Exception -> 0x0076 }
        L_0x0076:
            r8 = move-exception
            net.xcharger.sdk.device.MessageProxyException r0 = new net.xcharger.sdk.device.MessageProxyException     // Catch:{ all -> 0x0090 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0090 }
            java.lang.String r2 = "  消息处理异常   "
            r1.<init>(r2)     // Catch:{ all -> 0x0090 }
            java.lang.String r2 = r8.toString()     // Catch:{ all -> 0x0090 }
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0090 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0090 }
            r0.<init>(r1)     // Catch:{ all -> 0x0090 }
            goto L_0x0071
        L_0x0090:
            r0 = move-exception
            monitor-exit(r15)
            throw r0
        L_0x0093:
            java.lang.String r10 = new java.lang.String     // Catch:{ Exception -> 0x0156 }
            byte[] r0 = r17.getPayload()     // Catch:{ Exception -> 0x0156 }
            java.lang.String r1 = net.xcharger.mqtt.core.Const.DEFAULT_ENCODING     // Catch:{ Exception -> 0x0156 }
            r10.<init>(r0, r1)     // Catch:{ Exception -> 0x0156 }
            java.util.logging.Logger r0 = logger     // Catch:{ Exception -> 0x0156 }
            java.util.logging.Level r1 = java.util.logging.Level.INFO     // Catch:{ Exception -> 0x0156 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0156 }
            java.lang.String r3 = "收到消息     :"
            r2.<init>(r3)     // Catch:{ Exception -> 0x0156 }
            java.lang.StringBuilder r2 = r2.append(r10)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0156 }
            r0.log(r1, r2)     // Catch:{ Exception -> 0x0156 }
            net.xcharger.sdk.device.MessageHandler r1 = r15.handler     // Catch:{ Exception -> 0x0156 }
            monitor-enter(r1)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r0 = "\n"
            int r9 = r10.indexOf(r0)     // Catch:{ all -> 0x0171 }
            if (r9 <= 0) goto L_0x00f5
            int r0 = r9 + 1
            int r2 = r10.length()     // Catch:{ all -> 0x0171 }
            java.lang.String r0 = r10.substring(r0, r2)     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = "\n"
            java.lang.String r3 = ""
            java.lang.String r0 = r0.replaceFirst(r2, r3)     // Catch:{ all -> 0x0171 }
            r15.data = r0     // Catch:{ all -> 0x0171 }
            r0 = 0
            java.lang.String r10 = r10.substring(r0, r9)     // Catch:{ all -> 0x0171 }
            java.lang.String r0 = " "
            java.lang.String[] r11 = r10.split(r0)     // Catch:{ all -> 0x0171 }
            int r0 = r11.length     // Catch:{ all -> 0x0171 }
            r2 = 3
            if (r0 != r2) goto L_0x00f5
            r0 = 0
            r0 = r11[r0]     // Catch:{ all -> 0x0171 }
            r15.messageName = r0     // Catch:{ all -> 0x0171 }
            r0 = 2
            r0 = r11[r0]     // Catch:{ all -> 0x0171 }
            long r2 = java.lang.Long.parseLong(r0)     // Catch:{ all -> 0x0171 }
            r15.messageid = r2     // Catch:{ all -> 0x0171 }
            r0 = 1
            r0 = r11[r0]     // Catch:{ all -> 0x0171 }
            r15.version = r0     // Catch:{ all -> 0x0171 }
        L_0x00f5:
            monitor-exit(r1)     // Catch:{ all -> 0x0171 }
            net.xcharger.mqtt.receive.ReceivedMsg r0 = new net.xcharger.mqtt.receive.ReceivedMsg     // Catch:{ Exception -> 0x0156 }
            r0.<init>()     // Catch:{ Exception -> 0x0156 }
            r15.receivedMsg = r0     // Catch:{ Exception -> 0x0156 }
            net.xcharger.mqtt.receive.ReceivedMsg r0 = r15.receivedMsg     // Catch:{ Exception -> 0x0156 }
            java.util.ArrayList r12 = r0.getReceivedMessagesInCopy()     // Catch:{ Exception -> 0x0156 }
            net.xcharger.mqtt.receive.ReceivedMsg r0 = r15.receivedMsg     // Catch:{ Exception -> 0x0156 }
            boolean r0 = r0.checkSize()     // Catch:{ Exception -> 0x0156 }
            if (r0 == 0) goto L_0x0110
            net.xcharger.mqtt.receive.ReceivedMsg r0 = r15.receivedMsg     // Catch:{ Exception -> 0x0156 }
            r0.delete()     // Catch:{ Exception -> 0x0156 }
        L_0x0110:
            net.xcharger.mqtt.receive.ReceivedMsg r0 = r15.receivedMsg     // Catch:{ Exception -> 0x0156 }
            long r2 = r15.messageid     // Catch:{ Exception -> 0x0156 }
            boolean r0 = r0.getMessageByMessageId(r2)     // Catch:{ Exception -> 0x0156 }
            if (r0 != 0) goto L_0x0138
            net.xcharger.sdk.device.MessageHandler r0 = r15.handler     // Catch:{ Exception -> 0x0156 }
            java.lang.String r1 = r15.messageName     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = r15.version     // Catch:{ Exception -> 0x0156 }
            java.lang.String r3 = r15.clientId     // Catch:{ Exception -> 0x0156 }
            java.lang.String r4 = r15.data     // Catch:{ Exception -> 0x0156 }
            r0.onMessage(r1, r2, r3, r4)     // Catch:{ Exception -> 0x0156 }
            java.util.List<net.xcharger.mqtt.receive.ReceivedMsg> r14 = net.xcharger.mqtt.receive.ReceivedMsg.receivedMessages     // Catch:{ Exception -> 0x0156 }
            net.xcharger.mqtt.receive.ReceivedMsg r0 = new net.xcharger.mqtt.receive.ReceivedMsg     // Catch:{ Exception -> 0x0156 }
            r1 = 0
            long r2 = r15.messageid     // Catch:{ Exception -> 0x0156 }
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            r0.<init>(r1, r2, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0156 }
            r14.add(r0)     // Catch:{ Exception -> 0x0156 }
        L_0x0138:
            java.util.logging.Logger r0 = logger     // Catch:{ Exception -> 0x0156 }
            java.util.logging.Level r1 = java.util.logging.Level.INFO     // Catch:{ Exception -> 0x0156 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0156 }
            java.lang.String r3 = "缓存大小！！！！＝＝＝＝"
            r2.<init>(r3)     // Catch:{ Exception -> 0x0156 }
            net.xcharger.mqtt.receive.ReceivedMsg r3 = r15.receivedMsg     // Catch:{ Exception -> 0x0156 }
            int r3 = r3.receivedMessageCount()     // Catch:{ Exception -> 0x0156 }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ Exception -> 0x0156 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0156 }
            r0.log(r1, r2)     // Catch:{ Exception -> 0x0156 }
            goto L_0x0071
        L_0x0156:
            r8 = move-exception
            net.xcharger.sdk.device.MessageProxyException r0 = new net.xcharger.sdk.device.MessageProxyException     // Catch:{ all -> 0x0090 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0090 }
            java.lang.String r2 = "  消息处理异常   "
            r1.<init>(r2)     // Catch:{ all -> 0x0090 }
            java.lang.String r2 = r8.toString()     // Catch:{ all -> 0x0090 }
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0090 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0090 }
            r0.<init>(r1)     // Catch:{ all -> 0x0090 }
            goto L_0x0071
        L_0x0171:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0171 }
            throw r0     // Catch:{ Exception -> 0x0156 }
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.mqtt.receive.MqttReceiver.messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage):void");
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.log(Level.INFO, "token client id" + token.getClient().getClientId());
        logger.log(Level.INFO, "getMessageId" + token.getMessageId());
    }

    private void startReconnect() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(new Runnable() {
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
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    public void connect() throws Exception {
        new ConnectOptions();
        this.client.connect(ConnectOptions.connectOptions(this.clientId, this.credential));
        if (this.client.isConnected()) {
        }
    }
}
