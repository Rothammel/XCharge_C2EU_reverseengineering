package org.eclipse.paho.client.mqttv3.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class CommsReceiver implements Runnable {
    private static final String CLASS_NAME = CommsReceiver.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private ClientComms clientComms = null;
    private ClientState clientState = null;

    /* renamed from: in */
    private MqttInputStream f200in;
    private Object lifecycle = new Object();
    private Thread recThread = null;
    private Future receiverFuture;
    private volatile boolean receiving;
    private boolean running = false;
    private final Semaphore runningSemaphore = new Semaphore(1);
    private String threadName;
    private CommsTokenStore tokenStore = null;

    public CommsReceiver(ClientComms clientComms2, ClientState clientState2, CommsTokenStore tokenStore2, InputStream in) {
        this.f200in = new MqttInputStream(clientState2, in);
        this.clientComms = clientComms2;
        this.clientState = clientState2;
        this.tokenStore = tokenStore2;
        log.setResourceName(clientComms2.getClient().getClientId());
    }

    public void start(String threadName2, ExecutorService executorService) {
        this.threadName = threadName2;
        log.fine(CLASS_NAME, "start", "855");
        synchronized (this.lifecycle) {
            if (!this.running) {
                this.running = true;
                this.receiverFuture = executorService.submit(this);
            }
        }
    }

    public void stop() {
        synchronized (this.lifecycle) {
            if (this.receiverFuture != null) {
                this.receiverFuture.cancel(true);
            }
            log.fine(CLASS_NAME, "stop", "850");
            if (this.running) {
                this.running = false;
                this.receiving = false;
                if (!Thread.currentThread().equals(this.recThread)) {
                    try {
                        this.runningSemaphore.acquire();
                        this.runningSemaphore.release();
                    } catch (InterruptedException e) {
                        this.runningSemaphore.release();
                    } catch (Throwable th) {
                        this.runningSemaphore.release();
                        throw th;
                    }
                }
            }
        }
        this.recThread = null;
        log.fine(CLASS_NAME, "stop", "851");
    }

    public void run() {
        this.recThread = Thread.currentThread();
        this.recThread.setName(this.threadName);
        MqttToken token = null;
        try {
            this.runningSemaphore.acquire();
            while (this.running && this.f200in != null) {
                try {
                    log.fine(CLASS_NAME, "run", "852");
                    this.receiving = this.f200in.available() > 0;
                    MqttWireMessage message = this.f200in.readMqttWireMessage();
                    this.receiving = false;
                    if (message instanceof MqttAck) {
                        token = this.tokenStore.getToken(message);
                        if (token != null) {
                            synchronized (token) {
                                this.clientState.notifyReceivedAck((MqttAck) message);
                            }
                        } else if ((message instanceof MqttPubRec) || (message instanceof MqttPubComp) || (message instanceof MqttPubAck)) {
                            log.fine(CLASS_NAME, "run", "857");
                        } else {
                            throw new MqttException(6);
                        }
                    } else if (message != null) {
                        this.clientState.notifyReceivedMsg(message);
                    }
                    this.receiving = false;
                    this.runningSemaphore.release();
                } catch (MqttException ex) {
                    try {
                        log.fine(CLASS_NAME, "run", "856", (Object[]) null, ex);
                        this.running = false;
                        this.clientComms.shutdownConnection(token, ex);
                    } finally {
                        this.receiving = false;
                        this.runningSemaphore.release();
                    }
                } catch (IOException ioe) {
                    log.fine(CLASS_NAME, "run", "853");
                    this.running = false;
                    if (!this.clientComms.isDisconnecting()) {
                        this.clientComms.shutdownConnection(token, new MqttException(32109, ioe));
                    }
                    this.receiving = false;
                    this.runningSemaphore.release();
                }
            }
            log.fine(CLASS_NAME, "run", "854");
        } catch (InterruptedException e) {
            this.running = false;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isReceiving() {
        return this.receiving;
    }
}
