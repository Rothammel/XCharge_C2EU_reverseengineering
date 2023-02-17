package org.eclipse.paho.client.mqttv3.internal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class CommsCallback implements Runnable {
    private static final int INBOUND_QUEUE_SIZE = 10;
    private Future callbackFuture;
    private Thread callbackThread;
    private ClientComms clientComms;
    private ClientState clientState;
    private MqttCallback mqttCallback;
    private MqttCallbackExtended reconnectInternalCallback;
    private String threadName;
    private static final String CLASS_NAME = CommsCallback.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    public boolean running = false;
    private boolean quiescing = false;
    private Object lifecycle = new Object();
    private Object workAvailable = new Object();
    private Object spaceAvailable = new Object();
    private boolean manualAcks = false;
    private final Semaphore runningSemaphore = new Semaphore(1);
    private Vector messageQueue = new Vector(10);
    private Vector completeQueue = new Vector(10);
    private Hashtable callbacks = new Hashtable();

    /* JADX INFO: Access modifiers changed from: package-private */
    public CommsCallback(ClientComms clientComms) {
        this.clientComms = clientComms;
        log.setResourceName(clientComms.getClient().getClientId());
    }

    public void setClientState(ClientState clientState) {
        this.clientState = clientState;
    }

    public void start(String threadName, ExecutorService executorService) {
        this.threadName = threadName;
        synchronized (this.lifecycle) {
            if (!this.running) {
                this.messageQueue.clear();
                this.completeQueue.clear();
                this.running = true;
                this.quiescing = false;
                this.callbackFuture = executorService.submit(this);
            }
        }
    }

    public void stop() {
        synchronized (this.lifecycle) {
            if (this.callbackFuture != null) {
                this.callbackFuture.cancel(true);
            }
            if (this.running) {
                log.fine(CLASS_NAME, "stop", "700");
                this.running = false;
                if (!Thread.currentThread().equals(this.callbackThread)) {
                    try {
                        synchronized (this.workAvailable) {
                            log.fine(CLASS_NAME, "stop", "701");
                            this.workAvailable.notifyAll();
                        }
                        this.runningSemaphore.acquire();
                        this.runningSemaphore.release();
                    } catch (InterruptedException e) {
                        this.runningSemaphore.release();
                    }
                }
            }
            this.callbackThread = null;
            log.fine(CLASS_NAME, "stop", "703");
        }
    }

    public void setCallback(MqttCallback mqttCallback) {
        this.mqttCallback = mqttCallback;
    }

    public void setReconnectCallback(MqttCallbackExtended callback) {
        this.reconnectInternalCallback = callback;
    }

    public void setManualAcks(boolean manualAcks) {
        this.manualAcks = manualAcks;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.callbackThread = Thread.currentThread();
        this.callbackThread.setName(this.threadName);
        try {
            this.runningSemaphore.acquire();
            while (this.running) {
                try {
                    try {
                        synchronized (this.workAvailable) {
                            if (this.running && this.messageQueue.isEmpty() && this.completeQueue.isEmpty()) {
                                log.fine(CLASS_NAME, "run", "704");
                                this.workAvailable.wait();
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                    if (this.running) {
                        MqttToken token = null;
                        synchronized (this.completeQueue) {
                            if (!this.completeQueue.isEmpty()) {
                                token = (MqttToken) this.completeQueue.elementAt(0);
                                this.completeQueue.removeElementAt(0);
                            }
                        }
                        if (token != null) {
                            handleActionComplete(token);
                        }
                        MqttPublish message = null;
                        synchronized (this.messageQueue) {
                            if (!this.messageQueue.isEmpty()) {
                                message = (MqttPublish) this.messageQueue.elementAt(0);
                                this.messageQueue.removeElementAt(0);
                            }
                        }
                        if (message != null) {
                            handleMessage(message);
                        }
                    }
                    if (this.quiescing) {
                        this.clientState.checkQuiesceLock();
                    }
                    this.runningSemaphore.release();
                    synchronized (this.spaceAvailable) {
                        log.fine(CLASS_NAME, "run", "706");
                        this.spaceAvailable.notifyAll();
                    }
                } catch (Throwable th) {
                    this.runningSemaphore.release();
                    synchronized (this.spaceAvailable) {
                        log.fine(CLASS_NAME, "run", "706");
                        this.spaceAvailable.notifyAll();
                        throw th;
                    }
                }
            }
        } catch (InterruptedException e2) {
            this.running = false;
        }
    }

    private void handleActionComplete(MqttToken token) throws MqttException {
        synchronized (token) {
            log.fine(CLASS_NAME, "handleActionComplete", "705", new Object[]{token.internalTok.getKey()});
            if (token.isComplete()) {
                this.clientState.notifyComplete(token);
            }
            token.internalTok.notifyComplete();
            if (!token.internalTok.isNotified()) {
                if (this.mqttCallback != null && (token instanceof MqttDeliveryToken) && token.isComplete()) {
                    this.mqttCallback.deliveryComplete((MqttDeliveryToken) token);
                }
                fireActionEvent(token);
            }
            if (token.isComplete() && ((token instanceof MqttDeliveryToken) || (token.getActionCallback() instanceof IMqttActionListener))) {
                token.internalTok.setNotified(true);
            }
        }
    }

    public void connectionLost(MqttException cause) {
        try {
            if (this.mqttCallback != null && cause != null) {
                log.fine(CLASS_NAME, "connectionLost", "708", new Object[]{cause});
                this.mqttCallback.connectionLost(cause);
            }
            if (this.reconnectInternalCallback != null && cause != null) {
                this.reconnectInternalCallback.connectionLost(cause);
            }
        } catch (Throwable t) {
            log.fine(CLASS_NAME, "connectionLost", "720", new Object[]{t});
        }
    }

    public void fireActionEvent(MqttToken token) {
        IMqttActionListener asyncCB;
        if (token != null && (asyncCB = token.getActionCallback()) != null) {
            if (token.getException() == null) {
                log.fine(CLASS_NAME, "fireActionEvent", "716", new Object[]{token.internalTok.getKey()});
                asyncCB.onSuccess(token);
                return;
            }
            log.fine(CLASS_NAME, "fireActionEvent", "716", new Object[]{token.internalTok.getKey()});
            asyncCB.onFailure(token, token.getException());
        }
    }

    public void messageArrived(MqttPublish sendMessage) {
        if (this.mqttCallback != null || this.callbacks.size() > 0) {
            synchronized (this.spaceAvailable) {
                while (this.running && !this.quiescing && this.messageQueue.size() >= 10) {
                    try {
                        log.fine(CLASS_NAME, "messageArrived", "709");
                        this.spaceAvailable.wait(200L);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!this.quiescing) {
                this.messageQueue.addElement(sendMessage);
                synchronized (this.workAvailable) {
                    log.fine(CLASS_NAME, "messageArrived", "710");
                    this.workAvailable.notifyAll();
                }
            }
        }
    }

    public void quiesce() {
        this.quiescing = true;
        synchronized (this.spaceAvailable) {
            log.fine(CLASS_NAME, "quiesce", "711");
            this.spaceAvailable.notifyAll();
        }
    }

    public boolean isQuiesced() {
        return this.quiescing && this.completeQueue.size() == 0 && this.messageQueue.size() == 0;
    }

    private void handleMessage(MqttPublish publishMessage) throws MqttException, Exception {
        String destName = publishMessage.getTopicName();
        log.fine(CLASS_NAME, "handleMessage", "713", new Object[]{new Integer(publishMessage.getMessageId()), destName});
        deliverMessage(destName, publishMessage.getMessageId(), publishMessage.getMessage());
        if (!this.manualAcks) {
            if (publishMessage.getMessage().getQos() == 1) {
                this.clientComms.internalSend(new MqttPubAck(publishMessage), new MqttToken(this.clientComms.getClient().getClientId()));
            } else if (publishMessage.getMessage().getQos() == 2) {
                this.clientComms.deliveryComplete(publishMessage);
                MqttPubComp pubComp = new MqttPubComp(publishMessage);
                this.clientComms.internalSend(pubComp, new MqttToken(this.clientComms.getClient().getClientId()));
            }
        }
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        if (qos == 1) {
            this.clientComms.internalSend(new MqttPubAck(messageId), new MqttToken(this.clientComms.getClient().getClientId()));
        } else if (qos == 2) {
            this.clientComms.deliveryComplete(messageId);
            MqttPubComp pubComp = new MqttPubComp(messageId);
            this.clientComms.internalSend(pubComp, new MqttToken(this.clientComms.getClient().getClientId()));
        }
    }

    public void asyncOperationComplete(MqttToken token) {
        if (this.running) {
            this.completeQueue.addElement(token);
            synchronized (this.workAvailable) {
                log.fine(CLASS_NAME, "asyncOperationComplete", "715", new Object[]{token.internalTok.getKey()});
                this.workAvailable.notifyAll();
            }
            return;
        }
        try {
            handleActionComplete(token);
        } catch (Throwable ex) {
            log.fine(CLASS_NAME, "asyncOperationComplete", "719", null, ex);
            this.clientComms.shutdownConnection(null, new MqttException(ex));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Thread getThread() {
        return this.callbackThread;
    }

    public void setMessageListener(String topicFilter, IMqttMessageListener messageListener) {
        this.callbacks.put(topicFilter, messageListener);
    }

    public void removeMessageListener(String topicFilter) {
        this.callbacks.remove(topicFilter);
    }

    public void removeMessageListeners() {
        this.callbacks.clear();
    }

    protected boolean deliverMessage(String topicName, int messageId, MqttMessage aMessage) throws Exception {
        boolean delivered = false;
        Enumeration keys = this.callbacks.keys();
        while (keys.hasMoreElements()) {
            String topicFilter = (String) keys.nextElement();
            if (MqttTopic.isMatched(topicFilter, topicName)) {
                aMessage.setId(messageId);
                ((IMqttMessageListener) this.callbacks.get(topicFilter)).messageArrived(topicName, aMessage);
                delivered = true;
            }
        }
        if (this.mqttCallback != null && !delivered) {
            aMessage.setId(messageId);
            this.mqttCallback.messageArrived(topicName, aMessage);
            return true;
        }
        return delivered;
    }
}