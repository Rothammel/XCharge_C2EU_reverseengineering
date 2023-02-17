package org.eclipse.paho.client.mqttv3.internal;

import java.io.EOFException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingResp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class ClientState {
    private static final String CLASS_NAME = ClientState.class.getName();
    private static final int MAX_MSG_ID = 65535;
    private static final int MIN_MSG_ID = 1;
    private static final String PERSISTENCE_CONFIRMED_PREFIX = "sc-";
    private static final String PERSISTENCE_RECEIVED_PREFIX = "r-";
    private static final String PERSISTENCE_SENT_BUFFERED_PREFIX = "sb-";
    private static final String PERSISTENCE_SENT_PREFIX = "s-";
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private int actualInFlight = 0;
    private CommsCallback callback = null;
    private boolean cleanSession;
    private ClientComms clientComms = null;
    private boolean connected = false;
    private int inFlightPubRels = 0;
    private Hashtable inUseMsgIds;
    private Hashtable inboundQoS2 = null;
    private long keepAlive;
    private long lastInboundActivity = 0;
    private long lastOutboundActivity = 0;
    private long lastPing = 0;
    private int maxInflight = 0;
    private int nextMsgId = 0;
    private Hashtable outboundQoS0 = null;
    private Hashtable outboundQoS1 = null;
    private Hashtable outboundQoS2 = null;
    private volatile Vector pendingFlows;
    private volatile Vector pendingMessages;
    private MqttClientPersistence persistence;
    private MqttWireMessage pingCommand;
    private int pingOutstanding = 0;
    private Object pingOutstandingLock = new Object();
    private MqttPingSender pingSender = null;
    private Object queueLock = new Object();
    private Object quiesceLock = new Object();
    private boolean quiescing = false;
    private CommsTokenStore tokenStore;

    protected ClientState(MqttClientPersistence persistence2, CommsTokenStore tokenStore2, CommsCallback callback2, ClientComms clientComms2, MqttPingSender pingSender2) throws MqttException {
        log.setResourceName(clientComms2.getClient().getClientId());
        log.finer(CLASS_NAME, "<Init>", "");
        this.inUseMsgIds = new Hashtable();
        this.pendingFlows = new Vector();
        this.outboundQoS2 = new Hashtable();
        this.outboundQoS1 = new Hashtable();
        this.outboundQoS0 = new Hashtable();
        this.inboundQoS2 = new Hashtable();
        this.pingCommand = new MqttPingReq();
        this.inFlightPubRels = 0;
        this.actualInFlight = 0;
        this.persistence = persistence2;
        this.callback = callback2;
        this.tokenStore = tokenStore2;
        this.clientComms = clientComms2;
        this.pingSender = pingSender2;
        restoreState();
    }

    /* access modifiers changed from: protected */
    public void setMaxInflight(int maxInflight2) {
        this.maxInflight = maxInflight2;
        this.pendingMessages = new Vector(this.maxInflight);
    }

    /* access modifiers changed from: protected */
    public void setKeepAliveSecs(long keepAliveSecs) {
        this.keepAlive = 1000 * keepAliveSecs;
    }

    /* access modifiers changed from: protected */
    public long getKeepAlive() {
        return this.keepAlive;
    }

    /* access modifiers changed from: protected */
    public void setCleanSession(boolean cleanSession2) {
        this.cleanSession = cleanSession2;
    }

    /* access modifiers changed from: protected */
    public boolean getCleanSession() {
        return this.cleanSession;
    }

    private String getSendPersistenceKey(MqttWireMessage message) {
        return PERSISTENCE_SENT_PREFIX + message.getMessageId();
    }

    private String getSendConfirmPersistenceKey(MqttWireMessage message) {
        return PERSISTENCE_CONFIRMED_PREFIX + message.getMessageId();
    }

    private String getReceivedPersistenceKey(MqttWireMessage message) {
        return PERSISTENCE_RECEIVED_PREFIX + message.getMessageId();
    }

    private String getReceivedPersistenceKey(int messageId) {
        return PERSISTENCE_RECEIVED_PREFIX + messageId;
    }

    private String getSendBufferedPersistenceKey(MqttWireMessage message) {
        return PERSISTENCE_SENT_BUFFERED_PREFIX + message.getMessageId();
    }

    /* access modifiers changed from: protected */
    public void clearState() throws MqttException {
        log.fine(CLASS_NAME, "clearState", ">");
        this.persistence.clear();
        this.inUseMsgIds.clear();
        this.pendingMessages.clear();
        this.pendingFlows.clear();
        this.outboundQoS2.clear();
        this.outboundQoS1.clear();
        this.outboundQoS0.clear();
        this.inboundQoS2.clear();
        this.tokenStore.clear();
    }

    private MqttWireMessage restoreMessage(String key, MqttPersistable persistable) throws MqttException {
        MqttWireMessage message = null;
        try {
            message = MqttWireMessage.createWireMessage(persistable);
        } catch (MqttException ex) {
            log.fine(CLASS_NAME, "restoreMessage", "602", new Object[]{key}, ex);
            if (!(ex.getCause() instanceof EOFException)) {
                throw ex;
            } else if (key != null) {
                this.persistence.remove(key);
            }
        }
        log.fine(CLASS_NAME, "restoreMessage", "601", new Object[]{key, message});
        return message;
    }

    private void insertInOrder(Vector list, MqttWireMessage newMsg) {
        int newMsgId = newMsg.getMessageId();
        for (int i = 0; i < list.size(); i++) {
            if (((MqttWireMessage) list.elementAt(i)).getMessageId() > newMsgId) {
                list.insertElementAt(newMsg, i);
                return;
            }
        }
        list.addElement(newMsg);
    }

    private Vector reOrder(Vector list) {
        Vector newList = new Vector();
        if (list.size() != 0) {
            int previousMsgId = 0;
            int largestGap = 0;
            int largestGapMsgIdPosInList = 0;
            for (int i = 0; i < list.size(); i++) {
                int currentMsgId = ((MqttWireMessage) list.elementAt(i)).getMessageId();
                if (currentMsgId - previousMsgId > largestGap) {
                    largestGap = currentMsgId - previousMsgId;
                    largestGapMsgIdPosInList = i;
                }
                previousMsgId = currentMsgId;
            }
            if ((65535 - previousMsgId) + ((MqttWireMessage) list.elementAt(0)).getMessageId() > largestGap) {
                largestGapMsgIdPosInList = 0;
            }
            for (int i2 = largestGapMsgIdPosInList; i2 < list.size(); i2++) {
                newList.addElement(list.elementAt(i2));
            }
            for (int i3 = 0; i3 < largestGapMsgIdPosInList; i3++) {
                newList.addElement(list.elementAt(i3));
            }
        }
        return newList;
    }

    /* access modifiers changed from: protected */
    public void restoreState() throws MqttException {
        Enumeration messageKeys = this.persistence.keys();
        int highestMsgId = this.nextMsgId;
        Vector orphanedPubRels = new Vector();
        log.fine(CLASS_NAME, "restoreState", "600");
        while (messageKeys.hasMoreElements()) {
            String key = (String) messageKeys.nextElement();
            MqttWireMessage message = restoreMessage(key, this.persistence.get(key));
            if (message != null) {
                if (key.startsWith(PERSISTENCE_RECEIVED_PREFIX)) {
                    log.fine(CLASS_NAME, "restoreState", "604", new Object[]{key, message});
                    this.inboundQoS2.put(new Integer(message.getMessageId()), message);
                } else if (key.startsWith(PERSISTENCE_SENT_PREFIX)) {
                    MqttPublish sendMessage = (MqttPublish) message;
                    highestMsgId = Math.max(sendMessage.getMessageId(), highestMsgId);
                    if (this.persistence.containsKey(getSendConfirmPersistenceKey(sendMessage))) {
                        MqttPubRel confirmMessage = (MqttPubRel) restoreMessage(key, this.persistence.get(getSendConfirmPersistenceKey(sendMessage)));
                        if (confirmMessage != null) {
                            log.fine(CLASS_NAME, "restoreState", "605", new Object[]{key, message});
                            this.outboundQoS2.put(new Integer(confirmMessage.getMessageId()), confirmMessage);
                        } else {
                            log.fine(CLASS_NAME, "restoreState", "606", new Object[]{key, message});
                        }
                    } else {
                        sendMessage.setDuplicate(true);
                        if (sendMessage.getMessage().getQos() == 2) {
                            log.fine(CLASS_NAME, "restoreState", "607", new Object[]{key, message});
                            this.outboundQoS2.put(new Integer(sendMessage.getMessageId()), sendMessage);
                        } else {
                            log.fine(CLASS_NAME, "restoreState", "608", new Object[]{key, message});
                            this.outboundQoS1.put(new Integer(sendMessage.getMessageId()), sendMessage);
                        }
                    }
                    this.tokenStore.restoreToken(sendMessage).internalTok.setClient(this.clientComms.getClient());
                    this.inUseMsgIds.put(new Integer(sendMessage.getMessageId()), new Integer(sendMessage.getMessageId()));
                } else if (key.startsWith(PERSISTENCE_SENT_BUFFERED_PREFIX)) {
                    MqttPublish sendMessage2 = (MqttPublish) message;
                    highestMsgId = Math.max(sendMessage2.getMessageId(), highestMsgId);
                    if (sendMessage2.getMessage().getQos() == 2) {
                        log.fine(CLASS_NAME, "restoreState", "607", new Object[]{key, message});
                        this.outboundQoS2.put(new Integer(sendMessage2.getMessageId()), sendMessage2);
                    } else if (sendMessage2.getMessage().getQos() == 1) {
                        log.fine(CLASS_NAME, "restoreState", "608", new Object[]{key, message});
                        this.outboundQoS1.put(new Integer(sendMessage2.getMessageId()), sendMessage2);
                    } else {
                        log.fine(CLASS_NAME, "restoreState", "511", new Object[]{key, message});
                        this.outboundQoS0.put(new Integer(sendMessage2.getMessageId()), sendMessage2);
                        this.persistence.remove(key);
                    }
                    this.tokenStore.restoreToken(sendMessage2).internalTok.setClient(this.clientComms.getClient());
                    this.inUseMsgIds.put(new Integer(sendMessage2.getMessageId()), new Integer(sendMessage2.getMessageId()));
                } else if (key.startsWith(PERSISTENCE_CONFIRMED_PREFIX)) {
                    if (!this.persistence.containsKey(getSendPersistenceKey((MqttPubRel) message))) {
                        orphanedPubRels.addElement(key);
                    }
                }
            }
        }
        Enumeration messageKeys2 = orphanedPubRels.elements();
        while (messageKeys2.hasMoreElements()) {
            String key2 = (String) messageKeys2.nextElement();
            log.fine(CLASS_NAME, "restoreState", "609", new Object[]{key2});
            this.persistence.remove(key2);
        }
        this.nextMsgId = highestMsgId;
    }

    private void restoreInflightMessages() {
        this.pendingMessages = new Vector(this.maxInflight);
        this.pendingFlows = new Vector();
        Enumeration keys = this.outboundQoS2.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            MqttWireMessage msg = (MqttWireMessage) this.outboundQoS2.get(key);
            if (msg instanceof MqttPublish) {
                log.fine(CLASS_NAME, "restoreInflightMessages", "610", new Object[]{key});
                msg.setDuplicate(true);
                insertInOrder(this.pendingMessages, (MqttPublish) msg);
            } else if (msg instanceof MqttPubRel) {
                log.fine(CLASS_NAME, "restoreInflightMessages", "611", new Object[]{key});
                insertInOrder(this.pendingFlows, (MqttPubRel) msg);
            }
        }
        Enumeration keys2 = this.outboundQoS1.keys();
        while (keys2.hasMoreElements()) {
            Object key2 = keys2.nextElement();
            MqttPublish msg2 = (MqttPublish) this.outboundQoS1.get(key2);
            msg2.setDuplicate(true);
            log.fine(CLASS_NAME, "restoreInflightMessages", "612", new Object[]{key2});
            insertInOrder(this.pendingMessages, msg2);
        }
        Enumeration keys3 = this.outboundQoS0.keys();
        while (keys3.hasMoreElements()) {
            Object key3 = keys3.nextElement();
            log.fine(CLASS_NAME, "restoreInflightMessages", "512", new Object[]{key3});
            insertInOrder(this.pendingMessages, (MqttPublish) this.outboundQoS0.get(key3));
        }
        this.pendingFlows = reOrder(this.pendingFlows);
        this.pendingMessages = reOrder(this.pendingMessages);
    }

    public void send(MqttWireMessage message, MqttToken token) throws MqttException {
        if (message.isMessageIdRequired() && message.getMessageId() == 0) {
            if ((message instanceof MqttPublish) && ((MqttPublish) message).getMessage().getQos() != 0) {
                message.setMessageId(getNextMessageId());
            } else if ((message instanceof MqttPubAck) || (message instanceof MqttPubRec) || (message instanceof MqttPubRel) || (message instanceof MqttPubComp) || (message instanceof MqttSubscribe) || (message instanceof MqttSuback) || (message instanceof MqttUnsubscribe) || (message instanceof MqttUnsubAck)) {
                message.setMessageId(getNextMessageId());
            }
        }
        if (token != null) {
            try {
                token.internalTok.setMessageID(message.getMessageId());
            } catch (Exception e) {
            }
        }
        if (message instanceof MqttPublish) {
            synchronized (this.queueLock) {
                if (this.actualInFlight >= this.maxInflight) {
                    log.fine(CLASS_NAME, "send", "613", new Object[]{new Integer(this.actualInFlight)});
                    throw new MqttException(32202);
                }
                MqttMessage innerMessage = ((MqttPublish) message).getMessage();
                log.fine(CLASS_NAME, "send", "628", new Object[]{new Integer(message.getMessageId()), new Integer(innerMessage.getQos()), message});
                switch (innerMessage.getQos()) {
                    case 1:
                        this.outboundQoS1.put(new Integer(message.getMessageId()), message);
                        this.persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
                        break;
                    case 2:
                        this.outboundQoS2.put(new Integer(message.getMessageId()), message);
                        this.persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
                        break;
                }
                this.tokenStore.saveToken(token, message);
                this.pendingMessages.addElement(message);
                this.queueLock.notifyAll();
            }
            return;
        }
        log.fine(CLASS_NAME, "send", "615", new Object[]{new Integer(message.getMessageId()), message});
        if (message instanceof MqttConnect) {
            synchronized (this.queueLock) {
                this.tokenStore.saveToken(token, message);
                this.pendingFlows.insertElementAt(message, 0);
                this.queueLock.notifyAll();
            }
            return;
        }
        if (message instanceof MqttPingReq) {
            this.pingCommand = message;
        } else if (message instanceof MqttPubRel) {
            this.outboundQoS2.put(new Integer(message.getMessageId()), message);
            this.persistence.put(getSendConfirmPersistenceKey(message), (MqttPubRel) message);
        } else if (message instanceof MqttPubComp) {
            this.persistence.remove(getReceivedPersistenceKey(message));
        }
        synchronized (this.queueLock) {
            if (!(message instanceof MqttAck)) {
                this.tokenStore.saveToken(token, message);
            }
            this.pendingFlows.addElement(message);
            this.queueLock.notifyAll();
        }
    }

    public void persistBufferedMessage(MqttWireMessage message) {
        String key = getSendBufferedPersistenceKey(message);
        try {
            message.setMessageId(getNextMessageId());
            String key2 = getSendBufferedPersistenceKey(message);
            try {
                this.persistence.put(key2, (MqttPublish) message);
            } catch (MqttPersistenceException e) {
                log.fine(CLASS_NAME, "persistBufferedMessage", "515");
                this.persistence.open(this.clientComms.getClient().getClientId(), this.clientComms.getClient().getServerURI());
                this.persistence.put(key2, (MqttPublish) message);
            }
            log.fine(CLASS_NAME, "persistBufferedMessage", "513", new Object[]{key2});
        } catch (MqttException e2) {
            log.warning(CLASS_NAME, "persistBufferedMessage", "513", new Object[]{key});
        }
    }

    public void unPersistBufferedMessage(MqttWireMessage message) {
        try {
            log.fine(CLASS_NAME, "unPersistBufferedMessage", "517", new Object[]{message.getKey()});
            this.persistence.remove(getSendBufferedPersistenceKey(message));
        } catch (MqttPersistenceException e) {
            log.fine(CLASS_NAME, "unPersistBufferedMessage", "518", new Object[]{message.getKey()});
        }
    }

    /* access modifiers changed from: protected */
    public void undo(MqttPublish message) throws MqttPersistenceException {
        synchronized (this.queueLock) {
            log.fine(CLASS_NAME, "undo", "618", new Object[]{new Integer(message.getMessageId()), new Integer(message.getMessage().getQos())});
            if (message.getMessage().getQos() == 1) {
                this.outboundQoS1.remove(new Integer(message.getMessageId()));
            } else {
                this.outboundQoS2.remove(new Integer(message.getMessageId()));
            }
            this.pendingMessages.removeElement(message);
            this.persistence.remove(getSendPersistenceKey(message));
            this.tokenStore.removeToken((MqttWireMessage) message);
            if (message.getMessage().getQos() > 0) {
                releaseMessageId(message.getMessageId());
                message.setMessageId(0);
            }
            checkQuiesceLock();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0026, code lost:
        if (r22.connected == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
        if (r22.keepAlive <= 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        r8 = java.lang.System.currentTimeMillis();
        r12 = r22.pingOutstandingLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003c, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        if (r22.pingOutstanding <= 0) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        if ((r8 - r22.lastInboundActivity) < (r22.keepAlive + ((long) 100))) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        log.severe(CLASS_NAME, "checkForActivity", "619", new java.lang.Object[]{new java.lang.Long(r22.keepAlive), new java.lang.Long(r22.lastOutboundActivity), new java.lang.Long(r22.lastInboundActivity), new java.lang.Long(r8), new java.lang.Long(r22.lastPing)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ca, code lost:
        throw org.eclipse.paho.client.mqttv3.internal.ExceptionHelper.createMqttException(32000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00cb, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00cc, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00cd, code lost:
        throw r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d5, code lost:
        if (r22.pingOutstanding != 0) goto L_0x015e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00e9, code lost:
        if ((r8 - r22.lastOutboundActivity) < (2 * r22.keepAlive)) goto L_0x015e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00eb, code lost:
        log.severe(CLASS_NAME, "checkForActivity", "642", new java.lang.Object[]{new java.lang.Long(r22.keepAlive), new java.lang.Long(r22.lastOutboundActivity), new java.lang.Long(r22.lastInboundActivity), new java.lang.Long(r8), new java.lang.Long(r22.lastPing)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x015d, code lost:
        throw org.eclipse.paho.client.mqttv3.internal.ExceptionHelper.createMqttException(32002);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0162, code lost:
        if (r22.pingOutstanding != 0) goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0177, code lost:
        if ((r8 - r22.lastInboundActivity) >= (r22.keepAlive - ((long) 100))) goto L_0x018e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x018c, code lost:
        if ((r8 - r22.lastOutboundActivity) < (r22.keepAlive - ((long) 100))) goto L_0x0236;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x018e, code lost:
        log.fine(CLASS_NAME, "checkForActivity", "620", new java.lang.Object[]{new java.lang.Long(r22.keepAlive), new java.lang.Long(r22.lastOutboundActivity), new java.lang.Long(r22.lastInboundActivity)});
        r10 = new org.eclipse.paho.client.mqttv3.MqttToken(r22.clientComms.getClient().getClientId());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x01ed, code lost:
        if (r23 == null) goto L_0x01f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r10.setActionCallback(r23);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x01f4, code lost:
        r22.tokenStore.saveToken(r10, r22.pingCommand);
        r22.pendingFlows.insertElementAt(r22.pingCommand, 0);
        r6 = getKeepAlive();
        notifyQueueLock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0212, code lost:
        r5 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0214, code lost:
        log.fine(CLASS_NAME, "checkForActivity", "624", new java.lang.Object[]{new java.lang.Long(r6)});
        r22.pingSender.schedule(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        log.fine(CLASS_NAME, "checkForActivity", "634", (java.lang.Object[]) null);
        r6 = java.lang.Math.max(1, getKeepAlive() - (r8 - r22.lastOutboundActivity));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x025a, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x025b, code lost:
        r5 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001d, code lost:
        r5 = null;
        r6 = getKeepAlive();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.eclipse.paho.client.mqttv3.MqttToken checkForActivity(org.eclipse.paho.client.mqttv3.IMqttActionListener r23) throws org.eclipse.paho.client.mqttv3.MqttException {
        /*
            r22 = this;
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log
            java.lang.String r12 = CLASS_NAME
            java.lang.String r13 = "checkForActivity"
            java.lang.String r14 = "616"
            r15 = 0
            java.lang.Object[] r15 = new java.lang.Object[r15]
            r11.fine(r12, r13, r14, r15)
            r0 = r22
            java.lang.Object r12 = r0.quiesceLock
            monitor-enter(r12)
            r0 = r22
            boolean r11 = r0.quiescing     // Catch:{ all -> 0x00ce }
            if (r11 == 0) goto L_0x001c
            monitor-exit(r12)     // Catch:{ all -> 0x00ce }
            r5 = 0
        L_0x001b:
            return r5
        L_0x001c:
            monitor-exit(r12)     // Catch:{ all -> 0x00ce }
            r5 = 0
            long r6 = r22.getKeepAlive()
            r0 = r22
            boolean r11 = r0.connected
            if (r11 == 0) goto L_0x001b
            r0 = r22
            long r12 = r0.keepAlive
            r14 = 0
            int r11 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1))
            if (r11 <= 0) goto L_0x001b
            long r8 = java.lang.System.currentTimeMillis()
            r4 = 100
            r0 = r22
            java.lang.Object r12 = r0.pingOutstandingLock
            monitor-enter(r12)
            r0 = r22
            int r11 = r0.pingOutstanding     // Catch:{ all -> 0x00cb }
            if (r11 <= 0) goto L_0x00d1
            r0 = r22
            long r14 = r0.lastInboundActivity     // Catch:{ all -> 0x00cb }
            long r14 = r8 - r14
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r16 = r0
            long r0 = (long) r4     // Catch:{ all -> 0x00cb }
            r18 = r0
            long r16 = r16 + r18
            int r11 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r11 < 0) goto L_0x00d1
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log     // Catch:{ all -> 0x00cb }
            java.lang.String r13 = CLASS_NAME     // Catch:{ all -> 0x00cb }
            java.lang.String r14 = "checkForActivity"
            java.lang.String r15 = "619"
            r16 = 5
            r0 = r16
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ all -> 0x00cb }
            r16 = r0
            r17 = 0
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 1
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 2
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastInboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 3
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r18
            r0.<init>(r8)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 4
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastPing     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r0 = r16
            r11.severe(r13, r14, r15, r0)     // Catch:{ all -> 0x00cb }
            r11 = 32000(0x7d00, float:4.4842E-41)
            org.eclipse.paho.client.mqttv3.MqttException r11 = org.eclipse.paho.client.mqttv3.internal.ExceptionHelper.createMqttException((int) r11)     // Catch:{ all -> 0x00cb }
            throw r11     // Catch:{ all -> 0x00cb }
        L_0x00cb:
            r11 = move-exception
        L_0x00cc:
            monitor-exit(r12)     // Catch:{ all -> 0x00cb }
            throw r11
        L_0x00ce:
            r11 = move-exception
            monitor-exit(r12)     // Catch:{ all -> 0x00ce }
            throw r11
        L_0x00d1:
            r0 = r22
            int r11 = r0.pingOutstanding     // Catch:{ all -> 0x00cb }
            if (r11 != 0) goto L_0x015e
            r0 = r22
            long r14 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            long r14 = r8 - r14
            r16 = 2
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r18 = r0
            long r16 = r16 * r18
            int r11 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r11 < 0) goto L_0x015e
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log     // Catch:{ all -> 0x00cb }
            java.lang.String r13 = CLASS_NAME     // Catch:{ all -> 0x00cb }
            java.lang.String r14 = "checkForActivity"
            java.lang.String r15 = "642"
            r16 = 5
            r0 = r16
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ all -> 0x00cb }
            r16 = r0
            r17 = 0
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 1
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 2
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastInboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 3
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r18
            r0.<init>(r8)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 4
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastPing     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r0 = r16
            r11.severe(r13, r14, r15, r0)     // Catch:{ all -> 0x00cb }
            r11 = 32002(0x7d02, float:4.4844E-41)
            org.eclipse.paho.client.mqttv3.MqttException r11 = org.eclipse.paho.client.mqttv3.internal.ExceptionHelper.createMqttException((int) r11)     // Catch:{ all -> 0x00cb }
            throw r11     // Catch:{ all -> 0x00cb }
        L_0x015e:
            r0 = r22
            int r11 = r0.pingOutstanding     // Catch:{ all -> 0x00cb }
            if (r11 != 0) goto L_0x0179
            r0 = r22
            long r14 = r0.lastInboundActivity     // Catch:{ all -> 0x00cb }
            long r14 = r8 - r14
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r16 = r0
            long r0 = (long) r4     // Catch:{ all -> 0x00cb }
            r18 = r0
            long r16 = r16 - r18
            int r11 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r11 >= 0) goto L_0x018e
        L_0x0179:
            r0 = r22
            long r14 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            long r14 = r8 - r14
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r16 = r0
            long r0 = (long) r4     // Catch:{ all -> 0x00cb }
            r18 = r0
            long r16 = r16 - r18
            int r11 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r11 < 0) goto L_0x0236
        L_0x018e:
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log     // Catch:{ all -> 0x00cb }
            java.lang.String r13 = CLASS_NAME     // Catch:{ all -> 0x00cb }
            java.lang.String r14 = "checkForActivity"
            java.lang.String r15 = "620"
            r16 = 3
            r0 = r16
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ all -> 0x00cb }
            r16 = r0
            r17 = 0
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.keepAlive     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 1
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r17 = 2
            java.lang.Long r18 = new java.lang.Long     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastInboundActivity     // Catch:{ all -> 0x00cb }
            r20 = r0
            r0 = r18
            r1 = r20
            r0.<init>(r1)     // Catch:{ all -> 0x00cb }
            r16[r17] = r18     // Catch:{ all -> 0x00cb }
            r0 = r16
            r11.fine(r13, r14, r15, r0)     // Catch:{ all -> 0x00cb }
            org.eclipse.paho.client.mqttv3.MqttToken r10 = new org.eclipse.paho.client.mqttv3.MqttToken     // Catch:{ all -> 0x00cb }
            r0 = r22
            org.eclipse.paho.client.mqttv3.internal.ClientComms r11 = r0.clientComms     // Catch:{ all -> 0x00cb }
            org.eclipse.paho.client.mqttv3.IMqttAsyncClient r11 = r11.getClient()     // Catch:{ all -> 0x00cb }
            java.lang.String r11 = r11.getClientId()     // Catch:{ all -> 0x00cb }
            r10.<init>(r11)     // Catch:{ all -> 0x00cb }
            if (r23 == 0) goto L_0x01f4
            r0 = r23
            r10.setActionCallback(r0)     // Catch:{ all -> 0x025a }
        L_0x01f4:
            r0 = r22
            org.eclipse.paho.client.mqttv3.internal.CommsTokenStore r11 = r0.tokenStore     // Catch:{ all -> 0x025a }
            r0 = r22
            org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage r13 = r0.pingCommand     // Catch:{ all -> 0x025a }
            r11.saveToken((org.eclipse.paho.client.mqttv3.MqttToken) r10, (org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage) r13)     // Catch:{ all -> 0x025a }
            r0 = r22
            java.util.Vector r11 = r0.pendingFlows     // Catch:{ all -> 0x025a }
            r0 = r22
            org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage r13 = r0.pingCommand     // Catch:{ all -> 0x025a }
            r14 = 0
            r11.insertElementAt(r13, r14)     // Catch:{ all -> 0x025a }
            long r6 = r22.getKeepAlive()     // Catch:{ all -> 0x025a }
            r22.notifyQueueLock()     // Catch:{ all -> 0x025a }
            r5 = r10
        L_0x0213:
            monitor-exit(r12)     // Catch:{ all -> 0x00cb }
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log
            java.lang.String r12 = CLASS_NAME
            java.lang.String r13 = "checkForActivity"
            java.lang.String r14 = "624"
            r15 = 1
            java.lang.Object[] r15 = new java.lang.Object[r15]
            r16 = 0
            java.lang.Long r17 = new java.lang.Long
            r0 = r17
            r0.<init>(r6)
            r15[r16] = r17
            r11.fine(r12, r13, r14, r15)
            r0 = r22
            org.eclipse.paho.client.mqttv3.MqttPingSender r11 = r0.pingSender
            r11.schedule(r6)
            goto L_0x001b
        L_0x0236:
            org.eclipse.paho.client.mqttv3.logging.Logger r11 = log     // Catch:{ all -> 0x00cb }
            java.lang.String r13 = CLASS_NAME     // Catch:{ all -> 0x00cb }
            java.lang.String r14 = "checkForActivity"
            java.lang.String r15 = "634"
            r16 = 0
            r0 = r16
            r11.fine(r13, r14, r15, r0)     // Catch:{ all -> 0x00cb }
            r14 = 1
            long r16 = r22.getKeepAlive()     // Catch:{ all -> 0x00cb }
            r0 = r22
            long r0 = r0.lastOutboundActivity     // Catch:{ all -> 0x00cb }
            r18 = r0
            long r18 = r8 - r18
            long r16 = r16 - r18
            long r6 = java.lang.Math.max(r14, r16)     // Catch:{ all -> 0x00cb }
            goto L_0x0213
        L_0x025a:
            r11 = move-exception
            r5 = r10
            goto L_0x00cc
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.ClientState.checkForActivity(org.eclipse.paho.client.mqttv3.IMqttActionListener):org.eclipse.paho.client.mqttv3.MqttToken");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v17, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage get() throws org.eclipse.paho.client.mqttv3.MqttException {
        /*
            r11 = this;
            r1 = 0
            java.lang.Object r3 = r11.queueLock
            monitor-enter(r3)
        L_0x0004:
            if (r1 == 0) goto L_0x0008
            monitor-exit(r3)     // Catch:{ all -> 0x00a2 }
        L_0x0007:
            return r1
        L_0x0008:
            java.util.Vector r2 = r11.pendingMessages     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 == 0) goto L_0x0018
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x0026
        L_0x0018:
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 == 0) goto L_0x0041
            int r2 = r11.actualInFlight     // Catch:{ all -> 0x00a2 }
            int r4 = r11.maxInflight     // Catch:{ all -> 0x00a2 }
            if (r2 < r4) goto L_0x0041
        L_0x0026:
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ InterruptedException -> 0x00f1 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ InterruptedException -> 0x00f1 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "644"
            r2.fine(r4, r5, r6)     // Catch:{ InterruptedException -> 0x00f1 }
            java.lang.Object r2 = r11.queueLock     // Catch:{ InterruptedException -> 0x00f1 }
            r2.wait()     // Catch:{ InterruptedException -> 0x00f1 }
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ InterruptedException -> 0x00f1 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ InterruptedException -> 0x00f1 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "647"
            r2.fine(r4, r5, r6)     // Catch:{ InterruptedException -> 0x00f1 }
        L_0x0041:
            boolean r2 = r11.connected     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x0068
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x005a
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            r4 = 0
            java.lang.Object r2 = r2.elementAt(r4)     // Catch:{ all -> 0x00a2 }
            org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage r2 = (org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage) r2     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2 instanceof org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x0068
        L_0x005a:
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ all -> 0x00a2 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ all -> 0x00a2 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "621"
            r2.fine(r4, r5, r6)     // Catch:{ all -> 0x00a2 }
            monitor-exit(r3)     // Catch:{ all -> 0x00a2 }
            r1 = 0
            goto L_0x0007
        L_0x0068:
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x00a5
            java.util.Vector r2 = r11.pendingFlows     // Catch:{ all -> 0x00a2 }
            r4 = 0
            java.lang.Object r2 = r2.remove(r4)     // Catch:{ all -> 0x00a2 }
            r0 = r2
            org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage r0 = (org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage) r0     // Catch:{ all -> 0x00a2 }
            r1 = r0
            boolean r2 = r1 instanceof org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel     // Catch:{ all -> 0x00a2 }
            if (r2 == 0) goto L_0x009d
            int r2 = r11.inFlightPubRels     // Catch:{ all -> 0x00a2 }
            int r2 = r2 + 1
            r11.inFlightPubRels = r2     // Catch:{ all -> 0x00a2 }
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ all -> 0x00a2 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ all -> 0x00a2 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "617"
            r7 = 1
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ all -> 0x00a2 }
            r8 = 0
            java.lang.Integer r9 = new java.lang.Integer     // Catch:{ all -> 0x00a2 }
            int r10 = r11.inFlightPubRels     // Catch:{ all -> 0x00a2 }
            r9.<init>(r10)     // Catch:{ all -> 0x00a2 }
            r7[r8] = r9     // Catch:{ all -> 0x00a2 }
            r2.fine(r4, r5, r6, r7)     // Catch:{ all -> 0x00a2 }
        L_0x009d:
            r11.checkQuiesceLock()     // Catch:{ all -> 0x00a2 }
            goto L_0x0004
        L_0x00a2:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00a2 }
            throw r2
        L_0x00a5:
            java.util.Vector r2 = r11.pendingMessages     // Catch:{ all -> 0x00a2 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x00a2 }
            if (r2 != 0) goto L_0x0004
            int r2 = r11.actualInFlight     // Catch:{ all -> 0x00a2 }
            int r4 = r11.maxInflight     // Catch:{ all -> 0x00a2 }
            if (r2 >= r4) goto L_0x00e4
            java.util.Vector r2 = r11.pendingMessages     // Catch:{ all -> 0x00a2 }
            r4 = 0
            java.lang.Object r2 = r2.elementAt(r4)     // Catch:{ all -> 0x00a2 }
            r0 = r2
            org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage r0 = (org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage) r0     // Catch:{ all -> 0x00a2 }
            r1 = r0
            java.util.Vector r2 = r11.pendingMessages     // Catch:{ all -> 0x00a2 }
            r4 = 0
            r2.removeElementAt(r4)     // Catch:{ all -> 0x00a2 }
            int r2 = r11.actualInFlight     // Catch:{ all -> 0x00a2 }
            int r2 = r2 + 1
            r11.actualInFlight = r2     // Catch:{ all -> 0x00a2 }
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ all -> 0x00a2 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ all -> 0x00a2 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "623"
            r7 = 1
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ all -> 0x00a2 }
            r8 = 0
            java.lang.Integer r9 = new java.lang.Integer     // Catch:{ all -> 0x00a2 }
            int r10 = r11.actualInFlight     // Catch:{ all -> 0x00a2 }
            r9.<init>(r10)     // Catch:{ all -> 0x00a2 }
            r7[r8] = r9     // Catch:{ all -> 0x00a2 }
            r2.fine(r4, r5, r6, r7)     // Catch:{ all -> 0x00a2 }
            goto L_0x0004
        L_0x00e4:
            org.eclipse.paho.client.mqttv3.logging.Logger r2 = log     // Catch:{ all -> 0x00a2 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ all -> 0x00a2 }
            java.lang.String r5 = "get"
            java.lang.String r6 = "622"
            r2.fine(r4, r5, r6)     // Catch:{ all -> 0x00a2 }
            goto L_0x0004
        L_0x00f1:
            r2 = move-exception
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.ClientState.get():org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage");
    }

    public void setKeepAliveInterval(long interval) {
        this.keepAlive = interval;
    }

    public void notifySentBytes(int sentBytesCount) {
        if (sentBytesCount > 0) {
            this.lastOutboundActivity = System.currentTimeMillis();
        }
        log.fine(CLASS_NAME, "notifySentBytes", "643", new Object[]{new Integer(sentBytesCount)});
    }

    /* access modifiers changed from: protected */
    public void notifySent(MqttWireMessage message) {
        this.lastOutboundActivity = System.currentTimeMillis();
        log.fine(CLASS_NAME, "notifySent", "625", new Object[]{message.getKey()});
        MqttToken token = this.tokenStore.getToken(message);
        token.internalTok.notifySent();
        if (message instanceof MqttPingReq) {
            synchronized (this.pingOutstandingLock) {
                long time = System.currentTimeMillis();
                synchronized (this.pingOutstandingLock) {
                    this.lastPing = time;
                    this.pingOutstanding++;
                }
                log.fine(CLASS_NAME, "notifySent", "635", new Object[]{new Integer(this.pingOutstanding)});
            }
        } else if ((message instanceof MqttPublish) && ((MqttPublish) message).getMessage().getQos() == 0) {
            token.internalTok.markComplete((MqttWireMessage) null, (MqttException) null);
            this.callback.asyncOperationComplete(token);
            decrementInFlight();
            releaseMessageId(message.getMessageId());
            this.tokenStore.removeToken(message);
            checkQuiesceLock();
        }
    }

    private void decrementInFlight() {
        synchronized (this.queueLock) {
            this.actualInFlight--;
            log.fine(CLASS_NAME, "decrementInFlight", "646", new Object[]{new Integer(this.actualInFlight)});
            if (!checkQuiesceLock()) {
                this.queueLock.notifyAll();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkQuiesceLock() {
        int tokC = this.tokenStore.count();
        if (!this.quiescing || tokC != 0 || this.pendingFlows.size() != 0 || !this.callback.isQuiesced()) {
            return false;
        }
        log.fine(CLASS_NAME, "checkQuiesceLock", "626", new Object[]{new Boolean(this.quiescing), new Integer(this.actualInFlight), new Integer(this.pendingFlows.size()), new Integer(this.inFlightPubRels), Boolean.valueOf(this.callback.isQuiesced()), new Integer(tokC)});
        synchronized (this.quiesceLock) {
            this.quiesceLock.notifyAll();
        }
        return true;
    }

    public void notifyReceivedBytes(int receivedBytesCount) {
        if (receivedBytesCount > 0) {
            this.lastInboundActivity = System.currentTimeMillis();
        }
        log.fine(CLASS_NAME, "notifyReceivedBytes", "630", new Object[]{new Integer(receivedBytesCount)});
    }

    /* access modifiers changed from: protected */
    public void notifyReceivedAck(MqttAck ack) throws MqttException {
        this.lastInboundActivity = System.currentTimeMillis();
        log.fine(CLASS_NAME, "notifyReceivedAck", "627", new Object[]{new Integer(ack.getMessageId()), ack});
        MqttToken token = this.tokenStore.getToken((MqttWireMessage) ack);
        if (token == null) {
            log.fine(CLASS_NAME, "notifyReceivedAck", "662", new Object[]{new Integer(ack.getMessageId())});
        } else if (ack instanceof MqttPubRec) {
            send(new MqttPubRel((MqttPubRec) ack), token);
        } else if ((ack instanceof MqttPubAck) || (ack instanceof MqttPubComp)) {
            notifyResult(ack, token, (MqttException) null);
        } else if (ack instanceof MqttPingResp) {
            synchronized (this.pingOutstandingLock) {
                this.pingOutstanding = Math.max(0, this.pingOutstanding - 1);
                notifyResult(ack, token, (MqttException) null);
                if (this.pingOutstanding == 0) {
                    this.tokenStore.removeToken((MqttWireMessage) ack);
                }
            }
            log.fine(CLASS_NAME, "notifyReceivedAck", "636", new Object[]{new Integer(this.pingOutstanding)});
        } else if (ack instanceof MqttConnack) {
            int rc = ((MqttConnack) ack).getReturnCode();
            if (rc == 0) {
                synchronized (this.queueLock) {
                    if (this.cleanSession) {
                        clearState();
                        this.tokenStore.saveToken(token, (MqttWireMessage) ack);
                    }
                    this.inFlightPubRels = 0;
                    this.actualInFlight = 0;
                    restoreInflightMessages();
                    connected();
                }
                this.clientComms.connectComplete((MqttConnack) ack, (MqttException) null);
                notifyResult(ack, token, (MqttException) null);
                this.tokenStore.removeToken((MqttWireMessage) ack);
                synchronized (this.queueLock) {
                    this.queueLock.notifyAll();
                }
            } else {
                throw ExceptionHelper.createMqttException(rc);
            }
        } else {
            notifyResult(ack, token, (MqttException) null);
            releaseMessageId(ack.getMessageId());
            this.tokenStore.removeToken((MqttWireMessage) ack);
        }
        checkQuiesceLock();
    }

    /* access modifiers changed from: protected */
    public void notifyReceivedMsg(MqttWireMessage message) throws MqttException {
        this.lastInboundActivity = System.currentTimeMillis();
        log.fine(CLASS_NAME, "notifyReceivedMsg", "651", new Object[]{new Integer(message.getMessageId()), message});
        if (this.quiescing) {
            return;
        }
        if (message instanceof MqttPublish) {
            MqttPublish send = (MqttPublish) message;
            switch (send.getMessage().getQos()) {
                case 0:
                case 1:
                    if (this.callback != null) {
                        this.callback.messageArrived(send);
                        return;
                    }
                    return;
                case 2:
                    this.persistence.put(getReceivedPersistenceKey(message), (MqttPublish) message);
                    this.inboundQoS2.put(new Integer(send.getMessageId()), send);
                    send(new MqttPubRec(send), (MqttToken) null);
                    return;
                default:
                    return;
            }
        } else if (message instanceof MqttPubRel) {
            MqttPublish sendMsg = (MqttPublish) this.inboundQoS2.get(new Integer(message.getMessageId()));
            if (sendMsg == null) {
                send(new MqttPubComp(message.getMessageId()), (MqttToken) null);
            } else if (this.callback != null) {
                this.callback.messageArrived(sendMsg);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyComplete(MqttToken token) throws MqttException {
        MqttWireMessage message = token.internalTok.getWireMessage();
        if (message != null && (message instanceof MqttAck)) {
            log.fine(CLASS_NAME, "notifyComplete", "629", new Object[]{new Integer(message.getMessageId()), token, message});
            MqttAck ack = (MqttAck) message;
            if (ack instanceof MqttPubAck) {
                this.persistence.remove(getSendPersistenceKey(message));
                this.persistence.remove(getSendBufferedPersistenceKey(message));
                this.outboundQoS1.remove(new Integer(ack.getMessageId()));
                decrementInFlight();
                releaseMessageId(message.getMessageId());
                this.tokenStore.removeToken(message);
                log.fine(CLASS_NAME, "notifyComplete", "650", new Object[]{new Integer(ack.getMessageId())});
            } else if (ack instanceof MqttPubComp) {
                this.persistence.remove(getSendPersistenceKey(message));
                this.persistence.remove(getSendConfirmPersistenceKey(message));
                this.persistence.remove(getSendBufferedPersistenceKey(message));
                this.outboundQoS2.remove(new Integer(ack.getMessageId()));
                this.inFlightPubRels--;
                decrementInFlight();
                releaseMessageId(message.getMessageId());
                this.tokenStore.removeToken(message);
                log.fine(CLASS_NAME, "notifyComplete", "645", new Object[]{new Integer(ack.getMessageId()), new Integer(this.inFlightPubRels)});
            }
            checkQuiesceLock();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyResult(MqttWireMessage ack, MqttToken token, MqttException ex) {
        token.internalTok.markComplete(ack, ex);
        token.internalTok.notifyComplete();
        if (ack != null && (ack instanceof MqttAck) && !(ack instanceof MqttPubRec)) {
            log.fine(CLASS_NAME, "notifyResult", "648", new Object[]{token.internalTok.getKey(), ack, ex});
            this.callback.asyncOperationComplete(token);
        }
        if (ack == null) {
            log.fine(CLASS_NAME, "notifyResult", "649", new Object[]{token.internalTok.getKey(), ex});
            this.callback.asyncOperationComplete(token);
        }
    }

    public void connected() {
        log.fine(CLASS_NAME, "connected", "631");
        this.connected = true;
        this.pingSender.start();
    }

    public Vector resolveOldTokens(MqttException reason) {
        log.fine(CLASS_NAME, "resolveOldTokens", "632", new Object[]{reason});
        MqttException shutReason = reason;
        if (reason == null) {
            shutReason = new MqttException(32102);
        }
        Vector outT = this.tokenStore.getOutstandingTokens();
        Enumeration outTE = outT.elements();
        while (outTE.hasMoreElements()) {
            MqttToken tok = (MqttToken) outTE.nextElement();
            synchronized (tok) {
                if (!tok.isComplete() && !tok.internalTok.isCompletePending() && tok.getException() == null) {
                    tok.internalTok.setException(shutReason);
                }
            }
            if (!(tok instanceof MqttDeliveryToken)) {
                this.tokenStore.removeToken(tok.internalTok.getKey());
            }
        }
        return outT;
    }

    public void disconnected(MqttException reason) {
        log.fine(CLASS_NAME, "disconnected", "633", new Object[]{reason});
        this.connected = false;
        try {
            if (this.cleanSession) {
                clearState();
            }
            this.pendingMessages.clear();
            this.pendingFlows.clear();
            synchronized (this.pingOutstandingLock) {
                this.pingOutstanding = 0;
            }
        } catch (MqttException e) {
        }
    }

    private synchronized void releaseMessageId(int msgId) {
        this.inUseMsgIds.remove(new Integer(msgId));
    }

    private synchronized int getNextMessageId() throws MqttException {
        int startingMessageId = this.nextMsgId;
        int loopCount = 0;
        do {
            this.nextMsgId++;
            if (this.nextMsgId > 65535) {
                this.nextMsgId = 1;
            }
            if (this.nextMsgId == startingMessageId && (loopCount = loopCount + 1) == 2) {
                throw ExceptionHelper.createMqttException(32001);
            }
        } while (this.inUseMsgIds.containsKey(new Integer(this.nextMsgId)));
        Integer id = new Integer(this.nextMsgId);
        this.inUseMsgIds.put(id, id);
        return this.nextMsgId;
    }

    public void quiesce(long timeout) {
        if (timeout > 0) {
            log.fine(CLASS_NAME, "quiesce", "637", new Object[]{new Long(timeout)});
            synchronized (this.queueLock) {
                this.quiescing = true;
            }
            this.callback.quiesce();
            notifyQueueLock();
            synchronized (this.quiesceLock) {
                try {
                    int tokc = this.tokenStore.count();
                    if (tokc > 0 || this.pendingFlows.size() > 0 || !this.callback.isQuiesced()) {
                        log.fine(CLASS_NAME, "quiesce", "639", new Object[]{new Integer(this.actualInFlight), new Integer(this.pendingFlows.size()), new Integer(this.inFlightPubRels), new Integer(tokc)});
                        this.quiesceLock.wait(timeout);
                    }
                } catch (InterruptedException e) {
                }
            }
            synchronized (this.queueLock) {
                this.pendingMessages.clear();
                this.pendingFlows.clear();
                this.quiescing = false;
                this.actualInFlight = 0;
            }
            log.fine(CLASS_NAME, "quiesce", "640");
        }
    }

    public void notifyQueueLock() {
        synchronized (this.queueLock) {
            log.fine(CLASS_NAME, "notifyQueueLock", "638");
            this.queueLock.notifyAll();
        }
    }

    /* access modifiers changed from: protected */
    public void deliveryComplete(MqttPublish message) throws MqttPersistenceException {
        log.fine(CLASS_NAME, "deliveryComplete", "641", new Object[]{new Integer(message.getMessageId())});
        this.persistence.remove(getReceivedPersistenceKey((MqttWireMessage) message));
        this.inboundQoS2.remove(new Integer(message.getMessageId()));
    }

    /* access modifiers changed from: protected */
    public void deliveryComplete(int messageId) throws MqttPersistenceException {
        log.fine(CLASS_NAME, "deliveryComplete", "641", new Object[]{new Integer(messageId)});
        this.persistence.remove(getReceivedPersistenceKey(messageId));
        this.inboundQoS2.remove(new Integer(messageId));
    }

    public int getActualInFlight() {
        return this.actualInFlight;
    }

    public int getMaxInFlight() {
        return this.maxInflight;
    }

    /* access modifiers changed from: protected */
    public void close() {
        this.inUseMsgIds.clear();
        if (this.pendingMessages != null) {
            this.pendingMessages.clear();
        }
        this.pendingFlows.clear();
        this.outboundQoS2.clear();
        this.outboundQoS1.clear();
        this.outboundQoS0.clear();
        this.inboundQoS2.clear();
        this.tokenStore.clear();
        this.inUseMsgIds = null;
        this.pendingMessages = null;
        this.pendingFlows = null;
        this.outboundQoS2 = null;
        this.outboundQoS1 = null;
        this.outboundQoS0 = null;
        this.inboundQoS2 = null;
        this.tokenStore = null;
        this.callback = null;
        this.clientComms = null;
        this.persistence = null;
        this.pingCommand = null;
    }

    public Properties getDebug() {
        Properties props = new Properties();
        props.put("In use msgids", this.inUseMsgIds);
        props.put("pendingMessages", this.pendingMessages);
        props.put("pendingFlows", this.pendingFlows);
        props.put("maxInflight", new Integer(this.maxInflight));
        props.put("nextMsgID", new Integer(this.nextMsgId));
        props.put("actualInFlight", new Integer(this.actualInFlight));
        props.put("inFlightPubRels", new Integer(this.inFlightPubRels));
        props.put("quiescing", Boolean.valueOf(this.quiescing));
        props.put("pingoutstanding", new Integer(this.pingOutstanding));
        props.put("lastOutboundActivity", new Long(this.lastOutboundActivity));
        props.put("lastInboundActivity", new Long(this.lastInboundActivity));
        props.put("outboundQoS2", this.outboundQoS2);
        props.put("outboundQoS1", this.outboundQoS1);
        props.put("outboundQoS0", this.outboundQoS0);
        props.put("inboundQoS2", this.inboundQoS2);
        props.put("tokens", this.tokenStore);
        return props;
    }
}
