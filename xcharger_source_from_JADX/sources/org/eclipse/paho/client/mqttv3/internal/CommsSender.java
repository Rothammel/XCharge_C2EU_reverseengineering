package org.eclipse.paho.client.mqttv3.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttOutputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class CommsSender implements Runnable {
    private static final String CLASS_NAME = CommsSender.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private ClientComms clientComms = null;
    private ClientState clientState = null;
    private Object lifecycle = new Object();
    private MqttOutputStream out;
    private boolean running = false;
    private final Semaphore runningSemaphore = new Semaphore(1);
    private Thread sendThread = null;
    private Future senderFuture;
    private String threadName;
    private CommsTokenStore tokenStore = null;

    public CommsSender(ClientComms clientComms2, ClientState clientState2, CommsTokenStore tokenStore2, OutputStream out2) {
        this.out = new MqttOutputStream(clientState2, out2);
        this.clientComms = clientComms2;
        this.clientState = clientState2;
        this.tokenStore = tokenStore2;
        log.setResourceName(clientComms2.getClient().getClientId());
    }

    public void start(String threadName2, ExecutorService executorService) {
        this.threadName = threadName2;
        synchronized (this.lifecycle) {
            if (!this.running) {
                this.running = true;
                this.senderFuture = executorService.submit(this);
            }
        }
    }

    public void stop() {
        synchronized (this.lifecycle) {
            if (this.senderFuture != null) {
                this.senderFuture.cancel(true);
            }
            log.fine(CLASS_NAME, "stop", "800");
            if (this.running) {
                this.running = false;
                if (!Thread.currentThread().equals(this.sendThread)) {
                    while (this.running) {
                        try {
                            this.clientState.notifyQueueLock();
                            this.runningSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            this.runningSemaphore.release();
                        } catch (Throwable th) {
                            this.runningSemaphore.release();
                            throw th;
                        }
                    }
                    this.runningSemaphore.release();
                }
            }
            this.sendThread = null;
            log.fine(CLASS_NAME, "stop", "801");
        }
    }

    public void run() {
        this.sendThread = Thread.currentThread();
        this.sendThread.setName(this.threadName);
        MqttWireMessage message = null;
        try {
            this.runningSemaphore.acquire();
            while (this.running && this.out != null) {
                try {
                    message = this.clientState.get();
                    if (message != null) {
                        log.fine(CLASS_NAME, "run", "802", new Object[]{message.getKey(), message});
                        if (message instanceof MqttAck) {
                            this.out.write(message);
                            this.out.flush();
                        } else {
                            MqttToken token = this.tokenStore.getToken(message);
                            if (token != null) {
                                synchronized (token) {
                                    this.out.write(message);
                                    this.out.flush();
                                    this.clientState.notifySent(message);
                                }
                            } else {
                                continue;
                            }
                        }
                    } else {
                        log.fine(CLASS_NAME, "run", "803");
                        this.running = false;
                    }
                } catch (IOException ex) {
                    if (!(message instanceof MqttDisconnect)) {
                        throw ex;
                    }
                } catch (MqttException me) {
                    handleRunException(message, me);
                } catch (Exception ex2) {
                    handleRunException(message, ex2);
                } catch (Throwable th) {
                    this.running = false;
                    this.runningSemaphore.release();
                    throw th;
                }
            }
            this.running = false;
            this.runningSemaphore.release();
            log.fine(CLASS_NAME, "run", "805");
        } catch (InterruptedException e) {
            this.running = false;
        }
    }

    private void handleRunException(MqttWireMessage message, Exception ex) {
        MqttException mex;
        log.fine(CLASS_NAME, "handleRunException", "804", (Object[]) null, ex);
        if (!(ex instanceof MqttException)) {
            mex = new MqttException(32109, ex);
        } else {
            mex = (MqttException) ex;
        }
        this.running = false;
        this.clientComms.shutdownConnection((MqttToken) null, mex);
    }
}
