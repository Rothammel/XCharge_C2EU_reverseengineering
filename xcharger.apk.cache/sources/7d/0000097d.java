package net.xcharger.mqtt.receive;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.xcharger.mqtt.core.Const;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class ReceivedMsg {
    static final String className = MqttReceiver.class.getName();
    private static Logger logger = Logger.getLogger(className);
    public static List<ReceivedMsg> receivedMessages = new ArrayList();
    public String data;
    public MqttMessage message;
    public String messageName;
    private long messagegid;
    public String topic;
    public String version;

    public ReceivedMsg(String topic, long messagegid, String messageName, String version, String data, MqttMessage message) {
        this.topic = topic;
        this.message = message;
        this.messageName = messageName;
        this.version = version;
        this.data = data;
        this.messagegid = messagegid;
    }

    public ReceivedMsg() {
    }

    public synchronized ArrayList<ReceivedMsg> getReceivedMessagesInCopy() {
        return new ArrayList<>(receivedMessages);
    }

    public int receivedMessageCount() {
        return receivedMessages.size();
    }

    /* JADX WARN: Code restructure failed: missing block: B:8:0x0026, code lost:
        net.xcharger.mqtt.receive.ReceivedMsg.logger.log(java.util.logging.Level.INFO, "    flag    :" + r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x003c, code lost:
        r1 = r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public synchronized boolean getMessageByMessageId(long r12) {
        /*
            r11 = this;
            r3 = 1
            r4 = 0
            monitor-enter(r11)
            r0 = 0
            java.util.logging.Logger r5 = net.xcharger.mqtt.receive.ReceivedMsg.logger     // Catch: java.lang.Throwable -> L68
            java.util.logging.Level r6 = java.util.logging.Level.INFO     // Catch: java.lang.Throwable -> L68
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L68
            java.lang.String r8 = "    messageid    :"
            r7.<init>(r8)     // Catch: java.lang.Throwable -> L68
            java.lang.StringBuilder r7 = r7.append(r12)     // Catch: java.lang.Throwable -> L68
            java.lang.String r7 = r7.toString()     // Catch: java.lang.Throwable -> L68
            r5.log(r6, r7)     // Catch: java.lang.Throwable -> L68
            java.util.List<net.xcharger.mqtt.receive.ReceivedMsg> r5 = net.xcharger.mqtt.receive.ReceivedMsg.receivedMessages     // Catch: java.lang.Throwable -> L68
            java.util.Iterator r5 = r5.iterator()     // Catch: java.lang.Throwable -> L68
        L20:
            boolean r6 = r5.hasNext()     // Catch: java.lang.Throwable -> L68
            if (r6 != 0) goto L3f
            java.util.logging.Logger r3 = net.xcharger.mqtt.receive.ReceivedMsg.logger     // Catch: java.lang.Throwable -> L68
            java.util.logging.Level r4 = java.util.logging.Level.INFO     // Catch: java.lang.Throwable -> L68
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L68
            java.lang.String r6 = "    flag    :"
            r5.<init>(r6)     // Catch: java.lang.Throwable -> L68
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch: java.lang.Throwable -> L68
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Throwable -> L68
            r3.log(r4, r5)     // Catch: java.lang.Throwable -> L68
            r1 = r0
        L3d:
            monitor-exit(r11)
            return r1
        L3f:
            java.lang.Object r2 = r5.next()     // Catch: java.lang.Throwable -> L68
            net.xcharger.mqtt.receive.ReceivedMsg r2 = (net.xcharger.mqtt.receive.ReceivedMsg) r2     // Catch: java.lang.Throwable -> L68
            r6 = 1
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch: java.lang.Throwable -> L68
            r7 = 0
            long r8 = r2.getMessagegid()     // Catch: java.lang.Throwable -> L68
            java.lang.Long r8 = java.lang.Long.valueOf(r8)     // Catch: java.lang.Throwable -> L68
            r6[r7] = r8     // Catch: java.lang.Throwable -> L68
            boolean r6 = net.xcharger.util.StrKit.notNull(r6)     // Catch: java.lang.Throwable -> L68
            if (r6 == 0) goto L20
            long r6 = r2.getMessagegid()     // Catch: java.lang.Throwable -> L68
            int r6 = (r6 > r12 ? 1 : (r6 == r12 ? 0 : -1))
            if (r6 != 0) goto L66
            r0 = r3
        L62:
            if (r0 == 0) goto L20
            r1 = r0
            goto L3d
        L66:
            r0 = r4
            goto L62
        L68:
            r3 = move-exception
            monitor-exit(r11)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.mqtt.receive.ReceivedMsg.getMessageByMessageId(long):boolean");
    }

    public boolean checkSize() {
        int size = Const.maxMemorySize;
        if (receivedMessages.size() < size) {
            return false;
        }
        return true;
    }

    public synchronized void delete() {
        receivedMessages.remove(0);
    }

    public long getMessagegid() {
        return this.messagegid;
    }

    public void setMessagegid(long messagegid) {
        this.messagegid = messagegid;
    }
}