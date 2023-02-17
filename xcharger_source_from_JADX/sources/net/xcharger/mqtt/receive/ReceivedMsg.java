package net.xcharger.mqtt.receive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.core.Const;
import net.xcharger.util.StrKit;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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

    public ReceivedMsg(String topic2, long messagegid2, String messageName2, String version2, String data2, MqttMessage message2) {
        this.topic = topic2;
        this.message = message2;
        this.messageName = messageName2;
        this.version = version2;
        this.data = data2;
        this.messagegid = messagegid2;
    }

    public ReceivedMsg() {
    }

    public synchronized ArrayList<ReceivedMsg> getReceivedMessagesInCopy() {
        return new ArrayList<>(receivedMessages);
    }

    public int receivedMessageCount() {
        return receivedMessages.size();
    }

    public synchronized boolean getMessageByMessageId(long messageid) {
        boolean z;
        boolean flag = false;
        logger.log(Level.INFO, "    messageid    :" + messageid);
        Iterator<ReceivedMsg> it = receivedMessages.iterator();
        while (true) {
            if (!it.hasNext()) {
                logger.log(Level.INFO, "    flag    :" + flag);
                z = flag;
                break;
            }
            ReceivedMsg receivedMsg = it.next();
            if (StrKit.notNull(Long.valueOf(receivedMsg.getMessagegid()))) {
                if (receivedMsg.getMessagegid() == messageid) {
                    flag = true;
                } else {
                    flag = false;
                }
                if (flag) {
                    z = flag;
                    break;
                }
            }
        }
        return z;
    }

    public boolean checkSize() {
        if (receivedMessages.size() >= Const.maxMemorySize) {
            return true;
        }
        return false;
    }

    public synchronized void delete() {
        receivedMessages.remove(0);
    }

    public long getMessagegid() {
        return this.messagegid;
    }

    public void setMessagegid(long messagegid2) {
        this.messagegid = messagegid2;
    }
}
