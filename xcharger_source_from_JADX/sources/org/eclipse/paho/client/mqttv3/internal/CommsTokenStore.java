package org.eclipse.paho.client.mqttv3.internal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class CommsTokenStore {
    private static final String CLASS_NAME = CommsTokenStore.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private MqttException closedResponse = null;
    private String logContext;
    private Hashtable tokens;

    public CommsTokenStore(String logContext2) {
        log.setResourceName(logContext2);
        this.tokens = new Hashtable();
        this.logContext = logContext2;
        log.fine(CLASS_NAME, "<Init>", "308");
    }

    public MqttToken getToken(MqttWireMessage message) {
        return (MqttToken) this.tokens.get(message.getKey());
    }

    public MqttToken getToken(String key) {
        return (MqttToken) this.tokens.get(key);
    }

    public MqttToken removeToken(MqttWireMessage message) {
        if (message != null) {
            return removeToken(message.getKey());
        }
        return null;
    }

    public MqttToken removeToken(String key) {
        log.fine(CLASS_NAME, "removeToken", "306", new Object[]{key});
        if (key != null) {
            return (MqttToken) this.tokens.remove(key);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public MqttDeliveryToken restoreToken(MqttPublish message) {
        MqttDeliveryToken token;
        synchronized (this.tokens) {
            String key = new Integer(message.getMessageId()).toString();
            if (this.tokens.containsKey(key)) {
                token = (MqttDeliveryToken) this.tokens.get(key);
                log.fine(CLASS_NAME, "restoreToken", "302", new Object[]{key, message, token});
            } else {
                token = new MqttDeliveryToken(this.logContext);
                token.internalTok.setKey(key);
                this.tokens.put(key, token);
                log.fine(CLASS_NAME, "restoreToken", "303", new Object[]{key, message, token});
            }
        }
        return token;
    }

    /* access modifiers changed from: protected */
    public void saveToken(MqttToken token, MqttWireMessage message) throws MqttException {
        synchronized (this.tokens) {
            if (this.closedResponse == null) {
                String key = message.getKey();
                log.fine(CLASS_NAME, "saveToken", HttpProxyConstants.DEFAULT_KEEP_ALIVE_TIME, new Object[]{key, message});
                saveToken(token, key);
            } else {
                throw this.closedResponse;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void saveToken(MqttToken token, String key) {
        synchronized (this.tokens) {
            log.fine(CLASS_NAME, "saveToken", "307", new Object[]{key, token.toString()});
            token.internalTok.setKey(key);
            this.tokens.put(key, token);
        }
    }

    /* access modifiers changed from: protected */
    public void quiesce(MqttException quiesceResponse) {
        synchronized (this.tokens) {
            log.fine(CLASS_NAME, "quiesce", "309", new Object[]{quiesceResponse});
            this.closedResponse = quiesceResponse;
        }
    }

    public void open() {
        synchronized (this.tokens) {
            log.fine(CLASS_NAME, "open", "310");
            this.closedResponse = null;
        }
    }

    public MqttDeliveryToken[] getOutstandingDelTokens() {
        MqttDeliveryToken[] mqttDeliveryTokenArr;
        synchronized (this.tokens) {
            log.fine(CLASS_NAME, "getOutstandingDelTokens", "311");
            Vector list = new Vector();
            Enumeration enumeration = this.tokens.elements();
            while (enumeration.hasMoreElements()) {
                MqttToken token = (MqttToken) enumeration.nextElement();
                if (token != null && (token instanceof MqttDeliveryToken) && !token.internalTok.isNotified()) {
                    list.addElement(token);
                }
            }
            mqttDeliveryTokenArr = (MqttDeliveryToken[]) list.toArray(new MqttDeliveryToken[list.size()]);
        }
        return mqttDeliveryTokenArr;
    }

    public Vector getOutstandingTokens() {
        Vector list;
        synchronized (this.tokens) {
            log.fine(CLASS_NAME, "getOutstandingTokens", "312");
            list = new Vector();
            Enumeration enumeration = this.tokens.elements();
            while (enumeration.hasMoreElements()) {
                MqttToken token = (MqttToken) enumeration.nextElement();
                if (token != null) {
                    list.addElement(token);
                }
            }
        }
        return list;
    }

    public void clear() {
        log.fine(CLASS_NAME, "clear", "305", new Object[]{new Integer(this.tokens.size())});
        synchronized (this.tokens) {
            this.tokens.clear();
        }
    }

    public int count() {
        int size;
        synchronized (this.tokens) {
            size = this.tokens.size();
        }
        return size;
    }

    public String toString() {
        String stringBuffer;
        String lineSep = System.getProperty("line.separator", StringUtils.f146LF);
        StringBuffer toks = new StringBuffer();
        synchronized (this.tokens) {
            Enumeration enumeration = this.tokens.elements();
            while (enumeration.hasMoreElements()) {
                toks.append("{" + ((MqttToken) enumeration.nextElement()).internalTok + "}" + lineSep);
            }
            stringBuffer = toks.toString();
        }
        return stringBuffer;
    }
}
